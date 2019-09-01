package com.github.tomokinakamaru.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

final class Utility {

  private Utility() {}

  static <T> void product(Set<T> s1, Set<T> s2, BiConsumer<T, T> consumer) {
    s1.forEach(a -> s2.forEach(b -> consumer.accept(a, b)));
  }

  static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
    Set<T> s3 = new HashSet<>(s1);
    s3.removeAll(s2);
    return s3;
  }

  static <T> boolean overlap(Set<T> s1, Set<T> s2) {
    return s1.stream().anyMatch(s2::contains);
  }

  static <T, S> Map<T, S> singletonMap(T key, S value) {
    Map<T, S> m = new HashMap<>();
    m.put(key, value);
    return m;
  }

  static <T> Set<T> singletonSet(T x) {
    Set<T> s = new HashSet<>();
    s.add(x);
    return s;
  }

  static <T> T pop(Set<T> s) {
    T x = s.iterator().next();
    s.remove(x);
    return x;
  }
}
