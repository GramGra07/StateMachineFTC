package org.gentrifiedApps.statemachineftc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SequentialRunSM<T extends Enum<T>> {
    // will take a run action and transition to the next state on the next call
    private List<T> states;
    private Map<T, StateChangeCallback> onEnterCommands;
    private Map<T, Supplier<Boolean>> transitions;
    private T currentState;
    private List<T> stateHistory;
    private boolean isStarted = false;
    private boolean isRunning = true;
    private boolean shouldRestart = true;
    private Map<T, StateChangeCallback> sustainOnEnter;
    public List<T> sustainStates;
    private Map<T, Supplier<Boolean>> sustainTransitions;

    public T getCurrentState() {
        return currentState;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStarted() {
        return isStarted;
    }

    SequentialRunSM(SequentialRunSM.Builder<T> builder) {
        this.states = builder.states;
        this.onEnterCommands = builder.onEnterCommands;
        this.transitions = builder.transitions;
        this.currentState = null;
        this.stateHistory = new ArrayList<>();

        this.sustainStates = new ArrayList<>(builder.states);
        this.sustainTransitions = new HashMap<>(builder.transitions);
        this.sustainOnEnter = new HashMap<>(builder.onEnterCommands);
    }

    public static class Builder<T extends Enum<T>> {
        private List<T> states;
        private Map<T, StateChangeCallback> onEnterCommands;
        private Map<T, Supplier<Boolean>> transitions;
        private SequentialRunSM<T> machine;
        private int stopRunningIncluded = 0;

        public Builder() {
            states = new ArrayList<>();
            onEnterCommands = new HashMap<>();
            transitions = new HashMap<>();
        }

        public SequentialRunSM.Builder<T> state(T state) {
            if (states.contains(state)) {
                throw new IllegalArgumentException("State already exists");
            }
            states.add(state);
            return this;
        }

        public SequentialRunSM.Builder<T> onEnter(T state, StateChangeCallback command) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            onEnterCommands.put(state, command);
            return this;
        }

        public SequentialRunSM.Builder<T> transition(T state, Supplier<Boolean> condition) {
            if (!states.contains(state)) {
                throw new IllegalArgumentException("State does not exist");
            }
            transitions.put(state, condition);
            return this;
        }

        public SequentialRunSM.Builder<T> stopRunning(T state) {
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

        public SequentialRunSM<T> build() {
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
            this.machine = new SequentialRunSM<T>(this);
            return this.machine;
        }
    }

    public void start() {
        if (isStarted) {
            throw new IllegalStateException("StateMachine has already been started");
        }
        isStarted = true;
        shouldRestart = true;
        isRunning = true;
        if (!states.isEmpty()) {
            currentState = states.get(0);
            StateChangeCallback onEnterAction = onEnterCommands.get(currentState);
            if (onEnterAction != null) {
                onEnterAction.onStateChange();
            }
        }
    }

    public void stop() {
        if (!isRunning) {
            throw new IllegalStateException("StateMachine is already stopped");
        }
        isRunning = false;
        //delete all actions
        states.clear();
        onEnterCommands.clear();
        transitions.clear();
    }

    public void restartAtBeginning() {
        if (shouldRestart) {
            stateHistory.clear();
            isRunning = false;
            isStarted = false;
            states = sustainStates;
            transitions = sustainTransitions;
            onEnterCommands = sustainOnEnter;
            currentStateIndex = 0;
            shouldRestart = false;
        }
    }

    StateMachine.TYPES currentActionType = StateMachine.TYPES.IDLE;
    int currentStateIndex = 0;

    public boolean update() {
        if (currentActionType == StateMachine.TYPES.IDLE && isStarted) {
            currentActionType = StateMachine.TYPES.ON_ENTER;
        }
        if (currentState == null || !isRunning) {
            return false;
        }

        StateChangeCallback onEnterAction = onEnterCommands.get(currentState);

        // Get the actions and conditions for the current state
        Supplier<Boolean> transitionCondition = transitions.get(currentState);
        if (currentActionType == StateMachine.TYPES.ON_ENTER) {
            if (onEnterAction != null) {
                onEnterAction.onStateChange();
            }
            currentActionType = StateMachine.TYPES.TRANSITION;
            return true;
        }
        // Handle STOP type
        if (currentActionType == StateMachine.TYPES.STOP) {
            stop();
            return false;
        }
        // Handle transition logic
        if (currentActionType == StateMachine.TYPES.TRANSITION) {
            if (transitionCondition != null && transitionCondition.get()) {
                int nextIndex = currentStateIndex + 1;
                isValidTransition(currentState, states.get(nextIndex));
                if (nextIndex < states.size()) {

                    // Transition to the next state
                stateHistory.add(currentState);
                    currentState = states.get(nextIndex);
                    currentStateIndex = nextIndex;
                    currentActionType = StateMachine.TYPES.ON_ENTER;
                    return true;
                } else {
                    currentActionType = StateMachine.TYPES.STOP; // Transition to STOP
                    return false;
            }
        }
        }

        return false; // No actions performed
    }

    public boolean isValidTransition(T fromState, T toState) {
        if (fromState == toState) {
            System.out.println("Cannot transition to itself");
            throw new IllegalArgumentException("Cannot transition to itself");
        }
        if (!states.contains(fromState) && !stateHistory.contains(fromState)) {
            System.out.println("Cannot transition to itself");
            throw new IllegalArgumentException(fromState + " does not exist in the state machine");
        }
        if (!states.contains(toState)) {
            System.out.println("Cannot transition to itself");
            throw new IllegalArgumentException(toState + " does not exist in the state machine");
        }
        Supplier<Boolean> transitionCondition = transitions.get(fromState);
        if (transitionCondition == null) {
            System.out.println("Cannot transition to itself");
            throw new IllegalStateException("No transition condition exists from state " + fromState);
        }
        return transitionCondition.get();
    }

    public List<T> getStateHistory() {
        return stateHistory;
    }
}
