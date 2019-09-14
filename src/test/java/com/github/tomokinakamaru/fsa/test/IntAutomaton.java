package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.AbstractAutomaton;
import java.util.Set;

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

  @Override
  protected State newState(Set<State> states) {
    return new State();
  }

  @Override
  protected State newState(State state) {
    return new State();
  }
}
