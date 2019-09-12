package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.State;
import com.github.tomokinakamaru.fsa.Transition;

final class IntTransition extends Transition<Integer> {

  IntTransition(State source, Integer symbol, State destination) {
    super(source, symbol, destination);
  }
}
