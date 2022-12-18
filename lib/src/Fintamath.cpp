#include "fintamath/expressions/Expression.hpp"
#include "fintamath/exceptions/Exception.hpp"
#include "fintamath/exceptions/UndefinedException.hpp"

#include <jni.h>
#include <string>

using namespace fintamath;

extern "C" JNIEXPORT jstring Java_com_fintamath_calculator_Calculator_calculate(JNIEnv *env, jobject, jstring inJStr) {
    std::string inStr = env->GetStringUTFChars(inJStr, nullptr);
    try {
        auto outExpr = Expression(inStr);
        return env->NewStringUTF((Expression(*outExpr.simplify(false)).toString(24) + "\n" + outExpr.toString()).c_str());
    } catch (const UndefinedException &exc) {
        return env->NewStringUTF("Undefined");
    } catch (const Exception &exc) {
        return env->NewStringUTF("Invalid input");
    }
}
