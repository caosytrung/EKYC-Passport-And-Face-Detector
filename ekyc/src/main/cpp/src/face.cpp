#include <iostream>
#include <fstream>
#include <chrono>
#include <algorithm>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "opencv2/opencv.hpp"
#include "face.h"
#include "cpu.h"
#include "tools.h"


using namespace std;

#define clip(x, y) (x < 0 ? 0 : (x > y ? y : x))

static inline float intersection_area(const Object &a, const Object &b) {
    cv::Rect_<float> inter = a.rect & b.rect;
    return inter.area();
}

static void qsort_descent_inplace(std::vector<Object> &faceobjects, int left, int right) {
    int i = left;
    int j = right;
    float p = faceobjects[(left + right) / 2].prob;

    while (i <= j) {
        while (faceobjects[i].prob > p)
            i++;

        while (faceobjects[j].prob < p)
            j--;

        if (i <= j) {
            std::swap(faceobjects[i], faceobjects[j]);
            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(faceobjects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(faceobjects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector<Object> &faceobjects) {
    if (faceobjects.empty())
        return;

    qsort_descent_inplace(faceobjects, 0, faceobjects.size() - 1);
}

static void
non_max_suppression_face(const std::vector<Object> &faceobjects, std::vector<int> &picked,
                         float nms_threshold) {
    picked.clear();

    const int n = faceobjects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++) {
        areas[i] = faceobjects[i].rect.area();
    }

    for (int i = 0; i < n; i++) {
        const Object &a = faceobjects[i];

        int keep = 1;
        for (int j = 0; j < (int) picked.size(); j++) {
            const Object &b = faceobjects[picked[j]];
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[picked[j]] - inter_area;
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.push_back(i);
    }
}

static inline float sigmoid(float x) {
    return static_cast<float>(1.f / (1.f + exp(-x)));
}

static void generate_proposals(const ncnn::Mat &anchors, int stride, const ncnn::Mat &in_pad,
                               const ncnn::Mat &feat_blob, float prob_threshold,
                               std::vector<Object> &objects) {
    const int num_grid = feat_blob.h;

    int num_grid_x;
    int num_grid_y;
    if (in_pad.w > in_pad.h) {
        num_grid_x = in_pad.w / stride;
        num_grid_y = num_grid / num_grid_x;
    } else {
        num_grid_y = in_pad.h / stride;
        num_grid_x = num_grid / num_grid_y;
    }

    const int num_class = feat_blob.w - 5 - 10;
    const int num_anchors = anchors.w / 2;

    for (int q = 0; q < num_anchors; q++) {
        const float anchor_w = anchors[q * 2];
        const float anchor_h = anchors[q * 2 + 1];

        const ncnn::Mat feat = feat_blob.channel(q);

        for (int i = 0; i < num_grid_y; i++) {
            for (int j = 0; j < num_grid_x; j++) {
                const float *featptr = feat.row(i * num_grid_x + j);
                int class_index = 0;
                float class_score = -FLT_MAX;
                for (int k = 0; k < num_class; k++) {
                    float score = featptr[5 + 10 + k];
                    if (score > class_score) {
                        class_index = k;
                        class_score = score;
                    }
                }

                float box_score = featptr[4];

                float confidence = sigmoid(box_score);

                if (confidence >= prob_threshold) {
                    float dx = sigmoid(featptr[0]);
                    float dy = sigmoid(featptr[1]);
                    float dw = sigmoid(featptr[2]);
                    float dh = sigmoid(featptr[3]);

                    float pb_cx = (dx * 2.f - 0.5f + j) * stride;
                    float pb_cy = (dy * 2.f - 0.5f + i) * stride;

                    float pb_w = pow(dw * 2.f, 2) * anchor_w;
                    float pb_h = pow(dh * 2.f, 2) * anchor_h;

                    float x0 = pb_cx - pb_w * 0.5f;
                    float y0 = pb_cy - pb_h * 0.5f;
                    float x1 = pb_cx + pb_w * 0.5f;
                    float y1 = pb_cy + pb_h * 0.5f;

                    Object obj;
                    obj.rect.x = x0;
                    obj.rect.y = y0;
                    obj.rect.width = x1 - x0;
                    obj.rect.height = y1 - y0;
                    obj.label = class_index;
                    obj.prob = confidence;

                    for (int l = 0; l < 5; l++) {
                        float x = featptr[2 * l + 5] * anchor_w + j * stride;
                        float y = featptr[2 * l + 1 + 5] * anchor_h + i * stride;
                        obj.pts.push_back(cv::Point2f(x, y));
                    }
                    objects.push_back(obj);
                }
            }
        }
    }
}

int
detect(const ncnn::Net &net, const cv::Mat rgb, std::vector<Object> &objects, float prob_threshold,
       float nms_threshold) {
    int target_size = 640;
    int img_w = rgb.cols;
    int img_h = rgb.rows;

    int w = img_w;
    int h = img_h;
    float scale = 1.f;
    if (w > h) {
        scale = (float) target_size / w;
        w = target_size;
        h = h * scale;
    } else {
        scale = (float) target_size / h;
        h = target_size;
        w = w * scale;
    }

    ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgb.data, ncnn::Mat::PIXEL_RGB, img_w, img_h, w,
                                                 h);

    int wpad = (w + 31) / 32 * 32 - w;
    int hpad = (h + 31) / 32 * 32 - h;
    ncnn::Mat in_pad;
    ncnn::copy_make_border(in, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2,
                           ncnn::BORDER_CONSTANT, 114.f);

    float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
    in_pad.substract_mean_normalize(0, norm_vals);

    ncnn::Extractor ex = net.create_extractor();

    ex.input("data", in_pad);
    ncnn::Mat out1, out2, out3;
    std::vector<Object> proposals;
    ex.extract("981", out1);
    ex.extract("983", out2);
    ex.extract("985", out3);

    ncnn::Mat anchors0(6);
    anchors0[0] = 4.f;
    anchors0[1] = 5.f;
    anchors0[2] = 8.f;
    anchors0[3] = 10.f;
    anchors0[4] = 13.f;
    anchors0[5] = 16.f;
    std::vector<Object> objects8;
    generate_proposals(anchors0, 8, in_pad, out1, prob_threshold, objects8);
    proposals.insert(proposals.end(), objects8.begin(), objects8.end());

    ncnn::Mat anchors1(6);
    anchors1[0] = 23.f;
    anchors1[1] = 29.f;
    anchors1[2] = 43.f;
    anchors1[3] = 55.f;
    anchors1[4] = 73.f;
    anchors1[5] = 105.f;
    std::vector<Object> objects16;
    generate_proposals(anchors1, 16, in_pad, out2, prob_threshold, objects16);
    proposals.insert(proposals.end(), objects16.begin(), objects16.end());

    ncnn::Mat anchors2(6);
    anchors2[0] = 146.f;
    anchors2[1] = 217.f;
    anchors2[2] = 231.f;
    anchors2[3] = 300.f;
    anchors2[4] = 335.f;
    anchors2[5] = 433.f;
    std::vector<Object> objects32;
    generate_proposals(anchors2, 32, in_pad, out3, prob_threshold, objects32);
    proposals.insert(proposals.end(), objects32.begin(), objects32.end());

    qsort_descent_inplace(proposals);
    std::vector<int> picked;
    non_max_suppression_face(proposals, picked, nms_threshold);

    int count = picked.size();
    objects.resize(count);
    for (int i = 0; i < count; i++) {
        objects[i] = proposals[picked[i]];
        float x0 = (objects[i].rect.x - (wpad / 2)) / scale;
        float y0 = (objects[i].rect.y - (hpad / 2)) / scale;
        float x1 = (objects[i].rect.x + objects[i].rect.width - (wpad / 2)) / scale;
        float y1 = (objects[i].rect.y + objects[i].rect.height - (hpad / 2)) / scale;

        for (int j = 0; j < objects[i].pts.size(); j++) {
            float ptx = (objects[i].pts[j].x - (wpad / 2)) / scale;
            float pty = (objects[i].pts[j].y - (hpad / 2)) / scale;
            objects[i].pts[j] = cv::Point2f(ptx, pty);
        }

        x0 = std::max(std::min(x0, (float) (img_w - 1)), 0.f);
        y0 = std::max(std::min(y0, (float) (img_h - 1)), 0.f);
        x1 = std::max(std::min(x1, (float) (img_w - 1)), 0.f);
        y1 = std::max(std::min(y1, (float) (img_h - 1)), 0.f);

        objects[i].rect.x = x0;
        objects[i].rect.y = y0;
        objects[i].rect.width = x1 - x0;
        objects[i].rect.height = y1 - y0;
    }
    return 0;
}

std::vector<float> ncnn2vector(ncnn::Mat img) {
    std::vector<float> vec(img.w);
    for (int i = 0; i < vec.size(); i++)
        vec.at(i) = img[i];
    return vec;
}

cv::Mat ncnn2cv(ncnn::Mat img) {
    unsigned char pix[img.h * img.w * 3];
    img.to_pixels(pix, ncnn::Mat::PIXEL_BGR);
    cv::Mat cv_img(img.h, img.w, CV_8UC3);
    for (int i = 0; i < cv_img.rows; i++) {
        for (int j = 0; j < cv_img.cols; j++) {
            cv_img.at<cv::Vec3b>(i, j)[0] = pix[3 * (i * cv_img.cols + j)];
            cv_img.at<cv::Vec3b>(i, j)[1] = pix[3 * (i * cv_img.cols + j) + 1];
            cv_img.at<cv::Vec3b>(i, j)[2] = pix[3 * (i * cv_img.cols + j) + 2];
        }
    }
    return cv_img;
}

ncnn::Mat resize(ncnn::Mat src, int w, int h) {
    int src_w = src.w;
    int src_h = src.h;
    unsigned char *u_src = new unsigned char[src_w * src_h * 3];
    src.to_pixels(u_src, ncnn::Mat::PIXEL_RGB);
    unsigned char *u_dst = new unsigned char[w * h * 3];
    ncnn::resize_bilinear_c3(u_src, src_w, src_h, u_dst, w, h);
    ncnn::Mat dst = ncnn::Mat::from_pixels(u_dst, ncnn::Mat::PIXEL_RGB, w, h);
    delete[] u_src;
    delete[] u_dst;
    return dst;
}

ncnn::Mat bgr2rgb(ncnn::Mat src) {
    int src_w = src.w;
    int src_h = src.h;
    unsigned char *u_rgb = new unsigned char[src_w * src_h * 3];
    src.to_pixels(u_rgb, ncnn::Mat::PIXEL_BGR2RGB);
    ncnn::Mat dst = ncnn::Mat::from_pixels(u_rgb, ncnn::Mat::PIXEL_RGB, src_w, src_h);
    delete[] u_rgb;
    return dst;
}

cv::Mat crop_img(cv::Mat image, vector<int> box) {
    int wid = image.cols;
    int hei = image.rows;
    int sx = box[0];
    int sy = box[1];
    int ex = box[2];
    int ey = box[3];
    int dh = ey - sy;
    int dw = ex - sx;
    int dsx, dex, dsy, dey;

    if (sx < 0) {
        dsx = -sx;
        sx = 0;
    } else
        dsx = 0;
    if (ex > wid) {
        dex = dw - (ex - wid);
        ex = wid;
    } else
        dex = dw;
    if (sy < 0) {
        dsy = -sy;
        sy = 0;
    } else
        dsy = 0;
    if (ey > hei) {
        dey = dh - (ey - hei);
        ey = hei;
    } else
        dey = dh;


    if (ex-sx > 0 && ey - sy > 0 && sx > 0 && sy > 0) {
        cv::Mat out_img = image(cv::Rect(sx, sy, ex-sx, ey-sy)).clone();
        return out_img;
    } else {
        return image;
    }
}

cv::Mat P2sRt(cv::Mat P) {
    // P: (4, 3) Affine Camera Matrix.
    cv::Mat R1 = P(cv::Rect(0, 0, 3, 1)).clone();
    cv::Mat R2 = P(cv::Rect(0, 1, 3, 1)).clone();
    cv::Mat r1 = R1 / l2norm(R1);
    cv::Mat r2 = R2 / l2norm(R2);
    cv::Mat r3 = r1.cross(r2);

    cv::Mat R = concatenate(r1, r2, r3);

    return R;
}

std::vector<float> calc_pose(cv::Mat param) {
    cv::Mat P = param(cv::Rect(0, 0, 12, 1));
    P = P.reshape(0, 3);
    cv::Mat R = P2sRt(P);
    std::vector<float> pose = matrix2angle(R);
    for (int i = 0; i < 3; i++)
        pose[i] = pose[i] * 180 / 3.14159;

    return pose;
}

std::vector<float>
estimate_pose(ncnn::Net *model, cv::Mat image, std::vector<int> box, cv::Mat mean_, cv::Mat std_) {
    extern float pixel_mean[3];
    extern float pixel_std[3];

    ncnn::Extractor _detector = model->create_extractor();
    std::vector<int> expanded_box = expand_box(image.cols, image.rows, box);
    cv::Mat face = crop_img(image, expanded_box);
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(face.data, ncnn::Mat::PIXEL_BGR, face.cols,
                                                 face.rows, 120, 120);
    in.substract_mean_normalize(pixel_mean, pixel_std);

    _detector.input("input", in);
    ncnn::Mat out;
    _detector.extract("output", out);
    std::vector<float> vec;
    vec = ncnn2vector(out);
    cv::Mat param = cv::Mat(1, 62, CV_32FC1, vec.data()).clone();
    param = param.mul(std_.reshape(0, 1)) + mean_.reshape(0, 1);
    std::vector<float> pose = calc_pose(param);

    return pose;
}

std::vector<Object> detectFace(cv::Mat &frame, const ncnn::Net &net) {
//    cv::Mat frame;
//    frame = cv::imread(imgPath, 1);

    std::vector<Object> objects;
    float prob_threshold = 0.4f;
    float nms_threshold = 0.45f;
    auto start = std::chrono::high_resolution_clock::now();
    detect(net, frame, objects, prob_threshold, nms_threshold);
    auto stop = std::chrono::high_resolution_clock::now();
    auto duration = duration_cast<std::chrono::milliseconds>(stop - start);

    int frameWidth = frame.size().width;
    int frameHeight = frame.size().height;

    __android_log_print(ANDROID_LOG_INFO, "sometag", "milliseconds: %d", duration);
    __android_log_print(ANDROID_LOG_INFO, "sometag1", "frameHeight: %d", frameHeight);

    return objects;
}

std::vector<float> detectPose(cv::Mat &image,
                              std::vector<Object> &faces,
                              ncnn::Net &landmark_net,
                              const Mat &landm_mean,
                              const Mat &landm_std) {
    if (faces.empty()) {
        std::vector<float> empty(1, 0);
        return empty;
    }

    if (faces.size() > 1) {
        std::vector<float> empty(1, 1);
        return empty;
    }


    if ((int) faces[0].rect.x <= 0 &&
        (int) faces[0].rect.y <= 0 &&
        (int) faces[0].rect.width <= 0 &&
        (int) faces[0].rect.height <= 0) {
        std::vector<float> empty(0);
        return empty;
    }

    std::vector<int> bbox = { (int) faces[0].rect.x, (int) faces[0].rect.y,(int) faces[0].rect.x +  (int) faces[0].rect.width,(int) faces[0].rect.y +  (int) faces[0].rect.height}; // this bbox got from FaceDetection model

// Get consts from config file
    extern float pixel_mean[3];
    extern float pixel_std[3];


// get pose, pose = {yaw, pitch, roll}
    std::vector<float> pose = estimate_pose(&landmark_net, image, bbox, landm_mean, landm_std);
    printf("Pose: yaw: %6.2f, pitch: %6.2f, roll: %6.2f \n", pose[0], pose[1], pose[2]);
    cout << "Face is: " << endl;

    return pose;
}