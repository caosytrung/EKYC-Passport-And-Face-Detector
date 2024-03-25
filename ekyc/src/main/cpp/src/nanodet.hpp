//
// Adapt from https://github.com/RangiLyu/nanodet/tree/main/demo_ncnn
//

#ifndef NANODET_HPP_
#define NANODET_HPP_

#include <opencv2/core/core.hpp>
#include <net.h>
#include "dto.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

namespace ekyc_card_detection {

    struct HeadInfo {
        std::string cls_layer;
        std::string dis_layer;
        int stride;
    };

    struct CenterPrior {
        int x;
        int y;
        int stride;
    };

    struct BoxInfo {
        float x1;
        float y1;
        float x2;
        float y2;
        float score;
        int label;
    };

    struct object_rect {
        int x;
        int y;
        int width;
        int height;
        int ori_width;
        int ori_height;
    };

    class NanoDet {
    public:
        NanoDet();

        ~NanoDet();

        LoadState load_model(AAssetManager *mrg);

        void detect(const cv::Mat &image, Output &output);

        static NanoDet *detector;
        ncnn::Net *Net;
        static bool hasGPU;
        // modify these parameters to the same with your config if you want to use your own model
        int input_size[2] = {416, 416}; // input height and width
        int num_class = 15; // number of classes. 80 for COCO
        int reg_max = 7; // `reg_max` set in the training config. Default: 7.
        float score_threshold = 0.4;
        float nms_threshold = 0.2;
        std::vector<int> strides = {8, 16, 32, 64}; // strides of the multi-level feature.
        const float mean_vals[3] = {103.53f, 116.28f, 123.675f};
        const float norm_vals[3] = {0.017429f, 0.017507f, 0.017125f};
        std::vector<std::string> labels{
                "CCCD",
                "CMQD",
                "CMND_BACK",
                "CCCD_BACK",
                "CMND",
                "PASSPORT",
                "CMQD_BACK",
                "CCCD_front_chip",
                "PASSPORT_OTHER",
                "BLX",
                "CCCD_back_chip",
                "BLX_BACK",
                "CMCC",
                "BLX_OLD",
                "BLX_BACK_OLD",
        };
    private:
        void preprocess(const cv::Mat &image, ncnn::Mat &in);

        void
        decode_infer(ncnn::Mat &feats, std::vector<CenterPrior> &center_priors, float threshold,
                     std::vector<std::vector<BoxInfo>> &results);

        void postprocess(std::vector<BoxInfo> &dets, Output &output);

        BoxInfo
        disPred2Bbox(const float *&dfl_det, int label, float score, int x, int y, int stride);

        void nms(std::vector<BoxInfo> &result, float nms_threshold);

        object_rect effect_roi;

    };

};

#endif //NANODET_HPP_
