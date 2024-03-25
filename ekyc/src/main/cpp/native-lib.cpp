#include <jni.h>
#include <string>
#include "src/face.h"
#include "ncnn-20220701-android/arm64-v8a/include/ncnn/mat.h"
#include <android/asset_manager_jni.h>
#include "benchmark.h"
#include "src/nanodet.hpp"

#define RGBA_A(p) (((p) & 0xFF000000) >> 24)
#define RGBA_R(p) (((p) & 0x00FF0000) >> 16)
#define RGBA_G(p) (((p) & 0x0000FF00) >>  8)
#define RGBA_B(p)  ((p) & 0x000000FF)
#define MAKE_RGBA(r, g, b, a) (((a) << 24) | ((r) << 16) | ((g) << 8) | (b))

using namespace cv;

std::string convertJString(JNIEnv *env, jstring str) {
    if (!str) std::string();

    const jsize len = env->GetStringUTFLength(str);
    const char *strChars = env->GetStringUTFChars(str, (jboolean *) 0);

    std::string Result(strChars, len);

    env->ReleaseStringUTFChars(str, strChars);

    return Result;
}

ncnn::Net net;
Mat landm_mean, landm_std;
ncnn::Net landmark_net;
ekyc_card_detection::NanoDet cardDetector;

extern "C"
JNIEXPORT void JNICALL
Java_com_fast_ekyc_native_NativeFunctionCall_init(JNIEnv *env, jobject thiz, jobject mgr,
                                                  jstring config_path) {
    AAssetManager *aMgr = AAssetManager_fromJava(env, mgr);
    net.load_param(aMgr, "weight_data.param");
    net.load_model(aMgr, "weight_data.bin");
    std::string srtConfigPath = convertJString(env, config_path);
    FileStorage fs(srtConfigPath, FileStorage::READ);
    fs["mean"] >> landm_mean;
    fs["std"] >> landm_std;
    landmark_net.load_param(aMgr, "face.param");
    landmark_net.load_model(aMgr, "face.bin");

    cardDetector.load_model(aMgr);
}



int bitmapToMat(JNIEnv *env, jobject srcBitMap, cv::Mat &resultMat) {
    // Lock the bitmap to get the buffer
    void *pixels = NULL;
    int imageWidth, imageHeight;
    AndroidBitmapInfo bmpInfo;
    memset(&bmpInfo, 0, sizeof(bmpInfo));
    AndroidBitmap_getInfo(env, srcBitMap, &bmpInfo);
    // Check format, only RGB565 & RGBA are supported
    if (bmpInfo.width <= 0 || bmpInfo.height <= 0 ||
        bmpInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return -1;
    }
    int nRes = AndroidBitmap_lockPixels(env, srcBitMap, &pixels);
    if (pixels == NULL) {
        return -1;
    }
    imageWidth = bmpInfo.width;
    imageHeight = bmpInfo.height;
//    cv::Mat resultMat;
    resultMat.create(imageHeight, imageWidth, CV_8UC3);

    for (int y = 0; y < imageHeight; y++) {
        uint32_t *bitmapLine = ((uint32_t *) pixels) + y * imageWidth;
        cv::Vec3b *pMatLine3 = resultMat.ptr<cv::Vec3b>(y);
        for (int x = 0; x < imageWidth; x++) {
            uint32_t v = *(bitmapLine + x);
            //int aValue = RGBA_A(v);
            // eprintf("print lock A: %d\n", aValue);

            //if(aValue != 255)
            // eprintf("A: %d at (%d, %d) \n", aValue, x, y);

            cv::Vec3b &curPixel = pMatLine3[x];
            curPixel[2] = RGBA_B(v);
            curPixel[1] = RGBA_G(v);
            curPixel[0] = RGBA_R(v);
            //curPixel[3] = RGBA_A(v);
        }
    }

    AndroidBitmap_unlockPixels(env, srcBitMap);
    return 0;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_fast_ekyc_native_NativeFunctionCall_detectPose(JNIEnv *env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    cv::Mat frame;

    bitmapToMat(env, bitmap, frame);

    std::vector<Object> originalFaces = detectFace(frame, net);
    auto result = detectPose(frame, originalFaces, landmark_net, landm_mean, landm_std);

    if (result.size() != 3) {
        jfloatArray output = env->NewFloatArray(result.size());
        env->SetFloatArrayRegion(output, 0, result.size(), &result[0]);

        return output;
    }

    std::vector<float> poseResult = {result[0], result[1], result[2], originalFaces[0].rect.x,
                                     originalFaces[0].rect.y,
                                     originalFaces[0].rect.width,
                                     originalFaces[0].rect.height, originalFaces[0].prob};
    jfloatArray output = env->NewFloatArray(poseResult.size());
    env->SetFloatArrayRegion(output, 0, poseResult.size(), &poseResult[0]);

    return output;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_fast_ekyc_native_NativeFunctionCall_detectCard(JNIEnv *env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    cv::Mat frame;
    bitmapToMat(env, bitmap, frame);


    ekyc_card_detection::Output out;
    cardDetector.detect(frame, out);

    auto cardClass = env->FindClass("com/fast/ekyc/native/model/CardObject");
    auto cid = env->GetMethodID(cardClass, "<init>", "(FFFFFLjava/lang/String;)V");
    jobjectArray ret = env->NewObjectArray(out.cards.size(), cardClass, nullptr);

    int i = 0;
    for (auto &card: out.cards) {
        env->PushLocalFrame(1);
        float confidence = card.confidence;
        float left = card.coords[0][0];
        float top = card.coords[0][1];
        float right = card.coords[2][0];
        float bottom = card.coords[2][1];


        jobject obj = env->NewObject(cardClass, cid, left, top, right,
                                     bottom, confidence,
                                     env->NewStringUTF(card.class_name.c_str()));
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }

    return ret;
}