package org.gentrifiedApps.statemachineftc;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StateMachine<T extends Enum<T>> {
    private List<T> states;
    private Map<T, StateChangeCallback> onEnterCommands;
    private Map<T, StateChangeCallback> onExitCommands;
    private Map<T, Supplier<Boolean>> transitions;
    private Map<T, StateChangeCallback> whileStateCommands;
    private Map<T, Supplier<Boolean>> whileStateEscapeConditions;
    private T currentState;
    private List<T> stateHistory;
    private boolean isStarted = false;
    private boolean isRunning = true;

    public <T extends Enum<T>> StateMachine(T initialState, Map onEnterCommands, Map onExitCommands, Map transitions, Map stopConditions) {
    }

    public T getCurrentState() {
        return currentState;
    }

    public boolean mainLoop(LinearOpMode opMode) {
        return opMode.opModeIsActive() && isRunning();
    }

    public boolean isRunning() {
        return isRunning;
    }

    StateMachine(Builder<T> builder) {
        this.states = builder.states;
        this.onEnterCommands = builder.onEnterCommands;
        this.onExitCommands = builder.onExitCommands;
        this.transitions = builder.transitions;
        this.whileStateCommands = builder.whileStateCommands;
        this.whileStateEscapeConditions = builder.whileStateEscapeConditions;
        this.currentState = null;
        this.stateHistory = new ArrayList<>();
    }

    public static class Builder<T extends Enum<T>> {
        private List<T> states;
        private Map<T, StateChangeCallback> onEnterCommands;
        private Map<T, StateChangeCallback> onExitCommands;
        private Map<T, Supplier<Boolean>> transitions;
        private Map<T, StateChangeCallback> whileStateCommands;
        private Map<T, Supplier<Boolean>> whileStateEscapeConditions;
        private StateMachine<T> machine;
        private int stopRunningIncluded = 0;

        public Builder() {
            states = new ArrayList<>();
            onEnterCommands = new HashMap<>();
            onExitCommands = new HashMap<>();
            transitions = new HashMap<>();
            whileStateCommands = new HashMap<>();
            whileStateEscapeConditions = new HashMap<>();
        }

        public Builder<T> state(T state) {
            if (states.contains(state)) {
                throw new IllegalArgumentException("State already exists");
            }
            states.add(state);
            return this;
        }

        public Builder<T> whileState(T state, Supplier<Boolean> escapeCondition, StateChangeCallback command) {
            whileStateCommands.put(state, command); // Store the command
            whileStateEscapeConditions.put(state, escapeCondition); // Store the escape condition
            return this;
        }

        public Builder<T> onEnter(T state, StateChangeCallback command) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            onEnterCommands.put(state, command);
            return this;
        }

        public Builder<T> onExit(T state, StateChangeCallback command) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            onExitCommands.put(state, command);
            return this;
        }

        public Builder<T> transition(T state, Supplier<Boolean> condition) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            transitions.put(state, condition);
            return this;
        }

        public Builder<T> stopRunning(T state) {
            this.stopRunningIncluded++;
            if (states.contains(state)) {
                throw new IllegalArgumentException("State already exists");
            }
            states.add(state);
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            onEnterCommands.put(state, () -> {
                this.machine.isRunning = false;
            });
            transitions.put(state, () -> {
                this.machine.isRunning = false;
                return true;
            });
            return this;
        }
        public Builder<T> delayEnter(T state, double delaySeconds) {
            if (delaySeconds <= 0){
                throw new IllegalArgumentException("Delay cannot be 0");
            }
            long startTime = System.currentTimeMillis();
            whileStateCommands.put(state, () -> {});
            whileStateEscapeConditions.put(state, () -> System.currentTimeMillis() - startTime >= delaySeconds*1000);
            return this;
        }

        public StateMachine<T> build() {
            if (states == null || states.isEmpty() || transitions == null || transitions.isEmpty()) {
                throw new IllegalArgumentException("States and transitions cannot be null or empty");
            }

            if (new HashSet<>(states).size() != states.size()) {
                throw new IllegalArgumentException("States cannot have duplicates");
            }

            if (states.size() != transitions.size()) {
                throw new IllegalArgumentException("Mismatched states and transitions");
            }

            if (onEnterCommands.get(states.get(0)) == null) {
                throw new IllegalArgumentException("Initial state must have a corresponding onEnter command");
            }
            if (this.stopRunningIncluded != 1) {
                throw new IllegalArgumentException("Not enough or too many stopRunning commands");
            }
            this.machine = new StateMachine<>(this);
            return this.machine;
        }
    }

    public void start() {
        if (isStarted) {
            throw new IllegalStateException("StateMachine has already been started");
        }
        isStarted = true;
        if (!states.isEmpty()) {
            currentState = states.get(0);
            StateChangeCallback onEnterAction = onEnterCommands.get(currentState);
            if (onEnterAction != null) {
                onEnterAction.onStateChange();
            }
        }
    }
    public void stop(){
        if (!isRunning){
            throw new IllegalStateException("StateMachine is already stopped");
        }
        isRunning = false;
        //delete all actions
        states.clear();
        onEnterCommands.clear();
        onExitCommands.clear();
        transitions.clear();
        whileStateCommands.clear();
        whileStateEscapeConditions.clear();
    }

    public boolean update() {
        if (!states.isEmpty()) {
            currentState = states.get(0);
            Supplier<Boolean> escapeCondition = whileStateEscapeConditions.get(currentState);
            while (escapeCondition != null && !escapeCondition.get()) {
                StateChangeCallback whileStateAction = whileStateCommands.get(currentState);
                if (whileStateAction != null) {
                    whileStateAction.onStateChange();
                }
            }
            Supplier<Boolean> transitionCondition = transitions.get(currentState);
            if (transitionCondition != null && transitionCondition.get()) {
                // Check if there are at least 2 states
                if (states.size() < 2) {
                    throw new IllegalStateException("Not enough states for transition");
                }
                // Get the next state
                T nextState = states.get(1);
                // Check if the transition is valid
                if (!isValidTransition(currentState, nextState)) {
                    throw new IllegalStateException("Invalid transition");
                }
                StateChangeCallback onExitAction = onExitCommands.get(currentState);
                if (onExitAction != null) {
                    onExitAction.onStateChange();
                }
                // Add the current state to the history
                stateHistory.add(currentState);
                // Remove the current state
                states.remove(0);
                // If there are more states, enter the next one
                if (!states.isEmpty()) {
                    currentState = states.get(0);
                    StateChangeCallback onEnterAction = onEnterCommands.get(currentState);
                    if (onEnterAction != null) {
                        onEnterAction.onStateChange();
                    }
                }
            } else {
                // Add the current state to the history
                stateHistory.add(currentState);
            }
        }
        return true;
    }

    public boolean isValidTransition(T fromState, T toState) {
        if (fromState == toState){
            throw new IllegalArgumentException("Cannot transition to itself");
        }
        if (!states.contains(fromState) && !stateHistory.contains(fromState)) {
            throw new IllegalArgumentException(fromState + " does not exist in the state machine");
        }
        if (!states.contains(toState)){
            throw new IllegalArgumentException(toState + " does not exist in the state machine");
        }
        Supplier<Boolean> transitionCondition = transitions.get(fromState);
        if (transitionCondition == null) {
            throw new IllegalStateException("No transition condition exists from state " + fromState);
        }
        return transitionCondition.get();
    }

    public List<T> getStateHistory() {
        return stateHistory;
    }
}