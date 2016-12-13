package com.fordprog.matrix.interpreter;


import static com.fordprog.matrix.MatrixParser.ProgramContext;

import com.fordprog.matrix.MatrixLexer;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.SemanticError;
import com.fordprog.matrix.interpreter.execution.CodeExecutor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class Interpreter {

  private final MatrixLexer matrixLexer;

  private final MatrixParser matrixParser;

  private final SemanticListener semanticListener;

  public Interpreter(String code) {
    matrixLexer = new MatrixLexer(new ANTLRInputStream(code));

    matrixParser = new MatrixParser(new CommonTokenStream(matrixLexer));

    semanticListener = new SemanticListener();

  }

  public void interpret() {

    ProgramContext parseTree = matrixParser.program();

    ParseTreeWalker.DEFAULT.walk(semanticListener, parseTree);

    List<SemanticError> errorList = semanticListener.getSemanticErrors();

    if (!errorList.isEmpty()) {
      System.out.println("------------------- ERRORS ---------------------");
      errorList.forEach(System.out::println);
    } else {
      System.out.println("Success!");

      CodeExecutor codeExecutor = new CodeExecutor(semanticListener.getSymbolTable(), parseTree);

      codeExecutor.execute();
    }

  }
}
