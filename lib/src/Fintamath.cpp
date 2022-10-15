#include "fintamath/expressions/Expression.hpp"

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring Java_com_fintarin_fintamath_1android_MainActivity_findSolution(JNIEnv *env, jobject,
                                                                                            jstring inputJStr) {

  std::string inputStr = env->GetStringUTFChars(inputJStr, nullptr);

  try {
    fintamath::Expression e = fintamath::Expression(inputStr);
    return env->NewStringUTF(e.simplify()->toString());

  } catch (const std::invalid_argument &exc) {
    return env->NewStringUTF(exc.what());
  } catch (const std::domain_error &exc) {
    return env->NewStringUTF(exc.what());
  }
}
