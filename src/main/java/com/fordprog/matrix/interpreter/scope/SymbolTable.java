package com.fordprog.matrix.interpreter.scope;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.IdentityHashMap;
import java.util.Map;

public class SymbolTable {

  private final Map<ParserRuleContext, Scope> scopeMap;

  private Scope currentScope;

  public SymbolTable(ParserRuleContext rootContext) {
    Scope rootScope = new Scope(null, rootContext);

    scopeMap = new IdentityHashMap<>();

    scopeMap.put(rootContext, rootScope);

    currentScope = rootScope;
  }

  public void newScope(ParserRuleContext context) {
    currentScope = scopeMap.computeIfAbsent(context, c -> new Scope(currentScope, context));
  }

  public void exitScope() {
    if (currentScope.getParent() == null) {
      throw new IllegalStateException("The current scope has no parent scope!");
    }

    currentScope = currentScope.getParent();
  }

  public void addSymbol(Symbol symbol) {
    currentScope.addSymbol(symbol);
  }

  public boolean inScope(String symbolName) {
    return currentScope.inScope(symbolName);
  }

  public boolean inCurrentScope(String symbolName) {
    return currentScope.inThisScope(symbolName);
  }

  public Symbol getSymbol(String symbolName) {
    return currentScope.getSymbol(symbolName);
  }

  public Scope getScope(ParserRuleContext context) {
    return scopeMap.get(context);
  }

  public Scope getCurrentScope() {
    return currentScope;
  }
}
