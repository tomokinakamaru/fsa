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

public abstract class Automaton<S, T extends Transition<S>, A extends Automaton<S, T, A>> {

  protected abstract T newTransition(State source, S symbol, State destination);

  protected abstract A newAutomaton();

  public final Set<State> initials = new LinkedHashSet<>();

  public final Set<T> transitions = new LinkedHashSet<>();

  public final Set<State> finals = new LinkedHashSet<>();

  public Set<State> head = null;

  protected Automaton() {}

  protected Automaton(S symbol) {
    State s = new State();
    State d = new State();
    initials.add(s);
    finals.add(d);
    transitions.add(newTransition(s, symbol, d));
  }

  public void reset() {
    head = getEpsilonClosure(initials);
  }

  public void consume(S symbol) {
    if (head == null) {
      reset();
    }
    head = getEpsilonClosure(getDestinations(getEpsilonClosure(head), symbol));
  }

  public boolean isAccepting() {
    if (head == null) {
      reset();
    }
    return overlap(getEpsilonClosure(head), finals);
  }

  public final Set<State> getStates() {
    Set<State> states = new LinkedHashSet<>(initials);
    for (T transition : transitions) {
      states.add(transition.source);
      states.add(transition.destination);
    }
    return states;
  }

  public final Set<T> getTransitionsFrom(State source) {
    return getTransitionsFrom(singleton(source));
  }

  public final Set<T> getTransitionsFrom(Set<State> sources) {
    return transitions
        .stream()
        .filter(t -> sources.contains(t.source))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public final Set<T> getTransitionsTo(State destination) {
    return getTransitionsTo(singleton(destination));
  }

  public final Set<T> getTransitionsTo(Set<State> destinations) {
    return transitions
        .stream()
        .filter(t -> destinations.contains(t.destination))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public final Set<State> getEpsilonClosure(Set<State> core) {
    Set<State> closure = new LinkedHashSet<>(core);
    Set<State> waits = new LinkedHashSet<>(core);
    while (!waits.isEmpty()) {
      Set<State> destinations = getDestinations(singleton(pop(waits)), null);
      waits.addAll(difference(destinations, closure));
      closure.addAll(destinations);
    }
    return closure;
  }

  public final Set<State> getReverseEpsilonClosure(Set<State> core) {
    Set<State> reverseClosure = new LinkedHashSet<>();

    Set<State> queue = new LinkedHashSet<>(initials);
    Set<State> queuedState = new LinkedHashSet<>(initials);

    while (!queue.isEmpty()) {
      State source = pop(queue);
      Set<State> closure = getEpsilonClosure(singleton(source));
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
    Set<Set<State>> waits = new LinkedHashSet<>();
    Map<Set<State>, State> stateMap = new LinkedHashMap<>();

    Set<State> initial = getEpsilonClosure(initials);
    if (!initial.isEmpty()) {
      waits.add(initial);
      stateMap.put(initial, new State());
      automaton.initials.add(stateMap.get(initial));
    }

    Set<S> symbols =
        transitions
            .stream()
            .map(t -> t.symbol)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    while (!waits.isEmpty()) {
      Set<State> src = waits.iterator().next();
      waits.remove(src);

      for (S symbol : symbols) {
        Set<State> dst = getEpsilonClosure(getDestinations(src, symbol));

        if (dst.isEmpty()) {
          continue;
        }

        if (!stateMap.containsKey(dst)) {
          stateMap.put(dst, new State());
          waits.add(dst);
        }

        State s = stateMap.get(src);
        State d = stateMap.get(dst);
        automaton.transitions.add(newTransition(s, symbol, d));
      }
    }

    for (Set<State> states : stateMap.keySet()) {
      if (finals.stream().anyMatch(states::contains)) {
        automaton.finals.add(stateMap.get(states));
      }
    }

    return automaton;
  }

  public final A copy() {
    Map<State, State> map = new HashMap<>();
    for (State state : getStates()) {
      map.put(state, new State());
    }

    A automaton = newAutomaton();

    for (State state : initials) {
      automaton.initials.add(map.get(state));
    }

    for (T transition : transitions) {
      State source = map.get(transition.source);
      State destination = map.get(transition.destination);
      automaton.transitions.add(newTransition(source, transition.symbol, destination));
    }

    for (State state : finals) {
      automaton.finals.add(map.get(state));
    }

    return automaton;
  }

  final void fuse(Set<State> src, Set<State> dst) {
    for (State s : src) {
      for (State d : dst) {
        transitions.add(newTransition(s, null, d));
      }
    }
  }

  private Set<State> getDestinations(Set<State> sources, S symbol) {
    return getTransitionsFrom(sources)
        .stream()
        .filter(t -> Objects.equals(symbol, t.symbol))
        .map(t -> t.destination)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
