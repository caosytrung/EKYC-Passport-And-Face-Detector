#include <vector>
#include <opencv2/opencv.hpp>
#include "tools.h"
#include "mat.h"

std::vector<float> matrix2angle(cv::Mat R)
{
    // R (3, 3): rotation matrix
    float x, y, z;
    if (R.at<float>(2, 0) > 0.998)
    {
        z = 0.0;
        x = M_PI_2;
        y = z + atan2(-R.at<float>(0, 1), -R.at<float>(0, 2));
    }
    else if (R.at<float>(2, 0) < -0.998)
    {
        z = 0.0;
        x = -M_PI_2;
        y = -z + atan2(R.at<float>(0, 1), R.at<float>(0, 2));
    }
    else
    {
        x = asin(R.at<float>(2, 0));
        y = atan2(R.at<float>(2, 1)/cos(x), R.at<float>(2, 2)/cos(x));
        z = atan2(R.at<float>(1, 0)/cos(x), R.at<float>(0, 0)/cos(x));
    }
    std::vector<float> output = {x, y, z};

    return output;
}

cv::Mat concatenate(cv::Mat x, cv::Mat y, cv::Mat z)
{
    // Concat vertically 3 cv::Mat into a cv::Mat
    cv::Mat output;
    cv::vconcat(x, y, output);
    cv::vconcat(output, z, output);

    return output;
}

float l2norm(cv::Mat x)
{
    float output = 0.0;
    output = cv::sum(x.mul(x))[0];
    output = sqrt(output);

    return output;
}

std::vector<int> expand_box(int width, int height, std::vector<int> box)
{
    int box_wid = box[2] - box[0];
    int box_hei = box[3] - box[1];
    float cx = box[0] + box_wid / 2;
    float cy = box[1] + box_hei / 2;
    float size = 1.58 * 0.5 * (box_wid + box_hei);

    std::vector<int> out_box(4);
    out_box[0] = cx - size / 2;
    out_box[1] = cy - size / 2;
    out_box[2] = out_box[0] + size;
    out_box[3] = out_box[1] + size;

    return out_box;
}
