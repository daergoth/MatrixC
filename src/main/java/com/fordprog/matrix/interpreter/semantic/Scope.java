package com.fordprog.matrix.interpreter.semantic;


import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

public class Scope {

  public static final Scope NULL_SCOPE = new Scope(null, null);

  private final Scope parent;

  private final ParserRuleContext context;

  private final Map<String, Symbol> scopeTable;

  public Scope(Scope parent, ParserRuleContext context) {
    this.parent = parent;
    this.context = context;

    scopeTable = new HashMap<>();
  }

  public boolean inScope(String varName) {
    return scopeTable.containsKey(varName) || (parent != null && parent.inScope(varName));
  }

  public boolean inThisScope(String varName) {
    return scopeTable.containsKey(varName);
  }

  public Symbol getSymbol(String varName) {
    if (scopeTable.containsKey(varName)) {
      return scopeTable.get(varName);
    }

    if (parent != null) {
      return parent.getSymbol(varName);
    }

    return null;
  }

  public void addSymbol(Symbol symbol) {
    scopeTable.put(symbol.getIdentifier(), symbol);
  }

  public Scope getParent() {
    return parent;
  }

  public ParserRuleContext getContext() {
    return context;
  }
}
