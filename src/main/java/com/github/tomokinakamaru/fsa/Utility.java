package com.github.tomokinakamaru.fsa;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class Utility {

  private Utility() {}

  static <T> T pop(Collection<T> collection) {
    T item = collection.iterator().next();
    collection.remove(item);
    return item;
  }

  static <T> Set<T> difference(Collection<T> collection1, Collection<T> collection2) {
    Set<T> s = new LinkedHashSet<>(collection1);
    s.removeAll(collection2);
    return s;
  }

  static <T> Set<T> singleton(T item) {
    Set<T> s = new LinkedHashSet<>();
    s.add(item);
    return s;
  }

  static <T> boolean overlap(Collection<T> collection1, Collection<T> collection2) {
    return collection1.stream().anyMatch(collection2::contains);
  }
}
