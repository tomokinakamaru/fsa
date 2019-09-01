package com.github.tomokinakamaru.fsm;

import static com.github.tomokinakamaru.fsm.Utility.difference;
import static com.github.tomokinakamaru.fsm.Utility.overlap;
import static com.github.tomokinakamaru.fsm.Utility.pop;
import static com.github.tomokinakamaru.fsm.Utility.product;
import static com.github.tomokinakamaru.fsm.Utility.singletonMap;
import static com.github.tomokinakamaru.fsm.Utility.singletonSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class FSM<T> {

  private static long nextState = 0;

  private final Set<Long> initials = new HashSet<>();

  private final Set<Transition<T>> transitions = new HashSet<>();

  private final Set<Long> finals = new HashSet<>();

  private Set<Long> head = null;

  private FSM() {}

  public static <T> FSM<T> atom(T label) {
    long source = newState();
    long destination = newState();
    FSM<T> nfa = new FSM<>();
    nfa.initials.add(source);
    nfa.transitions.add(new Transition<>(source, label, destination));
    nfa.finals.add(destination);
    return nfa;
  }

  public static <T> FSM<T> union(Collection<FSM<T>> collection) {
    FSM<T> nfa = new FSM<>();
    for (FSM<T> m : collection) {
      nfa.initials.addAll(m.initials);
      nfa.transitions.addAll(m.transitions);
      nfa.finals.addAll(m.finals);
    }
    return nfa;
  }

  public static <T> FSM<T> concat(List<FSM<T>> collection) {
    FSM<T> nfa = new FSM<>();
    nfa.initials.addAll(collection.get(0).initials);
    nfa.finals.addAll(collection.get(collection.size() - 1).finals);
    collection.forEach(m -> nfa.transitions.addAll(m.transitions));

    for (int i = 0; i < collection.size() - 1; i++) {
      nfa.fuse(collection.get(i).finals, collection.get(i + 1).initials);
    }
    return nfa;
  }

  public Set<Long> getInitialStates() {
    return new HashSet<>(initials);
  }

  public Set<Transition<T>> getTransitions() {
    return new HashSet<>(transitions);
  }

  public Set<Long> getFinals() {
    return new HashSet<>(finals);
  }

  public void reset() {
    head = findEpsilonClosure(initials);
  }

  public void consume(T label) {
    if (head == null) {
      reset();
    }
    head = findEpsilonClosure(findDestinations(head, label));
  }

  public boolean isAccepting() {
    if (head == null) {
      reset();
    }
    return overlap(head, finals);
  }

  public FSM<T> minimumDeterminized() {
    return reversed().determinized().reversed().determinized();
  }

  public FSM<T> repeated() {
    FSM<T> nfa = new FSM<>();
    nfa.initials.addAll(initials);
    nfa.transitions.addAll(transitions);
    nfa.finals.addAll(finals);
    nfa.fuse(initials, finals);
    nfa.fuse(finals, initials);
    return nfa;
  }

  public FSM<T> reversed() {
    FSM<T> nfa = new FSM<>();
    nfa.initials.addAll(finals);
    nfa.finals.addAll(initials);
    transitions.stream().map(Transition::reversed).forEach(nfa.transitions::add);
    return nfa;
  }

  public FSM<T> determinized() {
    Set<Long> initial = findEpsilonClosure(initials);

    Set<Set<Long>> waits = singletonSet(initial);
    Map<Set<Long>, Long> stateMap = singletonMap(initial, newState());

    Set<T> labels = getNonNullLabels();
    Set<Transition<T>> transitions = new HashSet<>();
    while (!waits.isEmpty()) {
      Set<Long> sources = pop(waits);
      for (T label : labels) {
        Set<Long> destinations = findEpsilonClosure(findDestinations(sources, label));
        if (!destinations.isEmpty()) {
          if (!stateMap.containsKey(destinations)) {
            stateMap.put(destinations, newState());
            waits.add(destinations);
          }
          long source = stateMap.get(sources);
          long destination = stateMap.get(destinations);
          transitions.add(new Transition<>(source, label, destination));
        }
      }
    }

    FSM<T> nfa = new FSM<>();
    nfa.initials.add(stateMap.get(initial));
    nfa.transitions.addAll(transitions);
    for (Set<Long> states : stateMap.keySet()) {
      if (finals.stream().anyMatch(states::contains)) {
        nfa.finals.add(stateMap.get(states));
      }
    }
    return nfa;
  }

  private Set<Long> findEpsilonClosure(Set<Long> core) {
    Set<Long> closure = new HashSet<>(core);
    Set<Long> waits = new HashSet<>(core);
    while (!waits.isEmpty()) {
      Set<Long> destinations = findDestinations(pop(waits), null);
      waits.addAll(difference(destinations, closure));
      closure.addAll(destinations);
    }
    return closure;
  }

  private Set<Long> findDestinations(long source, T label) {
    return transitions
        .stream()
        .filter(t -> t.getSource() == source)
        .filter(t -> Objects.equals(t.getLabel(), label))
        .map(Transition::getDestination)
        .collect(Collectors.toSet());
  }

  private Set<Long> findDestinations(Set<Long> sources, T label) {
    return sources
        .stream()
        .map(s -> findDestinations(s, label))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private Set<T> getNonNullLabels() {
    return transitions
        .stream()
        .map(Transition::getLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private void fuse(Set<Long> sources, Set<Long> destinations) {
    product(sources, destinations, (s, d) -> transitions.add(new Transition<>(s, d)));
  }

  private static long newState() {
    nextState++;
    return nextState;
  }
}
