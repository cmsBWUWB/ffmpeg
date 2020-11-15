#include <jni.h>
#include <string>
#include <android/log.h>

extern "C"{
    #include <libavcodec/avcodec.h>
    #include <libavformat/avformat.h>
    #include <libswscale/swscale.h>
}

#define TAG "native-lib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG ,__VA_ARGS__)


extern "C" JNIEXPORT jstring JNICALL
testFfmpeg(
        JNIEnv* env,
        jobject obj, jstring a, jstring b) {
    const char* version = av_version_info();

    return env->NewStringUTF(version);
}


static JNINativeMethod gMethods[] = {
        {"testFfmpeg", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void*)testFfmpeg},
};

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    char className[40] = {"com/cms/testvod/MainActivity"};
    jclass clazz = (env)->FindClass( (const char*)className);
    if((env)->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))< 0) {
        return -1;
    }
    result = JNI_VERSION_1_4;
    return result;

}
