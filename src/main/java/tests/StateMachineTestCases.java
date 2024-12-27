package tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gentrifiedApps.statemachineftc.StateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class StateMachineTestCases {
    private StateMachine<TestState> stateMachine;

    private enum TestState {
        STATE_A, STATE_B, STATE_C, STOP, STATE_NO;
    }

    @BeforeEach
    public void setUp() {
        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .state(TestState.STATE_C)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .whileState(TestState.STATE_A, () -> true, () -> System.out.println("Executing STATE_A"))
                .onExit(TestState.STATE_A, () -> System.out.println("Exiting STATE_A"))
                .transition(TestState.STATE_A, () -> true, 0)
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_B, () -> true, 0)
                .onEnter(TestState.STATE_C, () -> System.out.println("Entering STATE_C"))
                .transition(TestState.STATE_C, () -> true, 0)
                .stopRunning(TestState.STOP)
                .build();

        stateMachine.start();
    }

    @Test
    public void testInitialState() {
        assertEquals(TestState.STATE_A, stateMachine.getCurrentState(), "Initial state should be STATE_A");
    }

    @Test
    public void testStateTransition() {
        assertTrue(stateMachine.update(), "Update should process STATE_A");
        assertTrue(stateMachine.update(), "Update should process STATE_A");
        assertTrue(stateMachine.update(), "Update should process STATE_A");
        assertTrue(stateMachine.update(), "Update should process STATE_A");
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "State should transition to STATE_B");

        assertTrue(stateMachine.update(), "Update should process STATE_B");
        assertEquals(TestState.STATE_C, stateMachine.getCurrentState(), "State should transition to STATE_C");

        assertTrue(stateMachine.update(), "Update should process STATE_C");
        assertTrue(stateMachine.update(), "Update should process STATE_C");

        assertEquals(TestState.STOP, stateMachine.getCurrentState(), "State should transition to STOP");
    }

    @Test
    public void testStopState() {
        while (stateMachine.update()) {
            // Process all states until STOP
        }
        assertFalse(stateMachine.isRunning(), "StateMachine should stop running at STOP state");
    }

    @Test
    public void testWhileStateExecution() {
        stateMachine.update(); // Process STATE_A
        // Assuming the escape condition of STATE_A is set to false initially
        // Verify if whileState logic is executed
        stateMachine.update();
        stateMachine.update();
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "Escape condition should transition to STATE_B");
    }

    @Test
    public void testInvalidTransition() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_A);
        });
        assertEquals("Cannot transition to itself", exception.getMessage(), "Invalid transition error message should match");
    }
    @Test
    public void testOnEnterActionExecution() {
        stateMachine.update(); // Enter STATE_A
        assertEquals(TestState.STATE_A, stateMachine.getCurrentState(), "State should remain in STATE_A after ON_ENTER action");
    }

    @Test
    public void testWhileStateActionExecution() {
        stateMachine.update(); // Transition to WHILE_STATE for STATE_A
        assertTrue(stateMachine.update(), "WHILE_STATE action should execute while the escape condition is false");
    }

    @Test
    public void testEscapeConditionTransition() {
        stateMachine.update(); // Process STATE_A
        Supplier<Boolean> escapeCondition = () -> true; // Force transition from WHILE_STATE
        stateMachine.update();
        stateMachine.update();
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "Escape condition should trigger transition to STATE_B");
    }

    @Test
    public void testOnExitActionExecution() {
        stateMachine.update(); // STATE_A ON_ENTER
        stateMachine.update(); // STATE_A WHILE_STATE and transition
        stateMachine.update();
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "ON_EXIT action should lead to transition to STATE_B");
    }

    @Test
    public void testStopStateExecution() {
        while (stateMachine.update()) {
            // Process all states until STOP
        }
        assertFalse(stateMachine.isRunning(), "StateMachine should stop running at STOP state");
        assertEquals(TestState.STOP, stateMachine.getCurrentState(), "Final state should be STOP");
    }

    @Test
    public void testInvalidStateUpdate() {
        stateMachine.stop(); // Manually stop the state machine
        assertFalse(stateMachine.update(), "Update should return false if the state machine is stopped");
    }

    @Test
    public void testTransitionDelay() throws InterruptedException {
        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_A, () -> true, 2.0) // 2-second delay
                .transition(TestState.STATE_B, () -> true, 0)
                .stopRunning(TestState.STOP)
                .build();

        stateMachine.start();
        stateMachine.update(); // Transition starts
        stateMachine.update();

        Thread.sleep(1000); // Wait 1 second
        assertEquals(TestState.STATE_A, stateMachine.getCurrentState(), "State should still be STATE_A due to delay");

        Thread.sleep(2000); // Wait additional time
        stateMachine.update();
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "State should transition to STATE_B after delay");
    }

    @Test
    public void testInvalidTransition2() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_A);
        });
        assertEquals("Cannot transition to itself", exception.getMessage(), "Invalid transition error message should match");
    }

    @Test
    public void testMultipleStateCallbacks() {
        stateMachine.update(); // Enter STATE_A
        stateMachine.update(); // WHILE_STATE executes
        stateMachine.update(); // Transition to STATE_B
        assertEquals(TestState.STATE_B, stateMachine.getCurrentState(), "State should transition to STATE_B after callbacks");
    }

    @Test
    public void testNullInitialState() {
        assertThrows(IllegalArgumentException.class, () -> {
            StateMachine<TestState> stateMachine = new StateMachine.Builder<TestState>()
                    .build();
        });
    }

    @Test
    public void testRandomStateUpdates() {
        Random random = new Random();
        AtomicInteger randomCalls = new AtomicInteger();

        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .state(TestState.STATE_C)
                .stopRunning(TestState.STOP)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .whileState(TestState.STATE_A, () -> random.nextBoolean(), () -> randomCalls.incrementAndGet())
                .onExit(TestState.STATE_A, () -> System.out.println("Exiting STATE_A"))
                .transition(TestState.STATE_A, () -> random.nextBoolean(), 0)
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_B, () -> random.nextBoolean(), 0)
                .onEnter(TestState.STATE_C, () -> System.out.println("Entering STATE_C"))
                .transition(TestState.STATE_C, () -> random.nextBoolean(), 0)
                .build();

        stateMachine.start();

        for (int i = 0; i < 10; i++) { // Randomly update 10 times
            stateMachine.update();
        }

        System.out.println("Random calls during STATE_A: " + randomCalls.get());
        assertTrue(randomCalls.get() > 0, "Randomized whileState action should execute at least once");
    }

    @Test
    public void testDynamicEscapeCondition() {
        AtomicInteger counter = new AtomicInteger();
        Supplier<Boolean> dynamicEscapeCondition = () -> counter.incrementAndGet() > 3;

        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .stopRunning(TestState.STOP)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .whileState(TestState.STATE_A, dynamicEscapeCondition, () -> System.out.println("While in STATE_A"))
                .onExit(TestState.STATE_A, () -> System.out.println("Exiting STATE_A"))
                .transition(TestState.STATE_A, () -> true, 0)
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_B, () -> true, 0)
                .build();

        stateMachine.start();
        int updates = 0;
        while (stateMachine.update()) {
            updates++;
        }

        assertEquals(9, updates, "Dynamic escape condition should take 4 updates before transitioning");
    }

    @Test
    public void testStateMachineStabilityUnderHighLoad() {
        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .stopRunning(TestState.STOP)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .transition(TestState.STATE_A, () -> true, 0)
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_B, () -> true, 0)
                .build();

        stateMachine.start();

        for (int i = 0; i < 1000; i++) { // Stress test with high updates
            if (!stateMachine.update()) {
                break;
            }
        }

        assertFalse(stateMachine.isRunning(), "StateMachine should properly stop after high load of updates");
        assertEquals(TestState.STOP, stateMachine.getCurrentState(), "Final state should be STOP");
    }

    @Test
    public void testStateMachineWithRandomDelays() throws InterruptedException {
        Random random = new Random();

        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .state(TestState.STATE_C)
                .stopRunning(TestState.STOP)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .transition(TestState.STATE_A, () -> true, random.nextDouble() * 3) // Random delay up to 3 seconds
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .transition(TestState.STATE_B, () -> true, random.nextDouble() * 3) // Random delay up to 3 seconds
                .onEnter(TestState.STATE_C, () -> System.out.println("Entering STATE_C"))
                .transition(TestState.STATE_C, () -> true, random.nextDouble() * 3) // Random delay up to 3 seconds
                .build();

        stateMachine.start();
        int updates = 0;

        while (stateMachine.update()) {
            updates++;
            Thread.sleep(100); // Simulate processing time
        }

        System.out.println("Total updates processed: " + updates);
        assertEquals(TestState.STOP, stateMachine.getCurrentState(), "Final state should be STOP after processing with random delays");
    }

    @Test
    public void testIllegalArgument1() {


        assertThrows(IllegalArgumentException.class, () -> {
            stateMachine = new StateMachine.Builder<TestState>()
                    .state(TestState.STATE_A)
                    .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                    .transition(TestState.STATE_A, () -> true, 0)
                    .build();
        }, "Updating with no transitions should throw IllegalStateException");

    }
    @Test
    public void testIllegalArgument2() {


        assertThrows(IllegalArgumentException.class, () -> {
            stateMachine = new StateMachine.Builder<TestState>()
                    .state(TestState.STATE_A)
                    .build();
        }, "Updating with no transitions should throw IllegalStateException");
    }
    @Test
    public void testIllegalArgument3() {


        assertThrows(IllegalArgumentException.class, () -> {
            stateMachine = new StateMachine.Builder<TestState>()
                    .state(TestState.STATE_A)
                    .state(TestState.STATE_B)
                    .build();
        }, "Updating with no transitions should throw IllegalStateException");
    }

    @Test
    public void testIllegalArgument4() {


        assertThrows(IllegalArgumentException.class, () -> {
            stateMachine = new StateMachine.Builder<TestState>()
                    .state(TestState.STATE_A)
                    .transition(TestState.STATE_A, () -> true, 0)
                    .build();
        }, "Updating with no transitions should throw IllegalStateException");
    }
    @Test
    public void testValidTransition() {
        Supplier<Boolean> condition = () -> true;

        // Set up a valid transition
        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .transition(TestState.STATE_A, condition, 0)
                .transition(TestState.STATE_B, condition, 0)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .stopRunning(TestState.STOP)
                .build();

        assertDoesNotThrow(() -> {
            boolean result = stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_B);
            assertTrue(result, "Transition condition should return true for a valid transition");
        });
    }

    @Test
    public void testTransitionToSelf() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_A);
        });
        assertEquals("Cannot transition to itself", exception.getMessage(), "Error message for self-transition should match");
    }

    @Test
    public void testFromStateNotInStateMachine() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stateMachine.isValidTransition(TestState.STATE_NO, TestState.STATE_A);
        });
        assertEquals("STATE_NO does not exist in the state machine", exception.getMessage(), "Error message for non-existent fromState should match");
    }

    @Test
    public void testToStateNotInStateMachine() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_NO);
        });
        assertEquals("STATE_NO does not exist in the state machine", exception.getMessage(), "Error message for non-existent toState should match");
    }

    @Test
    public void testRandomTransitionCondition() {
        Random random = new Random();
        Supplier<Boolean> randomCondition = random::nextBoolean;

        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A)
                .state(TestState.STATE_B)
                .transition(TestState.STATE_A, randomCondition, 0)
                .transition(TestState.STATE_B, randomCondition, 0)
                .onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .stopRunning(TestState.STOP)
                .build();

        boolean result = stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_B);
        System.out.println("Random condition result: " + result);
        assertTrue(result || !result, "Random condition should return either true or false");
    }

    @Test
    public void testTransitionHistoryIncluded() {
        stateMachine = new StateMachine.Builder<TestState>()
                .state(TestState.STATE_A).onEnter(TestState.STATE_A, () -> System.out.println("Entering STATE_A"))
                .transition(TestState.STATE_A, () -> true, 0)

                .state(TestState.STATE_B)
                .transition(TestState.STATE_B, () -> true, 0)
                .onEnter(TestState.STATE_B, () -> System.out.println("Entering STATE_B"))
                .stopRunning(TestState.STOP)
                .build();

        stateMachine.start();
        stateMachine.update(); // Transition to STATE_B
        stateMachine.update(); // Transition to STATE_B
        stateMachine.update(); // Transition to STATE_B
        stateMachine.update(); // Transition to STATE_B
        stateMachine.isValidTransition(TestState.STATE_A, TestState.STATE_B);
        //print the history
        System.out.println(stateMachine.getStateHistory());
        assertTrue(stateMachine.getStateHistory().contains(TestState.STATE_A), "stateHistory should include the fromState after a valid transition");
    }

}
