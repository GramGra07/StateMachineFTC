package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gentrifiedApps.statemachineftc.StateMachine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class StateMachineTestCases {

    private StateMachine<States> stateMachine;

    enum States {
        STATE1,
        STATE2,
        STATE3,
        STATE4,
        STATE5,
        STOP,
    }

    @Test
    void testBasic() {
        System.out.println("Testing basic state machine");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();
        assertTrue(stateMachine.update());
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        assertTrue(stateMachine.update());
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        System.out.println("States updated successfully");
    }

    @Test
    void testOnExit() {
        System.out.println("Testing onExit");
        // Create a flag to track if the onExit command was executed
        final boolean[] commandExecuted = {false};
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .onExit(States.STATE1, () -> {
                    System.out.println("Exiting STATE1");
                    commandExecuted[0] = true;
                })
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        stateMachine = builder.build();
        stateMachine.start();

        // Check that the state machine is in the STATE1 state
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();

        // Check that the onExit command was executed
        assertTrue(commandExecuted[0]);
        System.out.println("OnExit was run");
        System.out.println("State History:" + stateMachine.getStateHistory());
    }

    @Test
    void testStop() {
        System.out.println("Testing stop");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();
        assertTrue(stateMachine.isRunning());
        assertTrue(stateMachine.update());
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        assertTrue(stateMachine.update());
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());
        System.out.println("State machine stopped");
    }

    @Test
    void testWhileState() {
        System.out.println("Testing whileState");
        // Create a flag to track if the whileState command was executed
        final boolean[] commandExecuted = {false, false};
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .whileState(States.STATE1, () -> commandExecuted[1], () -> {
                    commandExecuted[0] = true;
                    commandExecuted[1] = true;
                })
                .transition(States.STATE1, () -> false, 0)
                .stopRunning(States.STOP);
        stateMachine = builder.build();
        stateMachine.start();

        // Check that the state machine is in the STATE1 state
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();

        // Check that the whileState command was executed
        assertTrue(commandExecuted[0]);
        System.out.println("While state was run");
    }

    @Test
    void testInvalidState() {
        System.out.println("Testing invalid state");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, () -> builder.state(States.STATE1));
    }

    @Test
    void testDuplicateStates() {
        System.out.println("Testing duplicate states");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, () -> builder.state(States.STATE1));
    }

    @Test
    void testNoOnEnterForInitialState() {
        System.out.println("Testing no onEnter command for initial state");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testInvalidTransition() {
        System.out.println("Testing invalid transition");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, () -> builder.transition(States.STATE2, () -> true, 0));
    }

    @Test
    void testInvalidOnEnter() {
        System.out.println("Testing invalid onEnter");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, () -> builder.onEnter(States.STATE2, () -> System.out.println("Entering STATE2")));
    }

    @Test
    void testInvalidOnExit() {
        System.out.println("Testing invalid onExit");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, () -> builder.onExit(States.STATE2, () -> System.out.println("Exiting STATE2")));
    }

    @Test
    void testMismatchedStatesAndTransitions() {
        System.out.println("Testing mismatched states and transitions");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testNoStop() {
        System.out.println("Testing no stop");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        builder.onEnter(States.STATE1, () -> System.out.println("Entering STATE1"));
        builder.transition(States.STATE1, () -> true, 0);
        builder.state(States.STATE2);
        builder.onEnter(States.STATE2, () -> System.out.println("Entering STATE2"));
        builder.transition(States.STATE2, () -> true, 0);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testNullStatesAndTransitions() {
        System.out.println("Testing null states and transitions");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testInvalidBuild() {
        System.out.println("Testing invalid build");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testStart() {
        System.out.println("Testing start");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());
    }

    @Test
    void testUpdate() {
        System.out.println("Testing update");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());
    }

    @Test
    void testIsValidTransition() {
        System.out.println("Testing isValidTransition method");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test valid transition
        assertTrue(stateMachine.isValidTransition(States.STATE1, States.STATE2));

        // Test invalid transition
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE2, States.STATE3));

        System.out.println("isValidTransition method tested successfully");
    }

    @Test
    void testGetStateHistory() {
        System.out.println("Testing getStateHistory");
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());
        stateMachine.update();
        assertEquals(States.STATE1, stateMachine.getStateHistory().get(0));
        stateMachine.update();
        assertEquals(2, stateMachine.getStateHistory().size());
        assertEquals(States.STATE2, stateMachine.getStateHistory().get(1));
        System.out.println("State History:" + stateMachine.getStateHistory());
    }

    @Test
    void testUpdateFiveTimes() {
        System.out.println("Testing update method five times");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .state(States.STATE4)
                .onEnter(States.STATE4, () -> System.out.println("Entering STATE4"))
                .transition(States.STATE4, () -> true, 0)
                .state(States.STATE5)
                .onEnter(States.STATE5, () -> System.out.println("Entering STATE5"))
                .transition(States.STATE5, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine five times
        for (int i = 0; i < 4; i++) {
            assertTrue(stateMachine.update());
        }

        // Check the current state
        assertEquals(States.STATE5, stateMachine.getCurrentState());
        assertTrue(stateMachine.update());
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());

        System.out.println("Update method tested successfully five times");
    }

    @Test
    void testGetCurrentState() {
        System.out.println("Testing getCurrentState method");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Check the initial state
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();

        // Check the current state after update
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        System.out.println("getCurrentState method tested successfully");
    }

    @Test
    void extensiveTestIsValidTransition() {
        System.out.println("Extensively testing isValidTransition method");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test valid transition
        assertTrue(stateMachine.isValidTransition(States.STATE1, States.STATE2));

        // Test invalid transition
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE2, States.STATE3));

        // Test transition with states that do not exist in the state machine
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE3, States.STATE4));

        System.out.println("isValidTransition method tested successfully");
    }

    @Test
    void testValidTransition() {
        System.out.println("Testing valid transition");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test valid transition
        assertTrue(stateMachine.isValidTransition(States.STATE1, States.STATE2));
    }

    @Test
    void testInvalidTransitionAgain() {
        System.out.println("Testing invalid transition");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test invalid transition
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE2, States.STATE3));
    }

    @Test
    void testNonExistentStates() {
        System.out.println("Testing transition with states that do not exist in the state machine");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test transition with states that do not exist in the state machine
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE3, States.STATE4));
    }

