package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.AbstractTransition;

final class IntTransition extends AbstractTransition<State, Integer> {

  IntTransition(State source, Integer symbol, State destination) {
    super(source, symbol, destination);
  }
}
