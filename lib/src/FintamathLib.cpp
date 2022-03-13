#include "fintamath/solver/Solver.hpp"

void f() {
  Solver solver;
  ArithmeticExpression e("1+1");
  solver.solve(e);
}