// Continue with the rest of the test cases following the same pattern...

    @Test
    void testUpdateExtensive() {
        System.out.println("Testing update method");

        // Set up the state machine with multiple states and transitions
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine and check the current state
        assertTrue(stateMachine.update());
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Update the state machine again and check the current state
        assertTrue(stateMachine.update());
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        // Set up the state machine with a single state and no transitions
        builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .stopRunning(States.STOP);
        stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine and check the current state
        assertTrue(stateMachine.update());
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertThrows(IllegalStateException.class, stateMachine::update);

        // Set up the state machine with a state with a transition condition that always returns false
        builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> false, 0)
                .stopRunning(States.STOP);
        stateMachine = builder.build();
        stateMachine.start();

        //! never transitions
        assertTrue(stateMachine.update());
        assertTrue(stateMachine.isRunning());
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        System.out.println("Update method tested successfully");
    }

    @Test
    void testAllButFirst() {
        System.out.println("Testing most methods");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Test getCurrentState method
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Test update method
        assertTrue(stateMachine.update());
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        assertTrue(stateMachine.isRunning());
        assertTrue(stateMachine.isValidTransition(States.STATE1, States.STATE2));
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE2, States.STATE3));
        assertTrue(stateMachine.update());
        List<States> expectedStateHistory = Arrays.asList(States.STATE1, States.STATE2);
        assertEquals(expectedStateHistory, stateMachine.getStateHistory());

        System.out.println("Most methods tested successfully");
    }

    @Test
    void testAll() {
        System.out.println("Testing all functionality");

        // Create a flag to track if the onExit and whileState commands were executed
        final boolean[] commandExecuted = {false, false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .whileState(States.STATE1, () -> commandExecuted[1], () -> {
                    System.out.println("While in STATE1");
                    commandExecuted[1] = true;
                })
                .onExit(States.STATE1, () -> {
                    System.out.println("Exiting STATE1");
                    commandExecuted[0] = true;
                })
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        assertTrue(stateMachine.isRunning());
        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the onExit and whileState commands were executed
        assertTrue(commandExecuted[0]);
        assertTrue(commandExecuted[1]);

        // Update the state machine again
        stateMachine.update();
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());

        System.out.println("All functionality tested successfully");
    }

    @Test
    void testAllLoopFunctionality() {
        System.out.println("Testing all looping functionality");
        AtomicInteger counter = new AtomicInteger();
        int end;
        int rand = new Random().nextInt(1000);
        System.out.println("Random number: " + rand);
        StateMachine.Builder<States> builder;
        for (int l = 2; l < rand; l++) { //must start at two because of states
            counter.getAndSet(0);
            end = l;
            builder = new StateMachine.Builder<>();
            int finalEnd = end;
            builder.state(States.STATE1)
                    .onEnter(States.STATE1, () -> {
//                        System.out.println("Entering STATE1");
                    })
                    .transition(States.STATE1, () -> true, 0)
                    .state(States.STATE2)
                    .onEnter(States.STATE2, () -> {
//                        System.out.println("Entering STATE2");
                        counter.getAndIncrement();
                    })
                    .whileState(States.STATE2, () -> counter.get() >= finalEnd
                            , () -> {
//                                System.out.println("While in STATE2");
                                counter.getAndIncrement();
                            })
                    .transition(States.STATE2, () ->
                            counter.get() == finalEnd, 0
                    )
                    .stopRunning(States.STOP);
            stateMachine = builder.build();
            stateMachine.start();
            assertEquals(States.STATE1, stateMachine.getCurrentState());
            assertEquals(0, counter.get());
            assertTrue(stateMachine.update());
            assertEquals(States.STATE2, stateMachine.getCurrentState());
            assertEquals(1, counter.get());
            stateMachine.update();
            assertEquals(finalEnd, counter.get());
            assertEquals(States.STOP, stateMachine.getCurrentState());
            assertFalse(stateMachine.isRunning());
            assertThrows(IllegalStateException.class, stateMachine::update);
        }

        System.out.println("All while tested successfully");
    }

    @Test
    void testSmallNumberOfStates() {
        System.out.println("Testing large number of states");

        // Set up the state machine with a large number of states
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        for (States state : States.values()) {
            if (state == States.STOP) {
                break;
            }
            builder.state(state)
                    .onEnter(state, () -> System.out.println("Entering " + state))
                    .transition(state, () -> true, 0);
        }
        builder.stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine and check the current state
        for (int i = 0; i < States.values().length - 1; i++) {
            assertTrue(stateMachine.update());
            assertEquals(States.values()[(i + 1) % States.values().length], stateMachine.getCurrentState());
        }

        // Check that the state machine has stopped
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());

        System.out.println("Large number of states tested successfully");
    }

    public enum LargeStates {
        STATE1, STATE2, STATE3, STATE4, STATE5, STATE6, STATE7, STATE8, STATE9, STATE10,
        STATE11, STATE12, STATE13, STATE14, STATE15, STATE16, STATE17, STATE18, STATE19, STATE20,
        STATE21, STATE22, STATE23, STATE24, STATE25, STATE26, STATE27, STATE28, STATE29, STATE30,
        STATE31, STATE32, STATE33, STATE34, STATE35, STATE36, STATE37, STATE38, STATE39, STATE40,
        STATE41, STATE42, STATE43, STATE44, STATE45, STATE46, STATE47, STATE48, STATE49, STATE50,
        STATE51, STATE52, STATE53, STATE54, STATE55, STATE56, STATE57, STATE58, STATE59, STATE60,
        STATE61, STATE62, STATE63, STATE64, STATE65, STATE66, STATE67, STATE68, STATE69, STATE70,
        STATE71, STATE72, STATE73, STATE74, STATE75, STATE76, STATE77, STATE78, STATE79, STATE80,
        STATE81, STATE82, STATE83, STATE84, STATE85, STATE86, STATE87, STATE88, STATE89, STATE90,
        STATE91, STATE92, STATE93, STATE94, STATE95, STATE96, STATE97, STATE98, STATE99, STATE100,
        STOP
    }

    @Test
    void testLargeNumberOfStates() {
        System.out.println("Testing large number of states");

        // Set up the state machine with a large number of states
        StateMachine.Builder<LargeStates> builder = new StateMachine.Builder<>();
        for (LargeStates state : LargeStates.values()) {
            if (state == LargeStates.STOP) {
                break;
            }
            builder.state(state)
                    .onEnter(state, () -> System.out.println("Entering " + state))
                    .transition(state, () -> true, 0);
        }
        builder.stopRunning(LargeStates.STOP);
        StateMachine<LargeStates> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine and check the current state
        for (int i = 0; i < LargeStates.values().length - 1; i++) {
            assertTrue(stateMachine.update());
            assertEquals(LargeStates.values()[(i + 1) % LargeStates.values().length], stateMachine.getCurrentState());
        }

        // Check that the state machine has stopped
        assertEquals(LargeStates.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());

        System.out.println("Large number of states tested successfully");
    }

    @Test
    void testComplexTransition() {
        System.out.println("Testing complex transition");
        final int[] rand = {10};
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .whileState(States.STATE2, () -> rand[0] < 5, () -> {
                    // Simulate a complex command with a random number generator
                    rand[0] = new Random().nextInt(10);
                    System.out.println("Random number: " + rand[0]);
                })
                .transition(States.STATE2, () -> rand[0] < 5, 0
                )
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine until it reaches STATE3 or STOP
        while (stateMachine.isRunning() && stateMachine.getCurrentState() != States.STATE3 && stateMachine.getCurrentState() != States.STOP) {
            stateMachine.update();
        }

        // Check that the state machine has reached STATE3 or STOP
        assertTrue(stateMachine.getCurrentState() == States.STATE3 || stateMachine.getCurrentState() == States.STOP);

        System.out.println("Complex transition tested successfully");
    }

    @Test
    void testExtremeComplexTransition() {
        System.out.println("Testing extreme complex transition");
        final int bound = 1000;
        final int[] rand = {bound};
        final int low = 5;
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .whileState(States.STATE2, () -> rand[0] < low, () -> {
                    // Simulate a complex command with a random number generator
                    rand[0] = new Random().nextInt(bound);
                    System.out.println("Random number: " + rand[0]);
                })
                .transition(States.STATE2, () -> rand[0] < low, 0)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine until it reaches STATE3 or STOP
        while (stateMachine.isRunning() && stateMachine.getCurrentState() != States.STATE3 && stateMachine.getCurrentState() != States.STOP) {
            stateMachine.update();
        }

        // Check that the state machine has reached STATE3 or STOP
        assertTrue(stateMachine.getCurrentState() == States.STATE3 || stateMachine.getCurrentState() == States.STOP);

        System.out.println("Extremely complex transition tested successfully");
    }

    @Test
    void testComprehensive() {
        System.out.println("Testing all functionality comprehensively");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();
        // Test isValidTransition method with valid and invalid transitions
        assertTrue(stateMachine.isValidTransition(States.STATE1, States.STATE2));
        assertEquals(States.STATE1, stateMachine.getCurrentState());
        assertTrue(stateMachine.isRunning());

        // Update the state machine and check the current state and state history
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        assertTrue(stateMachine.update());
        assertEquals(Arrays.asList(States.STATE1, States.STATE2), stateMachine.getStateHistory());
        assertTrue(stateMachine.isRunning());

        // Update the state machine again and check the current state and state history
        stateMachine.update();
        assertEquals(States.STATE3, stateMachine.getCurrentState());
        assertEquals(Arrays.asList(States.STATE1, States.STATE2, States.STATE3), stateMachine.getStateHistory());
        assertTrue(stateMachine.isRunning());

        // Update the state machine again and check the current state and state history
        stateMachine.update();
        assertNotEquals(States.STOP, stateMachine.getCurrentState());
        assertTrue(stateMachine.isRunning());

        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE2, States.STATE1));

        // Test isValidTransition method with states that do not exist in the state machine
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE3, States.STATE4));

        System.out.println("All functionality tested comprehensively");
    }

    @Test
    void testStateWithTransitionToItself() {
        System.out.println("Testing state with transition to itself");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();
        assertThrows(IllegalArgumentException.class, () -> stateMachine.isValidTransition(States.STATE1, States.STATE1));

        System.out.println("State with transition to itself tested successfully");
    }

    @Test
    void testStateWithMultipleTransitions() {
        System.out.println("Testing state with multiple transitions");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .transition(States.STATE1, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check the current state
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        System.out.println("State with multiple transitions tested successfully");
    }

    @Test
    void testStateWithNoTransitions() {
        System.out.println("Testing state with no transitions");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .stopRunning(States.STOP);

        // Update the state machine
        assertThrows(IllegalArgumentException.class, builder::build);

        System.out.println("State with no transitions tested successfully");
    }

    @Test
    void testStateWithTransitionToNewState() {
        System.out.println("Testing state with transition to new state");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE4) // new state
                .onEnter(States.STATE4, () -> System.out.println("Entering STATE4"))
                .transition(States.STATE4, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check the current state
        assertEquals(States.STATE4, stateMachine.getCurrentState());

        System.out.println("State with transition to new state tested successfully");
    }

    @Test
    void testStateWithTransitionToUnvisitedState() {
        System.out.println("Testing state with transition to unvisited state");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE5) // unvisited state
                .onEnter(States.STATE5, () -> System.out.println("Entering STATE5"))
                .transition(States.STATE5, () -> false, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check the current state
        assertEquals(States.STATE5, stateMachine.getCurrentState());

        System.out.println("State with transition to unvisited state tested successfully");
    }

    @Test
    void testStartWithAlreadyStartedStateMachine() {
        System.out.println("Testing start method with an already started state machine");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();

        // Attempt to start the state machine again
        assertThrows(IllegalStateException.class, stateMachine::start);

        System.out.println("Start method with an already started state machine tested successfully");
    }

    @Test
    void testUpdateWithStoppedStateMachine() {
        System.out.println("Testing update method with a stopped state machine");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();

        // Update the state machine until it stops
        while (stateMachine.isRunning()) {
            stateMachine.update();
        }

        // Attempt to update the stopped state machine
        assertThrows(IllegalStateException.class, stateMachine::update);

        System.out.println("Update method with a stopped state machine tested successfully");
    }

    @Test
    void testOrderOfOperations() {
        System.out.println("Testing order of operations");

        // Create a list to keep track of the order of operations
        List<String> operations = new ArrayList<>();

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> operations.add("Entering STATE1"))
                .whileState(States.STATE1, () -> operations.size() > 5, () -> {
                    operations.add("While in STATE1");
                })
                .transition(States.STATE1, () -> {
                    operations.add("Transitioning from STATE1");
                    return true;
                }, 0)
                .onExit(States.STATE1, () -> operations.add("Exiting STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> operations.add("Entering STATE2"))
                .transition(States.STATE2, () -> {
                    operations.add("Transitioning from STATE2");
                    return true;
                }, 0)
                .onExit(States.STATE2, () -> operations.add("Exiting STATE2"))
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        operations.add("Started state machine");
        // Start the state machine
        stateMachine.start();

        // Update the state machine
        stateMachine.update();
        operations.add("Updated state machine");
        stateMachine.update();

        // Check the order of operations
        List<String> expectedOperations = Arrays.asList(
                "Started state machine",
                "Entering STATE1",
                "While in STATE1",
                "While in STATE1",
                "While in STATE1",
                "While in STATE1",
                "Transitioning from STATE1",
                "Transitioning from STATE1",
                "Exiting STATE1",
                "Entering STATE2",
                "Updated state machine",
                "Transitioning from STATE2",
                "Transitioning from STATE2",
                "Exiting STATE2"
        );
        assertEquals(expectedOperations, operations);
        System.out.println("Order of operations: " + operations);

        System.out.println("Order of operations tested successfully");
    }

    @Test
    void testStopMethod() {
        System.out.println("Testing stop method");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 0)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();
        assertTrue(stateMachine.isRunning());

        // Stop the state machine
        stateMachine.stop();
        assertFalse(stateMachine.isRunning());
        // Attempt to stop the state machine again
        assertThrows(IllegalStateException.class, stateMachine::stop);
        assertTrue(stateMachine.update());
        System.out.println("Stop method tested successfully");
    }

    @Test
    void testDelayState() {
        System.out.println("Testing delay state");

        // Create a flag to track if the delay state was entered
        final boolean[] delayStateEntered = {false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayStateEntered[0] = true;
                })
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the delay state was entered
        assertTrue(delayStateEntered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine has transitioned out of the delay state
        assertEquals(States.STOP, stateMachine.getCurrentState());

        System.out.println("Delay state tested successfully");
    }

    @Test
    void testMultipleDelayStates() {
        System.out.println("Testing multiple delay states");

        // Create flags to track if the delay states were entered
        final boolean[] delayState1Entered = {false};
        final boolean[] delayState2Entered = {false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayState1Entered[0] = true;
                })
                .transition(States.STATE2, () -> true, 2)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    delayState2Entered[0] = true;
                })
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the first delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the first delay state was entered
        assertTrue(delayState1Entered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine is in the second delay state
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        // Check that the second delay state was entered
        assertTrue(delayState2Entered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine has transitioned out of the delay states
        assertEquals(States.STOP, stateMachine.getCurrentState());

        System.out.println("Multiple delay states tested successfully");
    }

    @Test
    void testDelayStateWithTransition() {
        System.out.println("Testing delay state with transition");

        // Create a flag to track if the delay state was entered
        final boolean[] delayStateEntered = {false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayStateEntered[0] = true;
                })
                .transition(States.STATE2, () -> true, 0)
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the delay state was entered
        assertTrue(delayStateEntered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine has transitioned out of the delay state
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        System.out.println("Delay state with transition tested successfully");
    }

    @Test
    void testStopAfterDelay() {
        System.out.println("Testing stop after delay");
        // Create a flag to track if the delay state was entered
        final boolean[] delayStateEntered = {false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayStateEntered[0] = true;
                })
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the delay state was entered
        assertTrue(delayStateEntered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine has transitioned out of the delay state
        assertEquals(States.STOP, stateMachine.getCurrentState());

        System.out.println("Stop after delay tested successfully");
    }

    @Test
    public void testStopAfterDelay2() {
        System.out.println("Testing stop after delay");

        // Create a flag to track if the delay state was entered
        final boolean[] delayStateEntered = {false};

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayStateEntered[0] = true;
                })
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the delay state was entered
        assertTrue(delayStateEntered[0]);

        // Stop the state machine while it's in the delay state
        stateMachine.stop();

        // Check that the state machine has stopped
        assertFalse(stateMachine.isRunning());

        System.out.println("Stop after delay tested successfully");
    }

    @Test
    void testRandomDelay() {
        System.out.println("Testing random delay");

        // Create a flag to track if the delay state was entered
        final boolean[] delayStateEntered = {false};

        // Generate a random delay time
        Random random = new Random();
        int delayTime = random.nextInt(10); // Delay for a random time up to 5 seconds
        System.out.println(delayTime + "seconds");
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, delayTime)
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    delayStateEntered[0] = true;
                })
                .transition(States.STATE2, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        stateMachine.start();

        // Update the state machine
        stateMachine.update();

        // Check that the state machine is in the delay state
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Check that the delay state was entered
        assertTrue(delayStateEntered[0]);

        // Update the state machine again
        stateMachine.update();

        // Check that the state machine has transitioned out of the delay state
        assertEquals(States.STOP, stateMachine.getCurrentState());

        System.out.println("Random delay tested successfully");
    }

    @Test
    void testComprehensiveDelayState() {
        System.out.println("Testing comprehensive delay state");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1.0) // 1 second delay
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 1.0) // 1 second delay
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());
        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());

        System.out.println("Comprehensive delay state tested successfully");
    }

    @Test
    void testExtremelyComprehensiveDelayState() {
        System.out.println("Testing extremely comprehensive delay state");


        long startTime = System.currentTimeMillis();
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, 1.0) // 1 second delay
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, 1.0) // 1 second delay
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 1.0) // 1 second delay
                .state(States.STATE4)
                .onEnter(States.STATE4, () -> System.out.println("Entering STATE4"))
                .transition(States.STATE4, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        // Start the state machine
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE4, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());
        long endTime = System.currentTimeMillis();
        System.out.println(Math.abs(endTime - startTime));
        assertTrue(Math.abs(endTime - startTime) > 3000);
        System.out.println("Extremely comprehensive delay state tested successfully");
    }

    @Test
    void testComprehensiveRandomDelayState() {
        System.out.println("Testing comprehensive random delay state");
        long startTime = System.currentTimeMillis();
        int[] random = {0, 0, 0, 0};
        random[0] = new Random().nextInt(20);
        System.out.println(random[0] + " seconds");
        random[1] = new Random().nextInt(20);
        System.out.println(random[1] + " seconds");
        random[2] = new Random().nextInt(20);
        System.out.println(random[2] + " seconds");
        random[3] = new Random().nextInt(20);
        System.out.println(random[3] + " seconds");

        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .transition(States.STATE1, () -> true, random[0]) // 1 second delay
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, random[1]) // 1 second delay
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, random[2]) // 1 second delay
                .state(States.STATE4)
                .onEnter(States.STATE4, () -> System.out.println("Entering STATE4"))
                .transition(States.STATE4, () -> true, random[3])
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();
        // Start the state machine
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE3, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE4, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STOP, stateMachine.getCurrentState());
        assertFalse(stateMachine.isRunning());
        long endTime = System.currentTimeMillis();
        System.out.println(Math.abs(endTime - startTime));
        assertTrue(Math.abs(endTime - startTime) > (random[0] + random[1] + random[2] + random[3]) * 1000);
        System.out.println("Comprehensive random delay state tested successfully");
    }

    @Test
    void testComplexTransitionIntoDelay() {
        System.out.println("Testing complex transition into delay");
        long startTime = System.currentTimeMillis();
        int[] random = {0, 0, 0, 0};
        random[0] = new Random().nextInt(15);
        System.out.println(random[0] + " seconds");
        random[1] = new Random().nextInt(15);
        System.out.println(random[1] + " seconds");

        final int bound = 1000;
        final int[] rand = {bound};
        final int low = 5;
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .whileState(States.STATE1, () -> rand[0] < low, () -> {
                    // Simulate a complex command with a random number generator
                    rand[0] = new Random().nextInt(bound);
                    System.out.println("Random number: " + rand[0]);
                })
                .transition(States.STATE1, () -> rand[0] < low, random[0]) // complex transition
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .transition(States.STATE2, () -> true, random[1]) // 1 second delay
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .transition(States.STATE3, () -> true, 0)
                .stopRunning(States.STOP);
        StateMachine<States> stateMachine = builder.build();

        // Start the state machine
        stateMachine.start();
        assertEquals(States.STATE1, stateMachine.getCurrentState());

        // Update the state machine
        stateMachine.update();
        assertEquals(States.STATE2, stateMachine.getCurrentState());

        assertTrue(stateMachine.isRunning());
        stateMachine.update();

        long endTime = System.currentTimeMillis();
        System.out.println(Math.abs(endTime - startTime));
        assertTrue(Math.abs(endTime - startTime) > (random[0] + random[1]) * 1000);
        System.out.println("Complex transition into delay tested successfully");
    }
    @Test
    void testDelayError() {
        System.out.println("Testing complex transition into delay");
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"));
        assertThrows(IllegalArgumentException.class, ()->builder.transition(States.STATE1, () -> true, -1));

        System.out.println("Delay error tested successfully");
    }
    @Test
    void testDelayErrorRandomNegative() {
        System.out.println("Testing complex transition into delay");
        // Set up the state machine
        StateMachine.Builder<States> builder = new StateMachine.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"));
        for (int i = 0; i < 100; i++) {
            int random = new Random().nextInt(1000);
            assertThrows(IllegalArgumentException.class, () -> builder.transition(States.STATE1, () -> true, -1*random));
        }

        System.out.println("Delay error tested successfully");
    }
}