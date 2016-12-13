package com.fordprog.matrix.interpreter.execution;


import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.UserDefinedFunction;

public interface FunctionVisitor {

  void visit(UserDefinedFunction userDefinedFunction);

  void visit(BuiltinFunction builtinFunction);

}
