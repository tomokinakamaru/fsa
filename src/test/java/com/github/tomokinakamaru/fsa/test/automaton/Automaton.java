package com.github.tomokinakamaru.fsa.test.automaton;

import com.github.tomokinakamaru.fsa.AbstractAutomaton;

public final class Automaton extends AbstractAutomaton<State, Integer, Transition, Automaton> {

  public Automaton() {}

  public Automaton(Integer n) {
    super(n);
  }

  @Override
  protected Automaton newAutomaton() {
    return new Automaton();
  }

  @Override
  protected Transition newTransition(State source, Integer symbol, State destination) {
    return new Transition(source, symbol, destination);
  }

  @Override
  protected State newState() {
    return new State();
  }
}
