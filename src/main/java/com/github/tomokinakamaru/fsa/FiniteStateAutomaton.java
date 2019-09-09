package com.github.tomokinakamaru.fsa;

import static com.github.tomokinakamaru.fsa.Utility.difference;
import static com.github.tomokinakamaru.fsa.Utility.overlap;
import static com.github.tomokinakamaru.fsa.Utility.pop;
import static com.github.tomokinakamaru.fsa.Utility.singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FiniteStateAutomaton<T> {

  public final Set<State> initials = new LinkedHashSet<>();

  public final Set<Transition<T>> transitions = new LinkedHashSet<>();

  public final Set<State> finals = new LinkedHashSet<>();

  public Set<State> head = null;

  public FiniteStateAutomaton() {}

  public FiniteStateAutomaton(T symbol) {
    State s = new State();
    State d = new State();
    initials.add(s);
    finals.add(d);
    transitions.add(new Transition<>(s, symbol, d));
  }

  public Set<State> getStates() {
    Set<State> states = new LinkedHashSet<>(initials);
    for (Transition transition : transitions) {
      states.add(transition.source);
      states.add(transition.destination);
    }
    return states;
  }

  public Set<T> getSymbols() {
    return transitions
        .stream()
        .map(t -> t.symbol)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Set<Transition> getTransitionsFrom(State source) {
    return getTransitionsFrom(singleton(source));
  }

  public Set<Transition> getTransitionsFrom(Set<State> sources) {
    return transitions
        .stream()
        .filter(t -> sources.contains(t.source))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Set<Transition> getTransitionsTo(State destination) {
    return getTransitionsTo(singleton(destination));
  }

  public Set<Transition> getTransitionsTo(Set<State> destinations) {
    return transitions
        .stream()
        .filter(t -> destinations.contains(t.destination))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Set<State> getDestinations(State source, T symbol) {
    return getDestinations(singleton(source), symbol);
  }

  public Set<State> getDestinations(Set<State> sources, T symbol) {
    return getTransitionsFrom(sources)
        .stream()
        .filter(t -> Objects.equals(symbol, t.symbol))
        .map(t -> t.destination)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Set<State> getEpsilonClosure(State state) {
    return getEpsilonClosure(singleton(state));
  }

  public Set<State> getEpsilonClosure(Set<State> core) {
    Set<State> closure = new LinkedHashSet<>(core);
    Set<State> waits = new LinkedHashSet<>(core);
    while (!waits.isEmpty()) {
      Set<State> destinations = getDestinations(pop(waits), null);
      waits.addAll(difference(destinations, closure));
      closure.addAll(destinations);
    }
    return closure;
  }

  public Set<State> getReverseEpsilonClosure(State core) {
    return getReverseEpsilonClosure(singleton(core));
  }

  public Set<State> getReverseEpsilonClosure(Set<State> core) {
    Set<State> reverseClosure = new LinkedHashSet<>();

    List<State> queue = new ArrayList<>(initials);
    Set<State> queuedState = new HashSet<>(initials);

    while (!queue.isEmpty()) {
      State source = pop(queue);
      Set<State> closure = getEpsilonClosure(source);
      if (overlap(closure, core)) {
        reverseClosure.addAll(closure);
      }
      for (Transition transition : getTransitionsFrom(source)) {
        if (!queuedState.contains(transition.destination)) {
          queue.add(transition.destination);
          queuedState.add(transition.destination);
        }
      }
    }

    return reverseClosure;
  }

  public void reset() {
    head = getEpsilonClosure(initials);
  }

  public void consume(T symbol) {
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

  public void traverse(Traverser<T> traverser) {
    List<State> queue = new ArrayList<>(initials);
    Set<State> queuedStates = new HashSet<>(initials);

    while (!queue.isEmpty()) {
      State source = queue.remove(0);
      traverser.traverse(source, this);
      for (Transition transition : transitions) {
        if (!queuedStates.contains(transition.destination)) {
          queue.add(transition.destination);
          queuedStates.add(transition.destination);
        }
      }
    }
  }

  public FiniteStateAutomaton<T> and(FiniteStateAutomaton<T> a2) {
    FiniteStateAutomaton<T> a1 = copy();
    a2 = a2.copy();
    FiniteStateAutomaton<T> a = new FiniteStateAutomaton<>();
    a.initials.addAll(a1.initials);
    a.transitions.addAll(a1.transitions);
    a.transitions.addAll(a2.transitions);
    a.fuse(a1.finals, a2.initials);
    a.finals.addAll(a2.finals);
    return a;
  }

  public FiniteStateAutomaton<T> or(FiniteStateAutomaton<T> a2) {
    FiniteStateAutomaton<T> automaton = copy();
    a2 = a2.copy();
    automaton.initials.addAll(a2.initials);
    automaton.transitions.addAll(a2.transitions);
    automaton.finals.addAll(a2.finals);
    return automaton;
  }

  public FiniteStateAutomaton<T> repeated() {
    FiniteStateAutomaton<T> automaton = copy();
    automaton.fuse(automaton.initials, automaton.finals);
    automaton.fuse(automaton.finals, automaton.initials);
    return automaton;
  }

  public FiniteStateAutomaton<T> minimumDeterminized() {
    return reversed().determinized().reversed().determinized();
  }

  public FiniteStateAutomaton<T> reversed() {
    FiniteStateAutomaton<T> automaton = new FiniteStateAutomaton<>();
    FiniteStateAutomaton<T> copied = copy();

    automaton.initials.addAll(copied.finals);
    automaton.finals.addAll(copied.initials);
    for (Transition<T> t : copied.transitions) {
      automaton.transitions.add(new Transition<>(t.destination, t.symbol, t.source));
    }

    return automaton;
  }

  public FiniteStateAutomaton<T> determinized() {
    FiniteStateAutomaton<T> automaton = new FiniteStateAutomaton<>();
    Set<Set<State>> waits = new LinkedHashSet<>();
    Map<Set<State>, State> stateMap = new LinkedHashMap<>();

    Set<State> initial = getEpsilonClosure(initials);
    if (!initial.isEmpty()) {
      waits.add(initial);
      stateMap.put(initial, new State());
      automaton.initials.add(stateMap.get(initial));
    }

    Set<T> symbols = getSymbols();
    while (!waits.isEmpty()) {
      Set<State> src = waits.iterator().next();
      waits.remove(src);

      for (T symbol : symbols) {
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
        automaton.transitions.add(new Transition<>(s, symbol, d));
      }
    }

    for (Set<State> states : stateMap.keySet()) {
      if (finals.stream().anyMatch(states::contains)) {
        automaton.finals.add(stateMap.get(states));
      }
    }

    return automaton;
  }

  public FiniteStateAutomaton<T> copy() {
    Map<State, State> map = new HashMap<>();
    for (State state : getStates()) {
      map.put(state, new State());
    }

    FiniteStateAutomaton<T> automaton = new FiniteStateAutomaton<>();

    for (State state : initials) {
      automaton.initials.add(map.get(state));
    }

    for (Transition<T> transition : transitions) {
      State source = map.get(transition.source);
      State destination = map.get(transition.destination);
      automaton.transitions.add(new Transition<>(source, transition.symbol, destination));
    }

    for (State state : finals) {
      automaton.finals.add(map.get(state));
    }

    return automaton;
  }

  public void fuse(State source, State destination) {
    fuse(singleton(source), singleton(destination));
  }

  public void fuse(Set<State> src, Set<State> dst) {
    for (State s : src) {
      for (State d : dst) {
        transitions.add(new Transition<>(s, null, d));
      }
    }
  }
}
