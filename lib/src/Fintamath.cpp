#include "fintamath/exceptions/UndefinedException.hpp"
#include "fintamath/expressions/Expression.hpp"
#include "fintamath/expressions/ExpressionFunctions.hpp"

#include <android/log.h>
#include <jni.h>
#include <string>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>

using namespace fintamath;

static const char *loggerTag = "com.fintamath.lib";

constexpr int32_t maxSolutionLength = 1000000;

const char *charLimitExc = "Character limit exceeded";
const char *invalidInputExc = "Invalid input";
const char *failedToSolveExc = "Failed to solve";

static pid_t calcPid = -1;

static auto *solutionStrShared =
    (char *)mmap(nullptr, maxSolutionLength, PROT_READ | PROT_WRITE, MAP_SHARED | MAP_ANONYMOUS, -1, 0);

std::string makeOutResult(const std::string &res) {
  return res + "\n";
}

std::string calculate(const std::string &inStr) {
  try {
    ArgumentPtr inExpr = parseExpr(inStr);
    Expression simplExpr(inExpr);
    Expression solExpr = solve(simplExpr);
    Expression solPrecise10Expr = solExpr.precise(10);

    std::string solutions = makeOutResult(inExpr->toString()) + //
                            makeOutResult(solExpr.toString()) + //
                            makeOutResult(solPrecise10Expr.toString());

    if (solutions.empty() || solutions.size() >= maxSolutionLength) {
      return charLimitExc;
    }

    solutions.pop_back();

    return solutions;
  }
  catch (const InvalidInputException &exc) {
    return invalidInputExc;
  }
}

void stopCurrentCalculations() {
  if (calcPid != -1) {
    kill(calcPid, SIGKILL);
    calcPid = -1;
  }
}

extern "C" JNIEXPORT void Java_com_fintamath_calculator_Calculator_calculate(JNIEnv *env, jobject instance,
                                                                             jstring inJStr) {
  stopCurrentCalculations();

  std::string inStr = env->GetStringUTFChars(inJStr, nullptr);

  pid_t pid = fork();

  if (pid == 0) {
    std::string solutionStr = calculate(inStr);
    size_t solutionStrLen = solutionStr.length();

    solutionStr.copy(solutionStrShared, solutionStrLen);
    solutionStrShared[solutionStrLen] = '\0';

    _exit(0);
  }
  else if (pid != -1) {
    calcPid = pid;

    __android_log_print(ANDROID_LOG_DEBUG, loggerTag, "Calculations started. pid = %d", pid);

    int status;
    waitpid(pid, &status, 0);

    if (pid == calcPid) {
      jclass clazz = env->GetObjectClass(instance);
      jmethodID callbackId = env->GetMethodID(clazz, "onCalculated", "(Ljava/lang/String;)V");

      if (WIFEXITED(status)) {
        env->CallVoidMethod(instance, callbackId, env->NewStringUTF(solutionStrShared));
      }
      else {
        env->CallVoidMethod(instance, callbackId, env->NewStringUTF(failedToSolveExc));
      }

      env->DeleteLocalRef(clazz);

      __android_log_print(ANDROID_LOG_DEBUG, loggerTag, "Calculations ended. pid = %d", pid);
    }
    else {
      jclass clazz = env->GetObjectClass(instance);
      jmethodID callbackId = env->GetMethodID(clazz, "onInterrupted", "()V");

      env->CallVoidMethod(instance, callbackId);
      env->DeleteLocalRef(clazz);

      __android_log_print(ANDROID_LOG_DEBUG, loggerTag, "Calculations stopped. pid = %d", pid);
    }
  }
}

extern "C" JNIEXPORT void JNICALL Java_com_fintamath_calculator_Calculator_stopCurrentCalculations(JNIEnv *env,
                                                                                                   jobject instance) {
  stopCurrentCalculations();
}