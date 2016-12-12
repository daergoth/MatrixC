package com.fordprog.matrix.interpreter;


import org.antlr.v4.runtime.ParserRuleContext;

public class CodePoint {

  private final int lineNumber;

  private final int columnNumber;

  public CodePoint(int lineNumber, int columnNumber) {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }

  public static CodePoint from(ParserRuleContext ctx) {
    return new CodePoint(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  @Override
  public String toString() {
    return "line: " + lineNumber +
        ", column: " + columnNumber;
  }
}
