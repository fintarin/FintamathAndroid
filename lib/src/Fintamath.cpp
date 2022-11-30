#include "fintamath/expressions/Expression.hpp"

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring Java_com_fintamath_Calculator_simplify(JNIEnv *env, jobject, jstring inputJStr) {
  std::string inStr = env->GetStringUTFChars(inputJStr, nullptr);

  try {
    auto outExpr = fintamath::Expression(inStr);
    return env->NewStringUTF(outExpr.toString().c_str());
  } catch (const fintamath::Exception &exc) {
    return env->NewStringUTF(exc.what());
  }
}

extern "C" JNIEXPORT jstring Java_com_fintamath_Calculator_solve(JNIEnv *env, jobject, jstring inputJStr) {
  std::string inStr = env->GetStringUTFChars(inputJStr, nullptr);

  try {
    auto outExpr = fintamath::Expression(inStr).simplify();
    return env->NewStringUTF(outExpr->toString().c_str());
  } catch (const fintamath::Exception &exc) {
    return env->NewStringUTF(exc.what());
  }
}
