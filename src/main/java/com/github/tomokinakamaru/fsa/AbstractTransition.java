package com.github.tomokinakamaru.fsa;

import java.util.Objects;

public abstract class AbstractTransition<S> {

  public final State source;

  public final S symbol;

  public final State destination;

  protected AbstractTransition(State source, S symbol, State destination) {
    this.source = source;
    this.symbol = symbol;
    this.destination = destination;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(source, source, destination);
  }

  @Override
  public final boolean equals(Object obj) {
    if (!(obj instanceof AbstractTransition)) {
      return false;
    }
    AbstractTransition<?> transition = (AbstractTransition<?>) obj;
    return Objects.equals(source, transition.source)
        && Objects.equals(symbol, transition.symbol)
        && Objects.equals(destination, transition.destination);
  }
}
