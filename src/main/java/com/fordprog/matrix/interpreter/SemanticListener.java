package com.fordprog.matrix.interpreter;


import com.fordprog.matrix.MatrixBaseListener;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.AssignmentToFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.AssignmentWithVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.FunctionCallExpectedSemanticError;
import com.fordprog.matrix.interpreter.error.IdentifierAlreadyDeclaredSemanticError;
import com.fordprog.matrix.interpreter.error.InvalidMainFunctionSignatureSemanticError;
import com.fordprog.matrix.interpreter.error.MissingMainFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.MissingReturnInFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.OperationWithVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.ReturnInVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.SemanticError;
import com.fordprog.matrix.interpreter.error.UndeclaredIdentifierSemanticError;
import com.fordprog.matrix.interpreter.error.VoidParameterInFunctionCallSemanticError;
import com.fordprog.matrix.interpreter.error.WrongNumberOfParametersSemanticError;
import com.fordprog.matrix.interpreter.scope.Symbol;
import com.fordprog.matrix.interpreter.scope.SymbolTable;
import com.fordprog.matrix.interpreter.type.Function;
import com.fordprog.matrix.interpreter.type.Type;
import com.fordprog.matrix.interpreter.type.UserDefinedFunction;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SemanticListener extends MatrixBaseListener {

  private SymbolTable symbolTable;

  private final List<SemanticError> semanticErrors;

  private final ParseTreeProperty<Function> functions;

  private final ParseTreeProperty<Type> expressionTypes;

  private final ParseTreeProperty<Boolean> hasReturnStatement;

  public SemanticListener() {

    this.semanticErrors = new LinkedList<>();

    this.expressionTypes = new ParseTreeProperty<>();

    this.functions = new ParseTreeProperty<>();

    this.hasReturnStatement = new ParseTreeProperty<>();
  }

  @Override
  public void enterProgram(MatrixParser.ProgramContext ctx) {
    symbolTable = new SymbolTable(ctx);
  }

  @Override
  public void exitProgram(MatrixParser.ProgramContext ctx) {
    Symbol mainSymbol = symbolTable.getSymbol("main");

    // We have to check if function named 'main' exists
    if (mainSymbol == null || mainSymbol.getType() != Type.FUNCTION) {
      semanticErrors.add(new MissingMainFunctionSemanticError(CodePoint.from(ctx)));

      return;
    }

    Function mainFunction = (Function) mainSymbol.getValue();

    // Also we have to check for correct function signature
    // Correct signature: 'void main()'
    if (mainFunction.getReturnType() != Type.VOID || !mainFunction.getParameterList().isEmpty()) {
      semanticErrors.add(new InvalidMainFunctionSignatureSemanticError(CodePoint.from(ctx)));
    }

    symbolTable.exitScope();
  }

  @Override
  public void exitDeclaration(MatrixParser.DeclarationContext ctx) {
    Type declareType = Type.valueOf(ctx.type().getText().toUpperCase());

    // We have to check if the scope already has a declaration with the given identifier
    if (checkIfIndentifierNotInScope(ctx.id())) {
      // The current declaration is unique in the scope
      Symbol declaredSymbol = Symbol.builder()
          .identifier(ctx.id().getText())
          .type(declareType)
          .firstOccurrence(CodePoint.from(ctx))
          .build();

      symbolTable.addSymbol(declaredSymbol);
    }

    // We check if the expression has the correct type

    //checkMatchingTypes(declareType, ctx.expr());
  }

  @Override
  public void enterFunctionDecl(MatrixParser.FunctionDeclContext ctx) {
    // We check if the current scope already has a symbol named as the declaration's identifier
    if (checkIfIndentifierNotInScope(ctx.id())) {
      addFunctionSymbol(ctx);

      // New scope for parameters
      symbolTable.newScope(ctx);

      // Add all function parameters to scope
      getFunctionParameters(ctx.functionDeclParameterList()).forEach(symbolTable::addSymbol);
    }
  }

  @Override
  public void exitFunctionDecl(MatrixParser.FunctionDeclContext ctx) {
    if (functions.get(ctx) == null) {
      return;
    }

    // Check if the function has no return and return type is not 'void'
    if (functions.get(ctx).getReturnType() != Type.VOID && hasReturnStatement.get(ctx) == null) {
      semanticErrors
          .add(new MissingReturnInFunctionSemanticError(CodePoint.from(ctx), ctx.id().getText()));
    }

    // We have to destroy the scope with the parameters
    symbolTable.exitScope();
  }

  @Override
  public void enterBlock(MatrixParser.BlockContext ctx) {
    symbolTable.newScope(ctx);
  }

  @Override
  public void exitBlock(MatrixParser.BlockContext ctx) {
    symbolTable.exitScope();
  }

  @Override
  public void exitAssignStatement(MatrixParser.AssignStatementContext ctx) {
    // Check if the assignee exists in scope
    if (!symbolTable.inScope(ctx.id().getText())) {
      semanticErrors
          .add(new UndeclaredIdentifierSemanticError(CodePoint.from(ctx), ctx.id().getText()));

      return;
    }
    // When the assignee exists, we need to check if the expression type matches the declared symbol type
    else {
      Symbol assigneeSymbol = symbolTable.getSymbol(ctx.id().getText());

      Type assigneeType = assigneeSymbol.getType();

      Type exprType = expressionTypes.get(ctx.expr());

      // Check if assignment right side has type, if not error
      if (exprType == Type.VOID) {
        // We can assume that th expression is a function call, because only function return can be void
        MatrixParser.FunctionCallContext functionCallContext =
            (MatrixParser.FunctionCallContext) ctx.expr().getRuleContext();

        Symbol functionSymbol = symbolTable.getSymbol(functionCallContext.id().getText());

        semanticErrors.add(new AssignmentWithVoidFunctionSemanticError(CodePoint.from(ctx),
            functionSymbol.getIdentifier(), functionSymbol.getFirstOccurrence()));

        return;
      }

      // Check if assignee is a function, if it is error
      if (assigneeType == Type.FUNCTION) {
        semanticErrors.add(new AssignmentToFunctionSemanticError(CodePoint.from(ctx)));

        return;
      }
      // Check is the assignee's and expression type matches

//      else {
//        checkMatchingTypes(assigneeType, ctx.expr());
//      }

    }
  }

  @Override
  public void exitReturnStatement(MatrixParser.ReturnStatementContext ctx) {
    MatrixParser.FunctionDeclContext parentFunctionDeclContext = getParentFunctionDecl(ctx);

    // If parent function is void add error, because of this 'return' statement
    if (functions.get(parentFunctionDeclContext).getReturnType() == Type.VOID) {
      semanticErrors.add(new ReturnInVoidFunctionSemanticError(CodePoint.from(ctx)));
    }

//    else {
//      Type returnType = functions.get(parentFunctionDeclContext).getReturnType();
//
//      Type exprType = expressionTypes.get(ctx.expr());
//
//      // When parent function is non-void, then check if return type matches the returned expression type
//      if (returnType != exprType) {
//        semanticErrors.add(
//            new ReturnTypeMismatchSemanticError(CodePoint.from(ctx), returnType, exprType,
//                parentFunctionDeclContext.id().getText()));
//      }
//    }

    hasReturnStatement.put(parentFunctionDeclContext, true);
  }

  @Override
  public void enterForStatement(MatrixParser.ForStatementContext ctx) {
    // New scope for the loop variable
    symbolTable.newScope(ctx);
  }

  @Override
  public void exitForStatement(MatrixParser.ForStatementContext ctx) {
    // Destroy scope with the loop variable
    symbolTable.exitScope();
  }

  @Override
  public void exitExprLogicExpression(MatrixParser.ExprLogicExpressionContext ctx) {

    if (expressionTypes.get(ctx.expr()) == Type.VOID) {
      semanticErrors.add(
          new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx), ctx.expr().getText()));

      return;
    }

  }

  @Override
  public void exitRelationLogicExpression(MatrixParser.RelationLogicExpressionContext ctx) {

    Type leftExprType = expressionTypes.get(ctx.leftExpr);

    Type rightExprType = expressionTypes.get(ctx.rightExpr);

    if (leftExprType == Type.VOID) {
      semanticErrors.add(new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx),
          ctx.leftExpr.getText()));

      return;
    } else if (rightExprType == Type.VOID) {
      semanticErrors.add(new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx),
          ctx.rightExpr.getText()));

      return;
    }

  }

  @Override
  public void exitBinOperatorExpression(MatrixParser.BinOperatorExpressionContext ctx) {

    Type leftOperandType = expressionTypes.get(ctx.leftOperand);

    Type rightOperandType = expressionTypes.get(ctx.rightOperand);

    if (leftOperandType == Type.VOID) {
      semanticErrors.add(new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx),
          ctx.leftOperand.getText()));

      return;
    } else if (rightOperandType == Type.VOID) {
      semanticErrors.add(new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx),
          ctx.rightOperand.getText()));

      return;
    }

  }

  @Override
  public void enterFunctionCallExpression(MatrixParser.FunctionCallExpressionContext ctx) {
    Function function =
        (Function) (symbolTable.getSymbol(ctx.functionCall().id().getText()).getValue());

    Type returnType = function.getReturnType();

    if (returnType == Type.VOID) {
      expressionTypes.put(ctx, Type.VOID);
    }

  }

  @Override
  public void exitParenthesisExpression(MatrixParser.ParenthesisExpressionContext ctx) {

    Type exprType = expressionTypes.get(ctx.expr());
    expressionTypes.put(ctx, exprType);

  }

  @Override
  public void exitFunctionCall(MatrixParser.FunctionCallContext ctx) {
    String functionIdentifier = ctx.id().getText();

    if (!symbolTable.inScope(functionIdentifier)) {
      semanticErrors
          .add(new UndeclaredIdentifierSemanticError(CodePoint.from(ctx), functionIdentifier));

      return;
    }

    Function function = (Function) symbolTable.getSymbol(functionIdentifier).getValue();

    int expectedParameterNum = function.getParameterList().size();

    int actualParameterNum = ctx.functionCallParameterList().expr().size();

    if (expectedParameterNum != actualParameterNum) {
      semanticErrors.add(
          new WrongNumberOfParametersSemanticError(CodePoint.from(ctx), expectedParameterNum,
              actualParameterNum));
    }

    for (int i = 0; i < actualParameterNum; ++i) {
      Type exprType = expressionTypes.get(ctx.functionCallParameterList().expr(i));

      if (exprType == Type.VOID) {
        semanticErrors.add(
            new VoidParameterInFunctionCallSemanticError(CodePoint.from(ctx), functionIdentifier,
                i + 1));
      }
    }

  }

  @Override
  public void enterIdTerm(MatrixParser.IdTermContext ctx) {
    String identifier = ctx.id().getText();
    if (symbolTable.getSymbol(identifier).getType() == Type.FUNCTION) {
      semanticErrors.add(new FunctionCallExpectedSemanticError(CodePoint.from(ctx), identifier));
    }

  }

  /*
           * Helper private methods
           */
  private boolean checkIfIndentifierNotInScope(MatrixParser.IdContext idContext) {
    String identifier = idContext.getText();

    // We check if the scope already has a symbol with name 'identifier'
    if (symbolTable.inScope(identifier)) {
      // Scope already has 'identifier' symbol
      // Declaration cannot be done
      Symbol originalSymbol = symbolTable.getSymbol(identifier);

      semanticErrors
          .add(new IdentifierAlreadyDeclaredSemanticError(CodePoint.from(idContext), identifier,
              originalSymbol.getFirstOccurrence()));

      return false;
    }

    return true;
  }

  private void addFunctionSymbol(MatrixParser.FunctionDeclContext ctx) {
    Type returnType = Type.valueOf(ctx.type().getText().toUpperCase());

    List<Symbol> parameterSymbols = getFunctionParameters(ctx.functionDeclParameterList());

    UserDefinedFunction function =
        new UserDefinedFunction(returnType, parameterSymbols);

    functions.put(ctx, function);

    Symbol functionSymbol = Symbol.builder()
        .identifier(ctx.id().getText())
        .type(Type.FUNCTION)
        .firstOccurrence(CodePoint.from(ctx))
        .value(function)
        .build();

    symbolTable.addSymbol(functionSymbol);

    // New scope for parameters
    symbolTable.newScope(ctx);

    // Add all function parameters to scope
    parameterSymbols.forEach(symbolTable::addSymbol);
  }

  private List<Symbol> getFunctionParameters(MatrixParser.FunctionDeclParameterListContext ctx) {
    List<Symbol> parameterSymbols = new ArrayList<>();

    for (MatrixParser.FunctionDeclParameterContext paramCtx : ctx.functionDeclParameter()) {

      Symbol paramSymbol = Symbol.builder()
          .identifier(paramCtx.id().getText())
          .type(Type.valueOf(paramCtx.type().getText().toUpperCase()))
          .firstOccurrence(CodePoint.from(paramCtx))
          .build();

      parameterSymbols.add(paramSymbol);
    }

    return parameterSymbols;
  }

  private MatrixParser.FunctionDeclContext getParentFunctionDecl(ParserRuleContext ctx) {
    ParserRuleContext parent = ctx;

    do {
      parent = parent.getParent();
    } while (!(parent instanceof MatrixParser.FunctionDeclContext));

    return (MatrixParser.FunctionDeclContext) parent;
  }

  /*
   * Getters
   */

  public List<SemanticError> getSemanticErrors() {
    return semanticErrors;
  }

}
