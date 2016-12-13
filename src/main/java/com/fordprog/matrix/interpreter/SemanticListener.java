package com.fordprog.matrix.interpreter;


import static com.fordprog.matrix.MatrixParser.AssignStatementContext;
import static com.fordprog.matrix.MatrixParser.BinOperatorExpressionContext;
import static com.fordprog.matrix.MatrixParser.BlockContext;
import static com.fordprog.matrix.MatrixParser.BlockStatementContext;
import static com.fordprog.matrix.MatrixParser.ControllBlockStatementContext;
import static com.fordprog.matrix.MatrixParser.ExprLogicExpressionContext;
import static com.fordprog.matrix.MatrixParser.ForStatementContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallContext;
import static com.fordprog.matrix.MatrixParser.FunctionCallExpressionContext;
import static com.fordprog.matrix.MatrixParser.FunctionDeclContext;
import static com.fordprog.matrix.MatrixParser.FunctionDeclParameterContext;
import static com.fordprog.matrix.MatrixParser.FunctionDeclParameterListContext;
import static com.fordprog.matrix.MatrixParser.IdContext;
import static com.fordprog.matrix.MatrixParser.IdTermContext;
import static com.fordprog.matrix.MatrixParser.IfElseStatementContext;
import static com.fordprog.matrix.MatrixParser.MatrixContext;
import static com.fordprog.matrix.MatrixParser.Matrix_rowContext;
import static com.fordprog.matrix.MatrixParser.ParenthesisExpressionContext;
import static com.fordprog.matrix.MatrixParser.ProgramContext;
import static com.fordprog.matrix.MatrixParser.RelationLogicExpressionContext;
import static com.fordprog.matrix.MatrixParser.ReturnStatementContext;
import static com.fordprog.matrix.MatrixParser.SimpleStatementStatementContext;
import static com.fordprog.matrix.MatrixParser.StatementContext;
import static com.fordprog.matrix.MatrixParser.VariableDeclarationContext;

