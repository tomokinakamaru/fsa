package com.github.tomokinakamaru.fsa;

import java.util.LinkedHashSet;
import java.util.Set;

final class Utility {

  private Utility() {}

  static <T> T pop(Set<T> set) {
    T item = set.iterator().next();
    set.remove(item);
    return item;
  }

  static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
    Set<T> s = new LinkedHashSet<>(set1);
    s.removeAll(set2);
    return s;
  }

  static <T> Set<T> singleton(T item) {
    Set<T> s = new LinkedHashSet<>();
    s.add(item);
    return s;
  }

  static <T> boolean overlap(Set<T> set1, Set<T> set2) {
    return set1.stream().anyMatch(set2::contains);
  }
}
