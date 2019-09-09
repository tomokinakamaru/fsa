package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.FiniteStateAutomaton;
import com.github.tomokinakamaru.fsa.State;
import com.github.tomokinakamaru.fsa.Transition;
import com.github.tomokinakamaru.fsa.Traverser;
import org.junit.jupiter.api.Test;

final class Main {

  @Test
  void testAtom() {
    FiniteStateAutomaton<Integer> a = new FiniteStateAutomaton<>(1);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testAnd() {
    FiniteStateAutomaton<Integer> a1 = new FiniteStateAutomaton<>(1);
    FiniteStateAutomaton<Integer> a2 = new FiniteStateAutomaton<>(2);
    FiniteStateAutomaton<Integer> a = a1.and(a2).and(a1);
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
    FiniteStateAutomaton<Integer> a1 = new FiniteStateAutomaton<>(1);
    FiniteStateAutomaton<Integer> a2 = new FiniteStateAutomaton<>(2);
    FiniteStateAutomaton<Integer> a = a1.or(a2);
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
    FiniteStateAutomaton<Integer> a = new FiniteStateAutomaton<>(1).repeated();
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    a.consume(1);
    assert a.isAccepting();
  }

  @Test
  void testDeterminize() {
    FiniteStateAutomaton<Integer> a1 = new FiniteStateAutomaton<>(1);
    FiniteStateAutomaton<Integer> a2 = new FiniteStateAutomaton<>(2);
    FiniteStateAutomaton<Integer> a3 = new FiniteStateAutomaton<>(3);
    FiniteStateAutomaton<Integer> a = a1.and(a2.or(a3)).and(a1).minimumDeterminized();
    assert !a.isAccepting();

    a.consume(1);
    assert !a.isAccepting();

    a.consume(2);
    assert !a.isAccepting();

    a.consume(1);
    assert a.isAccepting();

    assert a.transitions.stream().noneMatch(Transition::isEpsilon);
    assert a.getStates().size() == 4;
  }

  @Test
  void testTraverse() {
    FiniteStateAutomaton<Integer> a1 = new FiniteStateAutomaton<>(1);
    FiniteStateAutomaton<Integer> a2 = new FiniteStateAutomaton<>(2);
    FiniteStateAutomaton<Integer> a3 = new FiniteStateAutomaton<>(3);
    FiniteStateAutomaton<Integer> a = a1.and(a2.or(a3)).and(a1).repeated().minimumDeterminized();

    StateCounter counter = new StateCounter();
    a.traverse(counter);
    assert counter.n == 3;
  }

  private static final class StateCounter implements Traverser<Integer> {

    int n = 0;

    @Override
    public void traverse(State state, FiniteStateAutomaton<Integer> automaton) {
      n += 1;
    }
  }
}
