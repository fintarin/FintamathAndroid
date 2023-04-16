#include "fintamath/exceptions/UndefinedException.hpp"
#include "fintamath/expressions/Expression.hpp"
#include "fintamath/expressions/ExpressionFunctions.hpp"

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
    Expression inExpr(inStr);
    Expression solExpr = solve(inExpr);
    Expression solPrecise10Expr = solExpr.precise(10);

    std::string resArray = makeOutResult(solExpr.toString()) +          //
                           makeOutResult(solPrecise10Expr.toString()) + //
                           makeOutResult(inExpr.toString());            //

    resArray.pop_back();

    return env->NewStringUTF(resArray.c_str());
  } catch (const UndefinedException &exc) {
    return env->NewStringUTF("Undefined");
  } catch (const Exception &exc) {
    return env->NewStringUTF("Invalid input");
  }
}
