package com.github.tomokinakamaru.fsa.test.automaton;

import com.github.tomokinakamaru.fsa.AbstractTransition;

public final class Transition extends AbstractTransition<State, Integer> {

  public Transition(State source, Integer symbol, State destination) {
    super(source, symbol, destination);
  }
}
