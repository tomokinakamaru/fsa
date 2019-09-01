import static com.github.tomokinakamaru.nfa.NFA.atom;
import static com.github.tomokinakamaru.nfa.NFA.concat;
import static com.github.tomokinakamaru.nfa.NFA.union;
import static java.util.Arrays.asList;

import com.github.tomokinakamaru.nfa.NFA;
import org.junit.jupiter.api.Test;

final class TestNFA {

  @Test
  void testAtom() {
    NFA<Integer> m = atom(1);
    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testUnion() {
    NFA<Integer> m = union(asList(atom(1), atom(2)));

    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.reset();
    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testConcat() {
    NFA<Integer> m = concat(asList(atom(1), atom(2)));

    assert !m.isAccepting();

    m.consume(1);
    assert !m.isAccepting();

    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testRepeat() {
    NFA<Integer> m = atom(1).repeated();

    assert m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testReverse() {
    NFA<Integer> m = concat(asList(atom(1), atom(2))).reversed();

    assert !m.isAccepting();

    m.consume(2);
    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testDeterminized() {
    NFA<Integer> m = union(asList(atom(1), atom(2))).determinized();

    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.reset();
    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testMinimumDeterminized() {
    NFA<Integer> m1 = union(asList(atom(1), atom(2)));
    NFA<Integer> m2 = union(asList(atom(3), atom(4)));
    NFA<Integer> m = concat(asList(m1, m2)).minimumDeterminized();

    assert !m.isAccepting();

    m.consume(1);
    assert !m.isAccepting();

    m.consume(3);
    assert m.isAccepting();

    assert m.getInitialStates().size() == 1;
    assert m.getFinals().size() == 1;
    assert m.getTransitions().size() == 4;
  }
}
