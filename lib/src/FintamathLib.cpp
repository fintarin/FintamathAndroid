#include "fintamath/expressions/Expression.hpp"


#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring Java_com_fintarin_fintamath_1android_MainActivity_findSolution(JNIEnv *env,
                                                                                                    jobject,
                                                                                                    jstring str) {
    std::string input = env->GetStringUTFChars(str, nullptr);
    try {
        fintamath::Expression e=fintamath::Expression(input);
        e.simplify();
        std::string output = e.toString();
        return env->NewStringUTF(output.c_str());
    } catch (std::exception &exception) {
        return env->NewStringUTF("Unable to calculate");
    }
}