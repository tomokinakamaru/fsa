package com.github.tomokinakamaru.fsa.test;

import org.junit.jupiter.api.Test;

final class TestAutomaton {

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
  void testDeterminize1() {
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

  @Test
  void testDeterminized2() {
    IntAutomaton a = new IntAutomaton().determinized();
    assert a.getStates().isEmpty();

    a.consume(1);
    assert !a.isAccepting();
  }

  @Test
  void testGetTransitions() {
    IntAutomaton a = new IntAutomaton(1);
    assert a.getTransitionsFrom(a.initials.iterator().next()).size() == 1;
    assert a.getTransitionsTo(a.finals.iterator().next()).size() == 1;
  }

  @Test
  void testGetReverseEpsilonClosure() {
    IntAutomaton a1 = new IntAutomaton(1).and(new IntAutomaton(null));
    assert a1.getEpsilonClosureTo(a1.finals).size() == 3;

    IntAutomaton a2 = new IntAutomaton(1).and(new IntAutomaton(null)).repeated();
    assert a2.getEpsilonClosureTo(a2.finals.iterator().next()).size() == 4;
  }
}
