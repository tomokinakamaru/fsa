package com.github.tomokinakamaru.fsa.test;

import com.github.tomokinakamaru.fsa.test.automaton.State;
import com.github.tomokinakamaru.fsa.test.automaton.Transition;
import org.junit.jupiter.api.Test;

final class TestTransition {

  @Test
  void testEquals1() {
    Transition t = new Transition(new State(), null, new State());
    assert !t.equals(new Object());
  }

  @Test
  void testEquals2() {
    State s = new State();
    Transition t1 = new Transition(s, null, new State());
    Transition t2 = new Transition(s, null, new State());
    assert !t1.equals(t2);
  }

  @Test
  void testEquals3() {
    State s = new State();
    Transition t1 = new Transition(new State(), null, s);
    Transition t2 = new Transition(new State(), null, s);
    assert !t1.equals(t2);
  }

  @Test
  void testEquals4() {
    State s = new State();
    Transition t1 = new Transition(s, 1, s);
    Transition t2 = new Transition(s, 1, s);
    assert t1.equals(t2);
  }
}
