package com.fordprog.matrix.interpreter;


import static com.fordprog.matrix.MatrixParser.ProgramContext;

import com.fordprog.matrix.MatrixLexer;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.SemanticError;
import com.fordprog.matrix.interpreter.execution.CodeExecutor;
import com.fordprog.matrix.interpreter.execution.stdlib.BuiltinDeclarationSource;
import com.fordprog.matrix.interpreter.execution.stdlib.InputOutputDeclarationSource;
import com.fordprog.matrix.interpreter.scope.Symbol;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {

  private final MatrixLexer matrixLexer;

  private final MatrixParser matrixParser;

  private final SemanticListener semanticListener;

  public Interpreter(String code) {
    matrixLexer = new MatrixLexer(new ANTLRInputStream(code));

    matrixParser = new MatrixParser(new CommonTokenStream(matrixLexer));

    semanticListener = new SemanticListener(createBuiltinSymbolDeclarations());
  }

  public void interpret() {

    ProgramContext parseTree = matrixParser.program();

    ParseTreeWalker.DEFAULT.walk(semanticListener, parseTree);

    List<SemanticError> errorList = semanticListener.getSemanticErrors();

    if (!errorList.isEmpty()) {
      System.out.println("------------------- ERRORS ---------------------");
      errorList.forEach(System.out::println);
    } else {
      CodeExecutor codeExecutor = new CodeExecutor(semanticListener.getSymbolTable(), parseTree);

      codeExecutor.execute();
    }

  }

  private List<Symbol> createBuiltinSymbolDeclarations() {
    List<BuiltinDeclarationSource> sources = Arrays.asList(
        new InputOutputDeclarationSource()
    );

    return sources.stream()
        .map(BuiltinDeclarationSource::getDeclarations)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
