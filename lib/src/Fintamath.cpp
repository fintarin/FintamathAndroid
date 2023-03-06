#include "fintamath/exceptions/Exception.hpp"
#include "fintamath/exceptions/UndefinedException.hpp"
#include "fintamath/expressions/Expression.hpp"

#include <jni.h>
#include <string>

using namespace fintamath;

std::string makeOutResult(const std::string &res) {
  constexpr int32_t maxResultLength = 10000;
  return res.length() < maxResultLength ? res + "\n" : "";
}

extern "C" JNIEXPORT jstring Java_com_fintamath_calculator_Calculator_calculate(JNIEnv *env, jobject, jstring inJStr) {
  std::string inStr = env->GetStringUTFChars(inJStr, nullptr);
  try {
    Expression resExpr(inStr);
    std::string res = makeOutResult(resExpr.toString());
    std::string resPrecise10 = makeOutResult(resExpr.solve(10));
    std::string resPrecise50 = makeOutResult(resExpr.solve(50));

    std::string resArray = res + resPrecise10 + resPrecise50;
    resArray.pop_back();

    return env->NewStringUTF(resArray.c_str());
  } catch (const UndefinedException &exc) {
    return env->NewStringUTF("Undefined");
  } catch (const Exception &exc) {
    return env->NewStringUTF("Invalid input");
  }
}
