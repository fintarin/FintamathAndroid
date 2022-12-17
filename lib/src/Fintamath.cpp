#include "fintamath/expressions/Expression.hpp"
#include "fintamath/exceptions/Exception.hpp"

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring Java_com_fintamath_Calculator_calculate(JNIEnv *env, jobject, jstring inputJStr) {
  std::string inStr = env->GetStringUTFChars(inputJStr, nullptr);

  try {
    auto outExpr = fintamath::Expression(inStr).simplify(false);
    return env->NewStringUTF(outExpr->toString().c_str());
  } catch (const fintamath::Exception &exc) {
    return env->NewStringUTF(exc.what());
  }
}
