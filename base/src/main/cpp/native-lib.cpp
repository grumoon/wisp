#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_grumoon_wisp_base_utils_TestUtils_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
