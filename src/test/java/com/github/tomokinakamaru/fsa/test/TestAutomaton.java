package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.test.automaton.Automaton;
import org.junit.jupiter.api.Test;

final class TestAutomaton {

  @Test
  void testAtom() {
    Automaton a = new Automaton(1);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testAnd() {
    Automaton a1 = new Automaton(1);
    Automaton a2 = new Automaton(2);
    Automaton a = a1.and(a2).and(a1);
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
    Automaton a1 = new Automaton(1);
    Automaton a2 = new Automaton(2);
    Automaton a = a1.or(a2);
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
    Automaton a = new Automaton(1).repeated();
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testDeterminize1() {
    Automaton a1 = new Automaton(1);
    Automaton a2 = new Automaton(2);
    Automaton a3 = new Automaton(3);
    Automaton a = a1.and(a2.or(a3)).and(a1).minDeterminized();
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

  @Test
  void testDeterminized2() {
    Automaton a = new Automaton().determinized();
    assert a.getStates().isEmpty();

    a.consume(1);
    assert !a.isAccepting();
  }

  @Test
  void testGetTransitions() {
    Automaton a = new Automaton(1);
    assert a.getTransitionsFrom(a.initials.iterator().next()).size() == 1;
    assert a.getTransitionsTo(a.finals.iterator().next()).size() == 1;
  }

  @Test
  void testGetReverseEpsilonClosure() {
    Automaton a1 = new Automaton(1).and(new Automaton(null));
    assert a1.getEpsilonClosureTo(a1.finals).size() == 3;

    Automaton a2 = new Automaton(1).and(new Automaton(null)).repeated();
    assert a2.getEpsilonClosureTo(a2.finals.iterator().next()).size() == 4;
  }
}
