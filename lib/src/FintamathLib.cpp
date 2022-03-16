#include "fintamath/solver/Solver.hpp"

#include <jni.h>
#include <string>
#include "fintamath/solver/Solver.hpp"
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_fintamath_1android_MainActivity_findSolution(
        JNIEnv* env,
        jobject,jstring str) {
  std::string solution_str = env->GetStringUTFChars(str, nullptr);
  Solver solver;
  ArithmeticExpression arithmeticExpression(solution_str);
  std::string s=solver.solve(arithmeticExpression).toString();
  return env->NewStringUTF(s.c_str());
}