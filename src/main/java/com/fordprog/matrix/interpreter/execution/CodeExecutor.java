package com.fordprog.matrix.interpreter.execution;


import static com.fordprog.matrix.MatrixParser.AssignStatementContext;
import static com.fordprog.matrix.MatrixParser.BinOperatorExpressionContext;
import static com.fordprog.matrix.MatrixParser.BlockContext;
import static com.fordprog.matrix.MatrixParser.BlockStatementContext;
import static com.fordprog.matrix.MatrixParser.ControllBlockContext;
import static com.fordprog.matrix.MatrixParser.ControllBlockStatementContext;
import static com.fordprog.matrix.MatrixParser.DeclareStatementContext;
import static com.fordprog.matrix.MatrixParser.ExprContext;
import static com.fordprog.matrix.MatrixParser.ExprLogicExpressionContext;
import static com.fordprog.matrix.MatrixParser.ForStatementContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallExpressionContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallParameterListContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallStatementContext;
import static com.fordprog.matrix.MatrixParser.FunctionDeclContext;
import static com.fordprog.matrix.MatrixParser.IdContext;
import static com.fordprog.matrix.MatrixParser.IdTermContext;
import static com.fordprog.matrix.MatrixParser.IfElseStatementContext;
import static com.fordprog.matrix.MatrixParser.IfStatementContext;
import static com.fordprog.matrix.MatrixParser.LogicExprContext;
import static com.fordprog.matrix.MatrixParser.MatrixContext;
import static com.fordprog.matrix.MatrixParser.MatrixTermContext;
import static com.fordprog.matrix.MatrixParser.ParenLogicExprContext;
import static com.fordprog.matrix.MatrixParser.ParenthesisExpressionContext;
import static com.fordprog.matrix.MatrixParser.ProgramContext;
import static com.fordprog.matrix.MatrixParser.RationalContext;
import static com.fordprog.matrix.MatrixParser.RationalTermContext;
import static com.fordprog.matrix.MatrixParser.RelationLogicExpressionContext;
import static com.fordprog.matrix.MatrixParser.ReturnStatementContext;
import static com.fordprog.matrix.MatrixParser.SimpleStatementContext;
import static com.fordprog.matrix.MatrixParser.SimpleStatementStatementContext;
import static com.fordprog.matrix.MatrixParser.StatementContext;
import static com.fordprog.matrix.MatrixParser.SymbolDeclarationContext;
import static com.fordprog.matrix.MatrixParser.TermContext;
import static com.fordprog.matrix.MatrixParser.TermExpressionContext;
import static com.fordprog.matrix.MatrixParser.VariableDeclarationContext;
import static com.fordprog.matrix.MatrixParser.WhileStatementContext;

import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.error.runtime.CannotConvertRuntimeError;
import com.fordprog.matrix.interpreter.error.runtime.InvalidOperationParameterRuntimeError;
import com.fordprog.matrix.interpreter.execution.stdlib.LogicOperation;
import com.fordprog.matrix.interpreter.execution.stdlib.MatrixOperation;
import com.fordprog.matrix.interpreter.execution.stdlib.RationalOperation;
import com.fordprog.matrix.interpreter.semantic.Scope;
import com.fordprog.matrix.interpreter.semantic.Symbol;
import com.fordprog.matrix.interpreter.semantic.SymbolTable;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Function;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;
import com.fordprog.matrix.interpreter.type.Type;
import com.fordprog.matrix.interpreter.type.UserDefinedFunction;

