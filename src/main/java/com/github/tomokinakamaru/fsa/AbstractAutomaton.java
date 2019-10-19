package com.github.tomokinakamaru.fsa;

import static com.github.tomokinakamaru.fsa.Utility.difference;
import static com.github.tomokinakamaru.fsa.Utility.overlap;
import static com.github.tomokinakamaru.fsa.Utility.pop;
import static com.github.tomokinakamaru.fsa.Utility.singleton;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAutomaton<
    Q extends StateInterface,
    S,
    T extends AbstractTransition<Q, S>,
    A extends AbstractAutomaton<Q, S, T, A>> {

  protected abstract Q newState();

  protected abstract T newTransition(Q source, S symbol, Q destination);

  protected abstract A newAutomaton();

  public final Set<Q> initials = new LinkedHashSet<>();

  public final Set<T> transitions = new LinkedHashSet<>();

  public final Set<Q> finals = new LinkedHashSet<>();

  public Set<Q> head = null;

  protected AbstractAutomaton() {}

  protected AbstractAutomaton(S symbol) {
    Q s = newState();
    Q d = newState();
    initials.add(s);
    finals.add(d);
    transitions.add(newTransition(s, symbol, d));
  }

  public void reset() {
    head = getEpsilonClosureFrom(initials);
  }

  public void consume(S symbol) {
    if (head == null) {
      reset();
    }
    head = getEpsilonClosureFrom(getDestinations(getEpsilonClosureFrom(head), symbol));
  }

  public boolean isAccepting() {
    if (head == null) {
      reset();
    }
    return overlap(getEpsilonClosureFrom(head), finals);
  }

  public final Set<Q> getStates() {
    Set<Q> states = new LinkedHashSet<>(initials);
    for (T transition : transitions) {
      states.add(transition.source);
      states.add(transition.destination);
    }
    return states;
  }

  public final Set<T> getTransitionsFrom(Q source) {
    return getTransitionsFrom(singleton(source));
  }

  public final Set<T> getTransitionsFrom(Set<Q> sources) {
    return transitions
        .stream()
        .filter(t -> sources.contains(t.source))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public final Set<T> getTransitionsTo(Q destination) {
    return getTransitionsTo(singleton(destination));
  }

  public final Set<T> getTransitionsTo(Set<Q> destinations) {
    return transitions
        .stream()
        .filter(t -> destinations.contains(t.destination))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public final Set<Q> getEpsilonClosureFrom(Q core) {
    return getEpsilonClosureFrom(singleton(core));
  }

  public final Set<Q> getEpsilonClosureFrom(Set<Q> core) {
    Set<Q> closure = new LinkedHashSet<>(core);
    Set<Q> waits = new LinkedHashSet<>(core);
    while (!waits.isEmpty()) {
      Set<Q> destinations = getDestinations(singleton(pop(waits)), null);
      waits.addAll(difference(destinations, closure));
      closure.addAll(destinations);
    }
    return closure;
  }

  public final Set<Q> getEpsilonClosureTo(Q core) {
    return getEpsilonClosureTo(singleton(core));
  }

  public final Set<Q> getEpsilonClosureTo(Set<Q> core) {
    Set<Q> reverseClosure = new LinkedHashSet<>();

    Set<Q> queue = new LinkedHashSet<>(initials);
    Set<Q> queuedState = new LinkedHashSet<>(initials);

    while (!queue.isEmpty()) {
      Q source = pop(queue);
      Set<Q> closure = getEpsilonClosureFrom(source);
      if (overlap(closure, core)) {
        reverseClosure.addAll(closure);
      }
      for (T transition : getTransitionsFrom(source)) {
        if (!queuedState.contains(transition.destination)) {
          queue.add(transition.destination);
          queuedState.add(transition.destination);
        }
      }
    }

    return reverseClosure;
  }

  public final A and(A a2) {
    A a1 = copy();
    a2 = a2.copy();
    A a = newAutomaton();
    a.initials.addAll(a1.initials);
    a.transitions.addAll(a1.transitions);
    a.transitions.addAll(a2.transitions);
    a.fuse(a1.finals, a2.initials);
    a.finals.addAll(a2.finals);
    return a;
  }

  public final A or(A a2) {
    A automaton = copy();
    a2 = a2.copy();
    automaton.initials.addAll(a2.initials);
    automaton.transitions.addAll(a2.transitions);
    automaton.finals.addAll(a2.finals);
    return automaton;
  }

  public final A repeated() {
    A automaton = copy();
    automaton.fuse(automaton.initials, automaton.finals);
    automaton.fuse(automaton.finals, automaton.initials);
    return automaton;
  }

  public final A reversed() {
    A automaton = newAutomaton();
    A copied = copy();

    automaton.initials.addAll(copied.finals);
    automaton.finals.addAll(copied.initials);
    for (T t : copied.transitions) {
      automaton.transitions.add(newTransition(t.destination, t.symbol, t.source));
    }

    return automaton;
  }

  public final A determinized() {
    A automaton = newAutomaton();
    Set<Set<Q>> waits = new LinkedHashSet<>();
    Map<Set<Q>, Q> stateMap = new LinkedHashMap<>();

    Set<Q> initial = getEpsilonClosureFrom(initials);
    if (!initial.isEmpty()) {
      waits.add(initial);
      stateMap.put(initial, newState(initial));
      automaton.initials.add(stateMap.get(initial));
    }

    Set<S> symbols =
        transitions
            .stream()
            .map(t -> t.symbol)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    while (!waits.isEmpty()) {
      Set<Q> src = waits.iterator().next();
      waits.remove(src);

      for (S symbol : symbols) {
        Set<Q> dst = getEpsilonClosureFrom(getDestinations(src, symbol));

        if (dst.isEmpty()) {
          continue;
        }

        if (!stateMap.containsKey(dst)) {
          stateMap.put(dst, newState(dst));
          waits.add(dst);
        }

        Q s = stateMap.get(src);
        Q d = stateMap.get(dst);
        automaton.transitions.add(newTransition(s, symbol, d));
      }
    }

    for (Set<Q> states : stateMap.keySet()) {
      if (finals.stream().anyMatch(states::contains)) {
        automaton.finals.add(stateMap.get(states));
      }
    }

    return automaton;
  }

  public final A copy() {
    Map<Q, Q> map = new HashMap<>();
    for (Q state : getStates()) {
      map.put(state, newState(state));
    }

    A automaton = newAutomaton();

    for (Q state : initials) {
      automaton.initials.add(map.get(state));
    }

    for (T transition : transitions) {
      Q source = map.get(transition.source);
      Q destination = map.get(transition.destination);
      automaton.transitions.add(newTransition(source, transition.symbol, destination));
    }

    for (Q state : finals) {
      automaton.finals.add(map.get(state));
    }

    return automaton;
  }

  protected Q newState(Set<Q> states) {
    return newState();
  }

  protected Q newState(Q state) {
    return newState();
  }

  final void fuse(Set<Q> src, Set<Q> dst) {
    for (Q s : src) {
      for (Q d : dst) {
        transitions.add(newTransition(s, null, d));
      }
    }
  }

  private Set<Q> getDestinations(Set<Q> sources, S symbol) {
    return getTransitionsFrom(sources)
        .stream()
        .filter(t -> Objects.equals(symbol, t.symbol))
        .map(t -> t.destination)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
