//
// Adapt from https://github.com/RangiLyu/nanodet/tree/main/demo_ncnn
//

#include "nanodet.hpp"
#include "dto.hpp"
// #include <benchmark.h>
#include <vector>
#include <opencv2/imgproc/imgproc.hpp>

namespace ekyc_card_detection {

    inline float fast_exp(float x) {
        union {
            uint32_t i;
            float f;
        } v{};
        v.i = (1 << 23) * (1.4426950409 * x + 126.93490512f);
        return v.f;
    }

    inline float sigmoid(float x) {
        return 1.0f / (1.0f + fast_exp(-x));
    }

    template<typename _Tp>
    int activation_function_softmax(const _Tp *src, _Tp *dst, int length) {
        const _Tp alpha = *std::max_element(src, src + length);
        _Tp denominator{0};

        for (int i = 0; i < length; ++i) {
            dst[i] = fast_exp(src[i] - alpha);
            denominator += dst[i];
        }

        for (int i = 0; i < length; ++i) {
            dst[i] /= denominator;
        }

        return 0;
    }


    static void generate_grid_center_priors(const int input_height, const int input_width,
                                            std::vector<int> &strides,
                                            std::vector<CenterPrior> &center_priors) {
        for (int i = 0; i < (int) strides.size(); i++) {
            int stride = strides[i];
            int feat_w = ceil((float) input_width / stride);
            int feat_h = ceil((float) input_height / stride);
            for (int y = 0; y < feat_h; y++) {
                for (int x = 0; x < feat_w; x++) {
                    CenterPrior ct;
                    ct.x = x;
                    ct.y = y;
                    ct.stride = stride;
                    center_priors.push_back(ct);
                }
            }
        }
    }


    bool NanoDet::hasGPU = false;
    NanoDet *NanoDet::detector = nullptr;

    NanoDet::NanoDet() {
        this->Net = new ncnn::Net();
    }
//
//    LoadState NanoDet::load_model(const char *param, const char *bin) {
//        // this->Net->opt.use_vulkan_compute = this->hasGPU
//        // this->Net->opt.use_fp16_arithmetic = true;
//        if (this->Net->load_param(param))
//            return LoadState::kLoadParamFailed;
//        if (this->Net->load_model(bin))
//            return LoadState::kLoadBinFailed;
//        return LoadState::kLoadSuccess;
//    }

    LoadState NanoDet::load_model(AAssetManager *mrg) {
        // model.opt.use_vulkan_compute = false;r
        if (this->Net->load_param(mrg, "card.param")) {
            return LoadState::kLoadParamFailed;
        }
        if (this->Net->load_model(mrg, "card.bin")) {
            return LoadState::kLoadBinFailed;
        }

        return LoadState::kLoadSuccess;
    }


    NanoDet::~NanoDet() {
        delete this->Net;
    }

    int
    resize_uniform(const cv::Mat &src, cv::Mat &dst, cv::Size dst_size, object_rect &effect_area) {
        int w = src.cols;
        int h = src.rows;
        int dst_w = dst_size.width;
        int dst_h = dst_size.height;
        dst = cv::Mat(cv::Size(dst_w, dst_h), CV_8UC3, cv::Scalar(0));

        float ratio_src = w * 1.0 / h;
        float ratio_dst = dst_w * 1.0 / dst_h;

        int tmp_w = 0;
        int tmp_h = 0;
        if (ratio_src > ratio_dst) {
            tmp_w = dst_w;
            tmp_h = floor((dst_w * 1.0 / w) * h);
        } else if (ratio_src < ratio_dst) {
            tmp_h = dst_h;
            tmp_w = floor((dst_h * 1.0 / h) * w);
        } else {
            cv::resize(src, dst, dst_size);
            effect_area.x = 0;
            effect_area.y = 0;
            effect_area.width = dst_w;
            effect_area.height = dst_h;
            return 0;
        }

        cv::Mat tmp;
        cv::resize(src, tmp, cv::Size(tmp_w, tmp_h));

        if (tmp_w != dst_w) {
            int index_w = floor((dst_w - tmp_w) / 2.0);
            for (int i = 0; i < dst_h; i++) {
                memcpy(dst.data + i * dst_w * 3 + index_w * 3, tmp.data + i * tmp_w * 3, tmp_w * 3);
            }
            effect_area.x = index_w;
            effect_area.y = 0;
            effect_area.width = tmp_w;
            effect_area.height = tmp_h;
        } else if (tmp_h != dst_h) {
            int index_h = floor((dst_h - tmp_h) / 2.0);
            memcpy(dst.data + index_h * dst_w * 3, tmp.data, tmp_w * tmp_h * 3);
            effect_area.x = 0;
            effect_area.y = index_h;
            effect_area.width = tmp_w;
            effect_area.height = tmp_h;
        } else {
            printf("error\n");
        }
        return 0;
    }

    void NanoDet::preprocess(const cv::Mat &image, ncnn::Mat &in) {
        int height = this->input_size[0];
        int width = this->input_size[1];
        cv::Mat resized_img;
        resize_uniform(image, resized_img, cv::Size(width, height), this->effect_roi);

        this->effect_roi.ori_height = image.rows;
        this->effect_roi.ori_width = image.cols;

        int img_w = resized_img.cols;
        int img_h = resized_img.rows;

        in = ncnn::Mat::from_pixels(resized_img.data, ncnn::Mat::PIXEL_BGR, img_w, img_h);
        // in = ncnn::Mat::from_pixels_resize(image.data, ncnn::Mat::PIXEL_BGR, img_w, img_h, this->input_size[0], this->input_size[1]);

        in.substract_mean_normalize(mean_vals, norm_vals);
    }

