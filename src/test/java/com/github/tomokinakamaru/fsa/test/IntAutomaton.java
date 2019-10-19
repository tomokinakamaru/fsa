package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.AbstractAutomaton;

final class IntAutomaton extends AbstractAutomaton<State, Integer, IntTransition, IntAutomaton> {

  IntAutomaton() {}

  IntAutomaton(Integer n) {
    super(n);
  }

  @Override
  protected IntAutomaton newAutomaton() {
    return new IntAutomaton();
  }

  @Override
  protected IntTransition newTransition(State source, Integer symbol, State destination) {
    return new IntTransition(source, symbol, destination);
  }

  @Override
  protected State newState() {
    return new State();
  }
}
