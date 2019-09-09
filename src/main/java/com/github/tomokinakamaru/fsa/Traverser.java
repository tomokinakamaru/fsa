package com.github.tomokinakamaru.fsa;

public interface Traverser<T> {

  default void traverse(State state, FiniteStateAutomaton<T> automaton) {}
}
