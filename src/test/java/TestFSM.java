import static com.github.tomokinakamaru.fsm.FSM.atom;
import static com.github.tomokinakamaru.fsm.FSM.concat;
import static com.github.tomokinakamaru.fsm.FSM.union;
import static java.util.Arrays.asList;

import com.github.tomokinakamaru.fsm.FSM;
import org.junit.jupiter.api.Test;

final class TestFSM {

  @Test
  void testAtom() {
    FSM<Integer> m = atom(1);
    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testUnion() {
    FSM<Integer> m = union(asList(atom(1), atom(2)));

    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.reset();
    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testConcat() {
    FSM<Integer> m = concat(asList(atom(1), atom(2)));

    assert !m.isAccepting();

    m.consume(1);
    assert !m.isAccepting();

    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testRepeat() {
    FSM<Integer> m = atom(1).repeated();

    assert m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testReverse() {
    FSM<Integer> m = concat(asList(atom(1), atom(2))).reversed();

    assert !m.isAccepting();

    m.consume(2);
    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();
  }

  @Test
  void testDeterminized() {
    FSM<Integer> m = union(asList(atom(1), atom(2))).determinized();

    assert !m.isAccepting();

    m.consume(1);
    assert m.isAccepting();

    m.reset();
    m.consume(2);
    assert m.isAccepting();
  }

  @Test
  void testMinimumDeterminized() {
    FSM<Integer> m1 = union(asList(atom(1), atom(2)));
    FSM<Integer> m2 = union(asList(atom(3), atom(4)));
    FSM<Integer> m = concat(asList(m1, m2)).minimumDeterminized();

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