import com.fordprog.matrix.MatrixBaseListener;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.AssignmentToFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.AssignmentWithVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.FunctionCallExpectedSemanticError;
import com.fordprog.matrix.interpreter.error.IdentifierAlreadyDeclaredSemanticError;
import com.fordprog.matrix.interpreter.error.IllformedMatrixSemanticError;
import com.fordprog.matrix.interpreter.error.InvalidMainFunctionSignatureSemanticError;
import com.fordprog.matrix.interpreter.error.MissingMainFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.MissingReturnInFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.OperationWithVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.ReturnInVoidFunctionSemanticError;
import com.fordprog.matrix.interpreter.error.SemanticError;
import com.fordprog.matrix.interpreter.error.TypeMismatchSemanticError;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SemanticListener extends MatrixBaseListener {

  private SymbolTable symbolTable;

  private final List<SemanticError> semanticErrors;

  private final ParseTreeProperty<Function> functions;

  private final ParseTreeProperty<Type> expressionTypes;


  public SemanticListener() {
    this.semanticErrors = new LinkedList<>();

    this.expressionTypes = new ParseTreeProperty<>();

    this.functions = new ParseTreeProperty<>();
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    symbolTable = new SymbolTable(ctx);
  }

  @Override
  public void exitProgram(ProgramContext ctx) {
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

  }


  @Override
  public void exitVariableDeclaration(VariableDeclarationContext ctx) {
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
  public void enterFunctionDecl(FunctionDeclContext ctx) {
    // We check if the current scope already has a symbol named as the declaration's identifier
    if (checkIfIndentifierNotInScope(ctx.id())) {
      addFunctionSymbol(ctx);

//      // New scope for parameters
//      symbolTable.newScope(ctx);
//
//      // Add all function parameters to scope
//      getFunctionParameters(ctx.functionDeclParameterList()).forEach(symbolTable::addSymbol);
    }
  }

  @Override
  public void exitFunctionDecl(FunctionDeclContext ctx) {
    if (functions.get(ctx) == null) {
      return;
    }

    boolean allBranchesCoveredWithReturn = checkBranchesForReturn(ctx.block());

    // Check if the function has no return and return type is not 'void'
    if (functions.get(ctx).getReturnType() != Type.VOID && !allBranchesCoveredWithReturn) {
      semanticErrors
          .add(new MissingReturnInFunctionSemanticError(CodePoint.from(ctx), ctx.id().getText()));
    }

    // We have to destroy the scope with the parameters
    symbolTable.exitScope();
  }

  @Override
  public void enterBlock(BlockContext ctx) {
    symbolTable.newScope(ctx);
  }

  @Override
  public void exitBlock(BlockContext ctx) {
    symbolTable.exitScope();
  }

  @Override
  public void exitAssignStatement(AssignStatementContext ctx) {
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
        FunctionCallContext functionCallContext =
            (FunctionCallContext) ctx.expr().getRuleContext();

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
  public void exitReturnStatement(ReturnStatementContext ctx) {
    FunctionDeclContext parentFunctionDeclContext = getParentFunctionDecl(ctx);

    // If parent function is void add error, because of this 'return' statement
    if (functions.get(parentFunctionDeclContext).getReturnType() == Type.VOID) {
      semanticErrors.add(new ReturnInVoidFunctionSemanticError(CodePoint.from(ctx)));
    }

    Type exprType = expressionTypes.get(ctx.expr());

    FunctionDeclContext functionDeclContext = getParentFunctionDecl(ctx);

    Function function = functions.get(functionDeclContext);

    // Expression in return statement is void, then error
    if (exprType == Type.VOID && function.getReturnType() != Type.VOID) {

      semanticErrors.add(
          new TypeMismatchSemanticError(CodePoint.from(ctx), function.getReturnType(), exprType));
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
  }

  @Override
  public void enterForStatement(ForStatementContext ctx) {
    // New scope for the loop variable
    symbolTable.newScope(ctx);
  }

  @Override
  public void exitForStatement(ForStatementContext ctx) {
    // Destroy scope with the loop variable
    symbolTable.exitScope();
  }

  @Override
  public void exitExprLogicExpression(ExprLogicExpressionContext ctx) {

    // We can only check if the expression has void type, if it has error
    if (expressionTypes.get(ctx.expr()) == Type.VOID) {
      semanticErrors.add(
          new OperationWithVoidFunctionSemanticError(CodePoint.from(ctx), ctx.expr().getText()));

      return;
    }

  }

  @Override
  public void exitRelationLogicExpression(RelationLogicExpressionContext ctx) {

    Type leftExprType = expressionTypes.get(ctx.leftExpr);

    Type rightExprType = expressionTypes.get(ctx.rightExpr);

    //If either side of the relation is void, the relation cannot be done
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
  public void exitBinOperatorExpression(BinOperatorExpressionContext ctx) {

    Type leftOperandType = expressionTypes.get(ctx.leftOperand);

    Type rightOperandType = expressionTypes.get(ctx.rightOperand);

    // If either side of the binary operation is void, the operation cannot be done
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
  public void enterFunctionCallExpression(FunctionCallExpressionContext ctx) {
    Function function =
        (Function) (symbolTable.getSymbol(ctx.functionCall().id().getText()).getValue());

    Type returnType = function.getReturnType();

    // Set void expression type if inner function call has no return type
    if (returnType == Type.VOID) {
      expressionTypes.put(ctx, Type.VOID);
    }

  }

  @Override
  public void exitParenthesisExpression(ParenthesisExpressionContext ctx) {

    // Forward the inner expression's type
    Type exprType = expressionTypes.get(ctx.expr());
    expressionTypes.put(ctx, exprType);

  }

  @Override
  public void exitFunctionCall(FunctionCallContext ctx) {
    String functionIdentifier = ctx.id().getText();

    if (!symbolTable.inScope(functionIdentifier)) {
      semanticErrors
          .add(new UndeclaredIdentifierSemanticError(CodePoint.from(ctx), functionIdentifier));

      return;
    }

    Function function = (Function) symbolTable.getSymbol(functionIdentifier).getValue();

    int expectedParameterNum = function.getParameterList().size();

    int actualParameterNum =
        ctx.functionCallParameterList() == null ? 0 : ctx.functionCallParameterList().expr().size();

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
  public void enterIdTerm(IdTermContext ctx) {
    String identifier = ctx.id().getText();

    // Check if the id is for a function, if it is error
    if (symbolTable.getSymbol(identifier).getType() == Type.FUNCTION) {
      semanticErrors.add(new FunctionCallExpectedSemanticError(CodePoint.from(ctx), identifier));
    }

  }

  @Override
  public void exitMatrix(MatrixContext ctx) {
    List<MatrixParser.Matrix_rowContext> matrixRowContexts = ctx.matrix_row();

    int columnNum = matrixRowContexts.get(0).matrix_element().size();
    for (Matrix_rowContext rowContext : matrixRowContexts) {
      if (rowContext.matrix_element().size() != columnNum) {
        semanticErrors.add(new IllformedMatrixSemanticError(CodePoint.from(ctx)));

        return;
      }
    }
  }

  /*
             * Helper private methods
             */
  private boolean checkIfIndentifierNotInScope(IdContext idContext) {
    String identifier = idContext.getText();

    // We check if the scope already has a symbol with name 'identifier'
    if (symbolTable.inCurrentScope(identifier)) {
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

  private void addFunctionSymbol(FunctionDeclContext ctx) {
    Type returnType = Type.valueOf(ctx.returnType().getText().toUpperCase());

    List<Symbol> parameterSymbols = getFunctionParameters(ctx.functionDeclParameterList());

    UserDefinedFunction function =
        new UserDefinedFunction(returnType, parameterSymbols, ctx);

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


  // This method checks if a block's all execution branch are covered with return statements
  // by checking each statement starting from the back.
  // - If it is a return statement the block is covered
  // - If it is an if-else statement, we need to check both blocks for full coverage (recursive calls)
  // - Otherwise all other statements (for, while, if, ...) are ignored, because they alone can't
  //   guarantee full execution branch coverage
  private boolean checkBranchesForReturn(BlockContext blockContext) {
    List<MatrixParser.StatementContext> statementContexts = blockContext.statement();

    // Reverse the list for backward
    Collections.reverse(statementContexts);

    // We start iterating in the reversed statement list
    for (StatementContext stCtx : statementContexts) {

      // If the statement is a return statement, the block is covered
      if (stCtx instanceof SimpleStatementStatementContext) {

        SimpleStatementStatementContext simpleStatement =
            (SimpleStatementStatementContext) stCtx;

        if (simpleStatement.simpleStatement() instanceof ReturnStatementContext) {
          return true;
        }

      }
      // If the statement either a block statement, or an if-else statement,
      //    we check all inner blocks for full coverage.
      // Otherwise we ignore the statement.
      else {
        ControllBlockStatementContext ctrlBlockStatement = (ControllBlockStatementContext) stCtx;

        // If the statement is an if-else statement
        if (ctrlBlockStatement.controllBlock() instanceof IfElseStatementContext) {
          IfElseStatementContext ifElseStatementContext =
              (IfElseStatementContext) ctrlBlockStatement.controllBlock();

          if (checkBranchesForReturn(ifElseStatementContext.ifBlock)
              && checkBranchesForReturn(ifElseStatementContext.elseBlock)) {
            return true;
          }

        }
        // If the statement is a block statement
        else if (ctrlBlockStatement.controllBlock() instanceof BlockStatementContext) {
          BlockStatementContext blockStatementContext =
              (BlockStatementContext) ctrlBlockStatement.controllBlock();

          if (checkBranchesForReturn(blockStatementContext.block())) {
            return true;
          }
        }
      }
    }

    // If we couldn't guarantee full execution branch coverage by this time, the
    // block is missing return statements.
    return false;
  }

  private List<Symbol> getFunctionParameters(FunctionDeclParameterListContext ctx) {
    List<Symbol> parameterSymbols = new ArrayList<>();

    // If the parameter list is empty return empty list
    if (ctx != null) {

      // Make a symbol from every function parameter
      for (FunctionDeclParameterContext paramCtx : ctx.functionDeclParameter()) {

        Symbol paramSymbol = Symbol.builder()
            .identifier(paramCtx.id().getText())
            .type(Type.valueOf(paramCtx.type().getText().toUpperCase()))
            .firstOccurrence(CodePoint.from(paramCtx))
            .build();

        parameterSymbols.add(paramSymbol);
      }
    }

    return parameterSymbols;
  }

  private FunctionDeclContext getParentFunctionDecl(ParserRuleContext ctx) {
    ParserRuleContext parent = ctx;

    do {
      parent = parent.getParent();
    } while (!(parent instanceof FunctionDeclContext));

    return (FunctionDeclContext) parent;
  }

  /*
   * Getters
   */

  public List<SemanticError> getSemanticErrors() {
    return semanticErrors;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }
}
