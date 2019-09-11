package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.Automaton;
import com.github.tomokinakamaru.fsa.State;
import com.github.tomokinakamaru.fsa.Transition;
import org.junit.jupiter.api.Test;

final class Main {

  private static final class IntTransition extends Transition<Integer> {
    IntTransition(State source, Integer symbol, State destination) {
      super(source, symbol, destination);
    }
  }

  private static final class IntAutomaton extends Automaton<Integer, IntTransition, IntAutomaton> {

    IntAutomaton() {}

    IntAutomaton(int n) {
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

  @Test
  void testAtom() {
    IntAutomaton a = new IntAutomaton(1);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testAnd() {
    IntAutomaton a1 = new IntAutomaton(1);
    IntAutomaton a2 = new IntAutomaton(2);
    IntAutomaton a = a1.and(a2).and(a1);
    assert !a.isAccepting();

    a.consume(1);
    assert !a.isAccepting();

    a.consume(2);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testOr() {
    IntAutomaton a1 = new IntAutomaton(1);
    IntAutomaton a2 = new IntAutomaton(2);
    IntAutomaton a = a1.or(a2);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    a.reset();
    assert !a.isAccepting();

    a.consume(2);
    assert a.isAccepting();
  }

  @Test
  void testRepeat() {
    IntAutomaton a = new IntAutomaton(1).repeated();
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testDeterminize() {
    IntAutomaton a1 = new IntAutomaton(1);
    IntAutomaton a2 = new IntAutomaton(2);
    IntAutomaton a3 = new IntAutomaton(3);
    IntAutomaton a = a1.and(a2.or(a3)).and(a1).reversed().determinized().reversed().determinized();
    assert !a.isAccepting();

    a.consume(1);
    assert !a.isAccepting();

    a.consume(2);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    assert a.transitions.stream().noneMatch(t -> t.symbol == null);
    assert a.getStates().size() == 4;
  }
}
