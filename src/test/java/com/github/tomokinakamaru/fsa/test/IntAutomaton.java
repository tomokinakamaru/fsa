package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.Automaton;
import com.github.tomokinakamaru.fsa.State;

final class IntAutomaton extends Automaton<Integer, IntTransition, IntAutomaton> {

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
}
