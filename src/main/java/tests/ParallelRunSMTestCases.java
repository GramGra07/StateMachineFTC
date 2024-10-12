package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gentrifiedApps.statemachineftc.ParallelRunSM;
import org.gentrifiedApps.statemachineftc.StateMachine;
import org.junit.jupiter.api.Test;

public class ParallelRunSMTestCases {
    enum States {
        STATE1,
        STATE2,
        STATE3,
        STATE4,
        STOP
    }

    @Test
    public void testParallelRunSM() {

        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertFalse(stateMachine.isRunning());
    }
    @Test
    public void testParallelness() {
        final boolean[] map = {false,false,false};
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> {
                    System.out.println("Entering STATE1");
                    map[0] = true;
                })
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    map[1] = true;
                })
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    map[2] = true;
                })
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(map[0]);
        assertTrue(map[1]);
        assertTrue(map[2]);
        assertFalse(stateMachine.isRunning());
    }
    //Test for the stopRunning method
    @Test
    public void testStopRunning() throws InterruptedException {
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .stopRunning(States.STOP,()->false);
        ParallelRunSM<States> stateMachine = builder.build(true,5000);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(stateMachine.update());
        assertFalse(stateMachine.checkExitTransition());
        assertTrue(stateMachine.isRunning());
        Thread.sleep(5100);
        assertTrue(stateMachine.update());
        assertFalse(stateMachine.isRunning());
    }
    @Test
    public void testWithoutTimeout() throws InterruptedException {
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .stopRunning(States.STOP,()->false);
        ParallelRunSM<States> stateMachine = builder.build(false,1000);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(stateMachine.update());
        assertFalse(stateMachine.checkExitTransition());
        assertTrue(stateMachine.isRunning());
        Thread.sleep(1100);
        assertTrue(stateMachine.update());
        assertTrue(stateMachine.isRunning());
    }
    @Test
    public void testStop() {
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        stateMachine.stop();
        assertFalse(stateMachine.isRunning());
    }
    //test comprehensively
    @Test
    public void testComprehensive() {
        final boolean[] map = {false,false,false};
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> {
                    System.out.println("Entering STATE1");
                    map[0] = true;})
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    map[1] = true;
                })
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    map[2] = true;
                })
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(map[0]);
        assertTrue(map[1]);
        assertTrue(map[2]);
        assertFalse(stateMachine.isRunning());
    }
    @Test
    public void testTiming() {
        final boolean[] map = {false,false,false};
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> {
                    System.out.println("Entering STATE1");
                    map[0] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }})
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    map[1] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    map[2] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(map[0]);
        assertTrue(map[1]);
        assertTrue(map[2]);
        assertFalse(stateMachine.isRunning());
    }
    @Test
    public void testStopRunningTiming() {
        final boolean[] map = {false,false,false};
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> {
                    System.out.println("Entering STATE1");
                    map[0] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }})
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    map[1] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    map[2] = true;
                    // delay
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .stopRunning(States.STOP,()->false);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(map[0]);
        assertTrue(map[1]);
        assertTrue(map[2]);
        assertTrue(stateMachine.isRunning());
    }
    @Test
    public void testStopRunningTiming2() {
        final boolean[] map = {false,false,false};
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> {
                    System.out.println("Entering STATE1");
                    map[0] = true;
                    // delay
                })
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> {
                    System.out.println("Entering STATE2");
                    map[1] = true;
                })
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> {
                    System.out.println("Entering STATE3");
                    map[2] = true;
                })
                .state(States.STATE4)
                .onEnter(States.STATE4, () -> {
                    System.out.println("Entering STATE4");
                    map[2] = false;
                })
                .stopRunning(States.STOP,()->true);
        ParallelRunSM<States> stateMachine = builder.build(false,100);
        stateMachine.start();
        assertTrue(stateMachine.isStarted());
        assertTrue(stateMachine.update());
        assertTrue(map[0]);
        assertTrue(map[1]);
        assertFalse(map[2]);
        assertFalse(stateMachine.isRunning());
    }
    // mismatch states and onenters
    @Test
    public void testMismatchStatesAndOnEnters() {
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .stopRunning(States.STOP,()->true);
        try {
            ParallelRunSM<States> stateMachine = builder.build(false,100);
        } catch (IllegalArgumentException e) {
            assertEquals("Not all states have corresponding onEnter commands", e.getMessage());
        }
    }
    // test a negative timeout
    @Test
    public void testNegativeTimeout() {
        ParallelRunSM.Builder<States> builder = new ParallelRunSM.Builder<>();
        builder.state(States.STATE1)
                .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
                .state(States.STATE2)
                .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
                .state(States.STATE3)
                .onEnter(States.STATE3, () -> System.out.println("Entering STATE3"))
                .stopRunning(States.STOP,()->true);
        try {
            ParallelRunSM<States> stateMachine = builder.build(false,-100);
        } catch (IllegalArgumentException e) {
            assertEquals("Timeout must be a positive integer", e.getMessage());
        }
    }
}
