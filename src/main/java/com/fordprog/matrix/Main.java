package com.fordprog.matrix;

import com.fordprog.matrix.interpreter.SemanticListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {

    final MatrixLexer matrixLexer = new MatrixLexer(new ANTLRFileStream(args[0]));

    final MatrixParser matrixParser = new MatrixParser(new CommonTokenStream(matrixLexer));

    ParseTree parseTree = matrixParser.program();

    ParseTreeWalker.DEFAULT.walk(new SemanticListener(), parseTree);

  }

}
