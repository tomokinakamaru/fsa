package com.github.tomokinakamaru.fsa;

import java.util.Objects;

public final class Transition<T> {

  public State source;

  public T symbol;

  public State destination;

  public Transition(State source, T symbol, State destination) {
    this.source = source;
    this.symbol = symbol;
    this.destination = destination;
  }

  public boolean isEpsilon() {
    return symbol == null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, source, destination);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Transition)) {
      return false;
    }
    Transition transition = (Transition) obj;
    return Objects.equals(source, transition.source)
        && Objects.equals(symbol, transition.symbol)
        && Objects.equals(destination, transition.destination);
  }
}
