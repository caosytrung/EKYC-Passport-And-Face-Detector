#include <opencv2/core/core.hpp>
#include <net.h>

using namespace cv;

struct Object
{
    cv::Rect_<float> rect;
    int label;
    float prob;
    std::vector<cv::Point2f> pts;
};

std::vector<Object> detectFace(cv::Mat &frame, const ncnn::Net &net);

std::vector<float> detectPose(cv::Mat &image,
                              std::vector<Object> &faces,
                              ncnn::Net &landmark_net,
                              const Mat &landm_mean,
                              const Mat &landm_std);