import java.math.BigInteger;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class CodeExecutor implements FunctionVisitor {

  private final SymbolTable symbolTable;

  private final ProgramContext programContext;

  private final Deque<CallContext> contextStack;

  private final Symbol voidSymbol;

  public CodeExecutor(SymbolTable symbolTable, ProgramContext programContext) {
    this.symbolTable = symbolTable;

    this.programContext = programContext;

    this.contextStack = new LinkedList<>();

    this.voidSymbol = Symbol.builder()
        .identifier("voidSymbol")
        .firstOccurrence(CodePoint.from(programContext))
        .type(Type.VOID)
        .build();
  }

  public void execute() {
    symbolTable.newScope(programContext);

    processGlobalSymbols();

    executeMain();
  }

  private void executeMain() {
    Symbol symbol = symbolTable.getSymbol("main");

    Function function = (Function) symbol.getValue();

    contextStack.addFirst(new CallContext(voidSymbol, symbolTable.getScope(programContext)));

    function.invoke(this);

    contextStack.removeFirst();
  }

  private void processGlobalSymbols() {

    for (SymbolDeclarationContext declarationContext
        : programContext.symbolDeclaration()) {

      if (declarationContext.functionDecl() != null) {
        if (declarationContext.functionDecl().id().getText().equals("main")) {
          break;
        }
      } else {
        processVariableDeclaration(declarationContext.variableDeclaration());
      }
    }
  }

  private void processVariableDeclaration(
      VariableDeclarationContext variableDeclarationContext) {
    Symbol varSymbol = symbolTable.getSymbol(variableDeclarationContext.id().getText());

    if (varSymbol.getType() == Type.RATIONAL) {
      processRationalDeclaration(variableDeclarationContext);
    } else {
      processMatrixDeclaration(variableDeclarationContext);
    }

  }

  private void processRationalDeclaration(
      VariableDeclarationContext variableDeclarationContext) {
    Symbol rationalSymbol = symbolTable.getSymbol(variableDeclarationContext.id().getText());

    executeExpression(variableDeclarationContext.expr(), rationalSymbol);
  }

  private void processMatrixDeclaration(
      VariableDeclarationContext variableDeclarationContext) {
    Symbol matrixSymbol = symbolTable.getSymbol(variableDeclarationContext.id().getText());

    executeExpression(variableDeclarationContext.expr(), matrixSymbol);
  }

  private void executeExpression(ExprContext expr, Symbol targetSymbol) {
    if (expr instanceof TermExpressionContext) {
      executeTermExpression((TermExpressionContext) expr, targetSymbol);
    } else if (expr instanceof FunctionCallExpressionContext) {
      executeFunctionCallExpression(((FunctionCallExpressionContext) expr).functionCall(),
          targetSymbol);
    } else if (expr instanceof BinOperatorExpressionContext) {
      executeBinOperatorExpressionContext((BinOperatorExpressionContext) expr, targetSymbol);
    } else {
      executeParenthesisExpression((ParenthesisExpressionContext) expr, targetSymbol);
    }
  }

  private void executeTermExpression(TermExpressionContext expr, Symbol targetSymbol) {
    TermContext termContext = expr.term();

    if (termContext instanceof IdTermContext) {
      executeIdTerm((IdTermContext) termContext, targetSymbol);
    } else if (termContext instanceof RationalTermContext) {
      executeRationalTerm((RationalTermContext) termContext, targetSymbol);
    } else {
      executeMatrixTerm((MatrixTermContext) termContext, targetSymbol);
    }
  }

  private void executeIdTerm(IdTermContext termContext, Symbol targetSymbol) {
    executeId(termContext.id(), targetSymbol);
  }

  private void executeId(IdContext idContext, Symbol targetSymbol) {
    Symbol sourceTerm = symbolTable.getSymbol(idContext.getText());

    targetSymbol.setValue(sourceTerm.getValue(), sourceTerm.getType());
  }

  private void executeRationalTerm(RationalTermContext termContext, Symbol targetSymbol) {
    executeRational(termContext.rational(), targetSymbol);
  }

  private void executeRational(RationalContext rationalContext, Symbol targetSymbol) {

    targetSymbol.setValue(Rational.fromRationalContext(rationalContext), Type.RATIONAL);
  }

  private void executeMatrixTerm(MatrixTermContext termContext, Symbol targetSymbol) {
    executeMatrixContext(termContext.matrix(), targetSymbol);
  }

  private void executeMatrixContext(MatrixContext matrixContext, Symbol targetSymbol) {

    targetSymbol.setValue(Matrix.fromMatrixContext(matrixContext, symbolTable.getCurrentScope()),
        Type.MATRIX);
  }

  private void executeFunctionCallExpression(FunctionCallContext expr,
                                             Symbol targetSymbol) {
    String functionIdentifier = expr.id().getText();

    Symbol functionSymbol = symbolTable.getSymbol(functionIdentifier);

    Function function = (Function) functionSymbol.getValue();

    List<Symbol> parameters = function.getParameterList();

    setFunctionParameters(function, expr.functionCallParameterList());

    contextStack.addFirst(new CallContext(targetSymbol, symbolTable.getCurrentScope()));

    function.invoke(this);

    for (int i = 0; i < parameters.size(); ++i) {
      function.getParameterList().get(i)
          .setValue(parameters.get(i).getValue(), parameters.get(i).getType());
    }

    contextStack.removeFirst();
  }

  private void setFunctionParameters(Function function,
                                     FunctionCallParameterListContext functionCallParameterListContext) {

    List<Symbol> functionParameters = function.getParameterList();

    for (int i = 0; i < functionParameters.size(); ++i) {
      executeExpression(functionCallParameterListContext.expr(i), functionParameters.get(i));
    }

  }

  private void executeBinOperatorExpressionContext(BinOperatorExpressionContext expr,
                                                   Symbol targetSymbol) {

    RationalOperation rationalOperation = RationalOperation.getInstance();
    MatrixOperation matrixOperation = MatrixOperation.getInstance();

    Symbol leftSymbol = Symbol.builder()
        .identifier("leftOperand")
        .type(Type.MATRIX)
        .firstOccurrence(CodePoint.from(expr))
        .build();

    Symbol rightSymbol = Symbol.builder()
        .identifier("rightOperand")
        .type(Type.MATRIX)
        .firstOccurrence(CodePoint.from(expr))
        .build();

    executeExpression(expr.leftOperand, leftSymbol);
    executeExpression(expr.rightOperand, rightSymbol);

    switch (expr.bin_operator().getText()) {
      case "+":
        try {
          Rational left = (Rational) leftSymbol.getValue(Type.RATIONAL);
          Rational right = (Rational) rightSymbol.getValue(Type.RATIONAL);

          targetSymbol.setValue(rationalOperation.add(left, right), Type.RATIONAL);
        } catch (CannotConvertRuntimeError e) {
          Matrix left = (Matrix) leftSymbol.getValue(Type.MATRIX);
          Matrix right = (Matrix) rightSymbol.getValue(Type.MATRIX);

          targetSymbol.setValue(matrixOperation.add(left, right), Type.MATRIX);
        }
        break;
      case "-":
        try {
          Rational left = (Rational) leftSymbol.getValue(Type.RATIONAL);
          Rational right = (Rational) rightSymbol.getValue(Type.RATIONAL);

          targetSymbol.setValue(rationalOperation.subtract(left, right), Type.RATIONAL);
        } catch (CannotConvertRuntimeError e) {
          Matrix left = (Matrix) leftSymbol.getValue(Type.MATRIX);
          Matrix right = (Matrix) rightSymbol.getValue(Type.MATRIX);

          targetSymbol.setValue(matrixOperation.subtract(left, right), Type.MATRIX);
        }
        break;
      case "/": {
        Rational left = (Rational) leftSymbol.getValue(Type.RATIONAL);
        Rational right = (Rational) rightSymbol.getValue(Type.RATIONAL);

        targetSymbol.setValue(rationalOperation.divide(left, right), Type.RATIONAL);
      }
      break;
      case "*": {
        Rational left = null;
        Rational right = null;

        boolean isLeftRational = true;
        boolean isRightRational = true;

        try {
          left = (Rational) leftSymbol.getValue(Type.RATIONAL);
        } catch (CannotConvertRuntimeError e) {
          isLeftRational = false;
        }

        try {
          right = (Rational) rightSymbol.getValue(Type.RATIONAL);
        } catch (CannotConvertRuntimeError e) {
          isRightRational = false;
        }

        if (isLeftRational && isRightRational) {
          targetSymbol.setValue(rationalOperation.multiply(left, right), Type.RATIONAL);
        } else if (!isLeftRational && !isRightRational) {
          throw new InvalidOperationParameterRuntimeError(
              "'*' operator cannot be applied to matrices! Maybe you wanted '#' operator?");
        } else {
          if (isLeftRational) {
            targetSymbol.setValue(matrixOperation.scalarMultiply(
                (Matrix) rightSymbol.getValue(Type.MATRIX), left), Type.MATRIX);
          } else {
            targetSymbol.setValue(matrixOperation.scalarMultiply(
                (Matrix) leftSymbol.getValue(Type.MATRIX), right), Type.MATRIX);
          }
        }
      }
      break;
      case "^": {
        Rational left = (Rational) leftSymbol.getValue(Type.RATIONAL);
        Rational right = (Rational) rightSymbol.getValue(Type.RATIONAL);

        targetSymbol.setValue(rationalOperation.power(left, right), Type.RATIONAL);
      }
      break;
      case "#": {
        Matrix left = (Matrix) leftSymbol.getValue(Type.MATRIX);
        Matrix right = (Matrix) rightSymbol.getValue(Type.MATRIX);

        targetSymbol.setValue(matrixOperation.multiply(left, right), Type.MATRIX);
      }
      break;
    }
  }

  private void executeParenthesisExpression(ParenthesisExpressionContext expr,
                                            Symbol targetSymbol) {
    executeExpression(expr.expr(), targetSymbol);
  }

  @Override
  public void visit(UserDefinedFunction userDefinedFunction) {
    executeFunction(userDefinedFunction.getFunctionDeclContext());
  }

  @Override
  public void visit(BuiltinFunction builtinFunction) {
    builtinFunction.call(contextStack.peekFirst().returnSymbol);
  }


  private void executeFunction(FunctionDeclContext functionDeclContext) {

    symbolTable.newScope(functionDeclContext.block());

    executeBlock(functionDeclContext.block());

    symbolTable.newScope(contextStack.peekFirst().scope.getContext());

  }

  private boolean executeBlock(BlockContext block) {
    symbolTable.newScope(block);

    for (StatementContext statementContext : block.statement()) {
      if (statementContext instanceof SimpleStatementStatementContext) {
        if (executeSimpleStatement(
            ((SimpleStatementStatementContext) statementContext).simpleStatement())) {
          return true;
        }

      } else {
        if (executeControllBlock(
            ((ControllBlockStatementContext) statementContext).controllBlock())) {
          return true;
        }

      }
    }

    symbolTable.exitScope();

    return false;
  }

  private boolean executeSimpleStatement(SimpleStatementContext simpleStatementContext) {
    if (simpleStatementContext instanceof DeclareStatementContext) {
      processVariableDeclaration(
          ((DeclareStatementContext) simpleStatementContext).variableDeclaration());

    } else if (simpleStatementContext instanceof AssignStatementContext) {
      executeAssignStatement((AssignStatementContext) simpleStatementContext);

    } else if (simpleStatementContext instanceof FunctionCallStatementContext) {
      executeFunctionCallExpression(
          ((FunctionCallStatementContext) simpleStatementContext).functionCall(), voidSymbol);

    } else {
      executeReturnStatement((ReturnStatementContext) simpleStatementContext);

      return true;
    }

    return false;
  }

  private void executeAssignStatement(AssignStatementContext assignStatementContext) {
    Symbol assigneSymbol = symbolTable.getSymbol(assignStatementContext.id().getText());

    executeExpression(assignStatementContext.expr(), assigneSymbol);
  }

  private void executeReturnStatement(ReturnStatementContext returnStatementContext) {
    executeExpression(returnStatementContext.expr(), contextStack.peekFirst().returnSymbol);
  }

  private boolean executeControllBlock(ControllBlockContext controllBlockStatementContext) {
    symbolTable.newScope(controllBlockStatementContext);

    if (controllBlockStatementContext instanceof IfStatementContext) {
      executeIfStatement((IfStatementContext) controllBlockStatementContext);

    } else if (controllBlockStatementContext instanceof IfElseStatementContext) {
      executeIfElseStatement((IfElseStatementContext) controllBlockStatementContext);

    } else if (controllBlockStatementContext instanceof WhileStatementContext) {
      executeWhileStatement((WhileStatementContext) controllBlockStatementContext);

    } else if (controllBlockStatementContext instanceof ForStatementContext) {
      executeForStatement((ForStatementContext) controllBlockStatementContext);

    } else {
      executeBlock(((BlockStatementContext) controllBlockStatementContext).block());
    }

    return false;
  }

  private void executeIfStatement(IfStatementContext ifStatementContext) {

    Symbol logicSymbol = executeParenLogicExpression(ifStatementContext.parenLogicExpr());

    if (!isZeroRationalSymbol(logicSymbol)) {
      executeBlock(ifStatementContext.block());
    }

  }

  private void executeIfElseStatement(IfElseStatementContext ifElseStatementContext) {

    Symbol logicSymbol = executeParenLogicExpression(ifElseStatementContext.parenLogicExpr());

    if (!isZeroRationalSymbol(logicSymbol)) {
      executeBlock(ifElseStatementContext.ifBlock);
    } else {
      executeBlock(ifElseStatementContext.elseBlock);
    }
  }

  private void executeWhileStatement(WhileStatementContext whileStatementContext) {

    Symbol logicSymbol = executeParenLogicExpression(whileStatementContext.parenLogicExpr());

    while (!isZeroRationalSymbol(logicSymbol)) {
      executeBlock(whileStatementContext.block());

      logicSymbol = executeParenLogicExpression(whileStatementContext.parenLogicExpr());
    }
  }

  private void executeForStatement(ForStatementContext forStatementContext) {
    processVariableDeclaration(forStatementContext.variableDeclaration());

    Symbol logicSymbol = executeLogicExpression(forStatementContext.logic);

    while (!isZeroRationalSymbol(logicSymbol)) {
      executeBlock(forStatementContext.block());

      executeSimpleStatement(forStatementContext.update);

      logicSymbol = executeLogicExpression(forStatementContext.logic);
    }

  }

  private Symbol executeParenLogicExpression(ParenLogicExprContext parenLogicExprContext) {
    return executeLogicExpression(parenLogicExprContext.logicExpr());
  }

  private Symbol executeLogicExpression(LogicExprContext logicExprContext) {
    Symbol logicSymbol = Symbol.builder()
        .identifier("logicSymbol")
        .type(Type.RATIONAL)
        .firstOccurrence(CodePoint.from(logicExprContext))
        .build();

    if (logicExprContext instanceof ExprLogicExpressionContext) {
      executeExpression(((ExprLogicExpressionContext) logicExprContext).expr(), logicSymbol);
    } else {
      executeRelationLogicExpression((RelationLogicExpressionContext) logicExprContext,
          logicSymbol);
    }

    return logicSymbol;
  }

  private void executeRelationLogicExpression(
      RelationLogicExpressionContext relationLogicExpressionContext, Symbol logicSymbol) {

    LogicOperation logicOperation = LogicOperation.getInstance();

    Symbol leftSymbol = Symbol.builder()
        .identifier("leftLogic")
        .type(Type.RATIONAL)
        .firstOccurrence(CodePoint.from(relationLogicExpressionContext))
        .build();

    Symbol rightSymbol = Symbol.builder()
        .identifier("rightLogic")
        .type(Type.RATIONAL)
        .firstOccurrence(CodePoint.from(relationLogicExpressionContext))
        .build();

    executeExpression(relationLogicExpressionContext.leftExpr, leftSymbol);
    executeExpression(relationLogicExpressionContext.rightExpr, rightSymbol);

    Rational left = (Rational) leftSymbol.getValue(Type.RATIONAL);
    Rational right = (Rational) rightSymbol.getValue(Type.RATIONAL);

    Rational result = null;

    switch (relationLogicExpressionContext.relation().getText()) {
      case "<": {
        result = logicOperation.lessThan(left, right);
      }
      break;
      case ">": {
        result = logicOperation.greaterThan(left, right);
      }
      break;
      case "<=": {
        result = logicOperation.lessThanOrEqual(left, right);
      }
      break;
      case ">=": {
        result = logicOperation.greaterThanOrEqual(left, right);
      }
      break;
      case "==": {
        result = logicOperation.equalTo(left, right);
      }
      break;
      case "!=": {
        result = logicOperation.equalTo(left, right).equals(Rational.TRUE) ?
            Rational.FALSE : Rational.TRUE;
      }
      break;
    }

    logicSymbol.setValue(result, Type.RATIONAL);
  }

  private boolean isZeroRationalSymbol(Symbol symbol) {
    Rational logicRational = (Rational) symbol.getValue(Type.RATIONAL);

    return logicRational.getDenominator().compareTo(BigInteger.ZERO) == 0
        || logicRational.getNumerator().compareTo(BigInteger.ZERO) == 0;
  }


  private static class CallContext {

    private final Symbol returnSymbol;

    private final Scope scope;


    private CallContext(Symbol returnSymbol, Scope scope) {
      this.returnSymbol = returnSymbol;
      this.scope = scope;
    }
  }
}
