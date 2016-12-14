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
import static com.fordprog.matrix.MatrixParser.Matrix_elementContext;
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
import com.fordprog.matrix.interpreter.scope.Scope;
import com.fordprog.matrix.interpreter.scope.Symbol;
import com.fordprog.matrix.interpreter.scope.SymbolTable;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Function;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;
import com.fordprog.matrix.interpreter.type.Type;
import com.fordprog.matrix.interpreter.type.UserDefinedFunction;

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
    int numerator = Integer.parseInt(rationalContext.INTEGER(0).getText());
    int denominator = Integer.parseInt(rationalContext.INTEGER(1).getText());

    Rational rational = new Rational(numerator, denominator);

    targetSymbol.setValue(rational, Type.RATIONAL);
  }

  private void executeMatrixTerm(MatrixTermContext termContext, Symbol targetSymbol) {
    executeMatrixContext(termContext.matrix(), targetSymbol);
  }

  private void executeMatrixContext(MatrixContext matrixContext, Symbol targetSymbol) {
    int rowNum = matrixContext.matrix_row().size();
    int columnNum = matrixContext.matrix_row(0).matrix_element().size();

    Rational rationalMatrix[][] = new Rational[rowNum][columnNum];

    for (int r = 0; r < rowNum; ++r) {
      for (int c = 0; c < columnNum; ++c) {
        Symbol tempSymbol = Symbol.builder()
            .identifier("temp")
            .firstOccurrence(CodePoint.from(matrixContext))
            .type(Type.RATIONAL)
            .build();
        Matrix_elementContext currentElement = matrixContext.matrix_row(r).matrix_element(c);

        if (currentElement.id() != null) {
          executeId(currentElement.id(), tempSymbol);
        } else {
          executeRational(currentElement.rational(), tempSymbol);
        }

        rationalMatrix[r][c] = (Rational) tempSymbol.getValue();

      }
    }

    Matrix matrix = new Matrix(rationalMatrix);
    targetSymbol.setValue(matrix, Type.MATRIX);
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
    //TODO
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
    //TODO
  }

  private boolean isZeroRationalSymbol(Symbol symbol) {
    Rational logicRational = (Rational) symbol.getValue(Type.RATIONAL);

    return logicRational.getDenominator() == 0 || logicRational.getNumerator() == 0;
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
