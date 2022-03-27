#include "fintamath/solver/Solver.hpp"
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL Java_com_fintarin_fintamath_1android_MainActivity_findSolution(JNIEnv *env,
                                                                                                    jobject,
                                                                                                    jstring str) {
  std::string solution_str = env->GetStringUTFChars(str, nullptr);
  Solver solver;
  try {
      ArithmeticExpression arithmeticExpression(solution_str);
      std::string s = solver.solve(arithmeticExpression).toString();
      return env->NewStringUTF(s.c_str());
  }
  catch (std::exception& exception) {
      return env->NewStringUTF("Unable to calculate");
  }
}
