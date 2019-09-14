package com.github.tomokinakamaru.fsa.test;

import org.junit.jupiter.api.Test;

final class TestTransition {

  @Test
  void testEquals1() {
    IntTransition t = new IntTransition(new State(), null, new State());
    assert !t.equals(new Object());
  }

  @Test
  void testEquals2() {
    State s = new State();
    IntTransition t1 = new IntTransition(s, null, new State());
    IntTransition t2 = new IntTransition(s, null, new State());
    assert !t1.equals(t2);
  }

  @Test
  void testEquals3() {
    State s = new State();
    IntTransition t1 = new IntTransition(new State(), null, s);
    IntTransition t2 = new IntTransition(new State(), null, s);
    assert !t1.equals(t2);
  }

  @Test
  void testEquals4() {
    State s = new State();
    IntTransition t1 = new IntTransition(s, 1, s);
    IntTransition t2 = new IntTransition(s, 1, s);
    assert t1.equals(t2);
  }
}
