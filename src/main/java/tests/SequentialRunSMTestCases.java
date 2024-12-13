package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gentrifiedApps.statemachineftc.SequentialRunSM;
import org.gentrifiedApps.statemachineftc.StateMachine;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class SequentialRunSMTestCases {

    private SequentialRunSM<States> sqsm;

    enum States {
        STATE1,
        STATE2,
        STATE3,
        STOP,
    }

    List<Boolean> booleans = Arrays.asList(false, false, true, false, true, false, true, false, true, false);

    @Test
    void testBasic() {
        System.out.println("Testing basic state machine");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        AtomicInteger count = new AtomicInteger();
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        assertTrue(srsmb.update());
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STATE3, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STOP, srsmb.getCurrentState());
        assertFalse(srsmb.isRunning());

        System.out.println("States updated successfully");
    }
    // test transition with a holdup
    @Test
    void testTransition(){
        System.out.println("Testing transition with holdup");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return false;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        assertTrue(srsmb.update());
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertNotEquals(States.STATE3, srsmb.getCurrentState());
        assertTrue(srsmb.isRunning());
        assertNotEquals(States.STOP, srsmb.getCurrentState());
        assertTrue(srsmb.update());

        System.out.println("States updated successfully");
    }
    @Test
    void testStopRunning(){
        System.out.println("Testing stopRunning");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        assertTrue(srsmb.update());
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STATE3, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STOP, srsmb.getCurrentState());
        assertFalse(srsmb.isRunning());
    }
    @Test
    void testDuplicateState(){
        System.out.println("Testing duplicate state");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        assertThrows(IllegalArgumentException.class, () -> {builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);
        });
    }
    // test that they are done in sequence and not in parallel
    @Test
    void testSequence(){
        System.out.println("Testing sequence");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        final boolean[] commandExecuted = {false, false};
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
            commandExecuted[0] = true;
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
            commandExecuted[1] = true;
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
            commandExecuted[0] = false;
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            commandExecuted[1] = false;
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        assertTrue(commandExecuted[0]);
        assertFalse(commandExecuted[1]);
        assertTrue(srsmb.update());
        assertTrue(commandExecuted[0]);
        assertTrue(commandExecuted[1]);
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertFalse(commandExecuted[0]);
        assertTrue(commandExecuted[1]);
        assertEquals(States.STATE3, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertFalse(commandExecuted[0]);
        assertFalse(commandExecuted[1]);
        assertEquals(States.STOP, srsmb.getCurrentState());
        assertFalse(srsmb.isRunning());
    }
    @Test
    void testEmptyStates(){
        System.out.println("Testing empty states");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        assertThrows(IllegalArgumentException.class, () -> {builder.build();});
    }
    // test stop function
    @Test
    void testStop(){
        System.out.println("Testing stop");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        AtomicInteger count = new AtomicInteger();
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        srsmb.stop();
        assertFalse(srsmb.isRunning());
    }
    // comprehensively test all the functionality
    @Test
    void testComprehensive() {
        System.out.println("Testing comprehensive");
        SequentialRunSM.Builder<States> builder = new SequentialRunSM.Builder<>();
        AtomicInteger count = new AtomicInteger();
        builder.state(States.STATE1).onEnter(States.STATE1, () -> {
            System.out.println("Entering STATE1");
        }).transition(States.STATE1, () -> {
            return true;
        }).state(States.STATE2).onEnter(States.STATE2, () -> {
            System.out.println("Entering STATE2");
        }).transition(States.STATE2, () -> {
            System.out.println("Transitioning from STATE2 to STATE3");
            return true;
        }).state(States.STATE3).onEnter(States.STATE3, () -> {
            System.out.println("Entering STATE3");
        }).transition(States.STATE3, () -> {
            System.out.println("Transitioning from STATE3 to STOP");
            return true;
        }).stopRunning(States.STOP);

        SequentialRunSM<States> srsmb = builder.build();
        srsmb.start();
        assertTrue(srsmb.isRunning());
        assertTrue(srsmb.update());
        assertEquals(States.STATE2, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STATE3, srsmb.getCurrentState());
        assertTrue(srsmb.update());
        assertEquals(States.STOP, srsmb.getCurrentState());
        assertFalse(srsmb.isRunning());

        System.out.println("Comprehensive test passed");
    }
}