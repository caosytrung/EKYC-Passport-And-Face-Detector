#ifndef FD_TOOLS
#define FD_TOOLS

std::vector<int> expand_box(int width, int height, std::vector<int> box);

float l2norm(cv::Mat x);

cv::Mat concatenate(cv::Mat x, cv::Mat y, cv::Mat z);

std::vector<float> matrix2angle(cv::Mat R);

#endif // FD_TOOLS
