package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.AbstractTransition;
import com.github.tomokinakamaru.fsa.State;

final class IntTransition extends AbstractTransition<Integer> {

  IntTransition(State source, Integer symbol, State destination) {
    super(source, symbol, destination);
  }
}
