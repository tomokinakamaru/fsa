package com.github.tomokinakamaru.nfa;

import java.util.Objects;

public final class Transition<T> {

  private final long source;

  private final T label;

  private final long destination;

  Transition(long source, T label, long destination) {
    this.source = source;
    this.label = label;
    this.destination = destination;
  }

  Transition(long source, long destination) {
    this.source = source;
    this.label = null;
    this.destination = destination;
  }

  public long getSource() {
    return source;
  }

  public T getLabel() {
    return label;
  }

  public long getDestination() {
    return destination;
  }

  Transition<T> reversed() {
    return new Transition<>(destination, label, source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination, label);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Transition)) {
      return false;
    }
    Transition<?> t = (Transition<?>) obj;
    return source == t.source && destination == t.destination && Objects.equals(label, t.label);
  }
}
