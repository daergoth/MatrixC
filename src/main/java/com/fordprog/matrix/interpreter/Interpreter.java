package com.fordprog.matrix.interpreter;


import static com.fordprog.matrix.MatrixParser.ProgramContext;

import com.fordprog.matrix.MatrixLexer;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.RuntimeError;
import com.fordprog.matrix.interpreter.error.semantic.SemanticError;
import com.fordprog.matrix.interpreter.execution.CodeExecutor;
import com.fordprog.matrix.interpreter.execution.stdlib.BuiltinDeclarationSource;
import com.fordprog.matrix.interpreter.execution.stdlib.InputOutputDeclarationSource;
import com.fordprog.matrix.interpreter.execution.stdlib.SpecialMatrixOperationDeclarationSource;
import com.fordprog.matrix.interpreter.semantic.Symbol;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.tool.GrammarParserInterpreter;

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
    matrixParser.setErrorHandler(new GrammarParserInterpreter.BailButConsumeErrorStrategy());

    semanticListener = new SemanticListener(createBuiltinSymbolDeclarations());
  }

  public void interpret() {

    ProgramContext parseTree = matrixParser.program();

    ParseTreeWalker.DEFAULT.walk(semanticListener, parseTree);

    List<SemanticError> errorList = semanticListener.getSemanticErrors();

    if (matrixParser.getNumberOfSyntaxErrors() > 0) {
      System.err.println("Aborting...");
    } else if (!errorList.isEmpty()) {
      System.err.println("------------------- ERRORS ---------------------");
      errorList.forEach(System.err::println);
    } else {
      CodeExecutor codeExecutor = new CodeExecutor(semanticListener.getSymbolTable(), parseTree);

      try {
        codeExecutor.execute();
      } catch (RuntimeError e) {
        System.err.println("[" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        System.err.println("Aborting...");
      }
    }

  }

  private List<Symbol> createBuiltinSymbolDeclarations() {
    List<BuiltinDeclarationSource> sources = Arrays.asList(
        new InputOutputDeclarationSource(),
        new SpecialMatrixOperationDeclarationSource()
    );

    return sources.stream()
        .map(BuiltinDeclarationSource::getDeclarations)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