    void NanoDet::postprocess(std::vector<BoxInfo> &dets, Output &output) {
        float width_ratio = (float) this->effect_roi.ori_width / (float) this->effect_roi.width;
        float height_ratio = (float) this->effect_roi.ori_height / (float) this->effect_roi.height;
        for (BoxInfo &obj: dets) {
            float x1 = (obj.x1 - this->effect_roi.x) * width_ratio;
            float x2 = (obj.x2 - this->effect_roi.x) * width_ratio;
            float y1 = (obj.y1 - this->effect_roi.y) * height_ratio;
            float y2 = (obj.y2 - this->effect_roi.y) * height_ratio;
            std::vector<std::vector<float> > coords{
                    {x1, y1},
                    {x2, y1},
                    {x2, y2},
                    {x1, y2}
            };
            Output::Card card;
            card.coords = coords;
            card.confidence = obj.score;
            card.class_name = labels[obj.label];
            output.cards.push_back(card);
        }
    }

//    void NanoDet::detect(std::string &imgPath, Output &output) {
//        cv::Mat bgr = cv::imread(imgPath, 1);
//        ncnn::Mat model_input;
//        std::vector<Object> objects;
//
//        preprocess(bgr, model_input);
//        process(bgr, objects);
//        postprocess(bgr, objects, output);
//        // output param: output
//    }


    void NanoDet::detect(const cv::Mat &image, Output &output) {
        ncnn::Mat input;

        preprocess(image, input);

        auto ex = this->Net->create_extractor();
        ex.input("data", input);

        std::vector<std::vector<BoxInfo>> results;
        results.resize(this->num_class);

        ncnn::Mat out;
        ex.extract("output", out);

        // generate center priors in format of (x, y, stride)
        std::vector<CenterPrior> center_priors;
        generate_grid_center_priors(this->input_size[0], this->input_size[1], this->strides,
                                    center_priors);

        this->decode_infer(out, center_priors, score_threshold, results);

        std::vector<BoxInfo> dets;
        for (int i = 0; i < (int) results.size(); i++) {
            this->nms(results[i], nms_threshold);

            for (auto box: results[i]) {
                dets.push_back(box);
            }
        }

        postprocess(dets, output);
    }

    void NanoDet::decode_infer(ncnn::Mat &feats, std::vector<CenterPrior> &center_priors,
                               float threshold, std::vector<std::vector<BoxInfo>> &results) {
        const int num_points = center_priors.size();
        for (int idx = 0; idx < num_points; idx++) {
            const int ct_x = center_priors[idx].x;
            const int ct_y = center_priors[idx].y;
            const int stride = center_priors[idx].stride;

            const float *scores = feats.row(idx);
            float score = 0;
            int cur_label = 0;
            for (int label = 0; label < this->num_class; label++) {
                if (scores[label] > score) {
                    score = scores[label];
                    cur_label = label;
                }
            }
            if (score > threshold) {
                const float *bbox_pred = feats.row(idx) + this->num_class;
                results[cur_label].push_back(
                        this->disPred2Bbox(bbox_pred, cur_label, score, ct_x, ct_y, stride));
            }
        }
    }

    BoxInfo
    NanoDet::disPred2Bbox(const float *&dfl_det, int label, float score, int x, int y, int stride) {
        float ct_x = x * stride;
        float ct_y = y * stride;
        std::vector<float> dis_pred;
        dis_pred.resize(4);
        for (int i = 0; i < 4; i++) {
            float dis = 0;
            float *dis_after_sm = new float[this->reg_max + 1];
            activation_function_softmax(dfl_det + i * (this->reg_max + 1), dis_after_sm,
                                        this->reg_max + 1);
            for (int j = 0; j < this->reg_max + 1; j++) {
                dis += j * dis_after_sm[j];
            }
            dis *= stride;
            dis_pred[i] = dis;
            delete[] dis_after_sm;
        }
        float xmin = (std::max)(ct_x - dis_pred[0], .0f);
        float ymin = (std::max)(ct_y - dis_pred[1], .0f);
        float xmax = (std::min)(ct_x + dis_pred[2], (float) this->input_size[0]);
        float ymax = (std::min)(ct_y + dis_pred[3], (float) this->input_size[1]);

        return BoxInfo{xmin, ymin, xmax, ymax, score, label};
    }

    void NanoDet::nms(std::vector<BoxInfo> &input_boxes, float NMS_THRESH) {
        std::sort(input_boxes.begin(), input_boxes.end(),
                  [](const BoxInfo &a, const BoxInfo &b) { return a.score > b.score; });
        std::vector<float> vArea(input_boxes.size());
        for (int i = 0; i < int(input_boxes.size()); ++i) {
            vArea[i] = (input_boxes.at(i).x2 - input_boxes.at(i).x1 + 1)
                       * (input_boxes.at(i).y2 - input_boxes.at(i).y1 + 1);
        }

        for (int i = 0; i < int(input_boxes.size()); ++i) {
            for (int j = i + 1; j < int(input_boxes.size());) {
                float xx1 = (std::max)(input_boxes[i].x1, input_boxes[j].x1);
                float yy1 = (std::max)(input_boxes[i].y1, input_boxes[j].y1);
                float xx2 = (std::min)(input_boxes[i].x2, input_boxes[j].x2);
                float yy2 = (std::min)(input_boxes[i].y2, input_boxes[j].y2);
                float w = (std::max)(float(0), xx2 - xx1 + 1);
                float h = (std::max)(float(0), yy2 - yy1 + 1);
                float inter = w * h;
                float ovr = inter / (vArea[i] + vArea[j] - inter);
                if (ovr >= NMS_THRESH) {
                    input_boxes.erase(input_boxes.begin() + j);
                    vArea.erase(vArea.begin() + j);
                } else {
                    j++;
                }
            }
        }
    }

};
