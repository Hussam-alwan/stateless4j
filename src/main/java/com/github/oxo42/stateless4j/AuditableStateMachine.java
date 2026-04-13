package com.github.oxo42.stateless4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditableStateMachine<S, T> extends StateMachine<S, T> {


    // TransitionRecord
    public static class TransitionRecord<S, T> {
        public final S from;
        public final S to;
        public final T trigger;
        public final long timestamp;

        public TransitionRecord(S from, S to, T trigger) {
            this.from = from;
            this.to = to;
            this.trigger = trigger;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%d] %s --(%s)--> %s", timestamp, from, trigger, to);
        }
    }


    private final List<TransitionRecord<S, T>> transitionHistory = new ArrayList<>();


    public AuditableStateMachine(S initialState) {
        super(initialState);
    }

    public AuditableStateMachine(S initialState, StateMachineConfig<S, T> config) {
        super(initialState, config);
    }


    @Override
    protected void publicFire(T trigger, Object... args) {
        S before = getState();
        super.publicFire(trigger, args);
        S after = getState();

        if (!before.equals(after)) {
            transitionHistory.add(new TransitionRecord<>(before, after, trigger));
        }
    }

    public List<TransitionRecord<S, T>> getHistory() {
        return Collections.unmodifiableList(transitionHistory);
    }


    public void printHistory() {
        System.out.println("=== Transition History ===");
        if (transitionHistory.isEmpty()) {
            System.out.println("No transitions recorded yet.");
            return;
        }
        for (TransitionRecord<S, T> record : transitionHistory) {
            System.out.println(record);
        }
        System.out.println("==========================");
    }


    public S getInitialState() {
        return transitionHistory.isEmpty() ? getState()
                : transitionHistory.get(0).from;
    }


    public S getPreviousState() {
        if (transitionHistory.isEmpty()) return null;
        return transitionHistory.get(transitionHistory.size() - 1).from;
    }


    public T getLastTrigger() {
        if (transitionHistory.isEmpty()) return null;
        return transitionHistory.get(transitionHistory.size() - 1).trigger;
    }


    public int getTransitionCount() {
        return transitionHistory.size();
    }


    public boolean hasTransitioned(S from, S to) {
        for (TransitionRecord<S, T> record : transitionHistory) {
            if (record.from.equals(from) && record.to.equals(to)) {
                return true;
            }
        }
        return false;
    }


    public List<TransitionRecord<S, T>> getTransitionsFrom(S state) {
        List<TransitionRecord<S, T>> result = new ArrayList<>();
        for (TransitionRecord<S, T> record : transitionHistory) {
            if (record.from.equals(state)) {
                result.add(record);
            }
        }
        return Collections.unmodifiableList(result);
    }


    public List<TransitionRecord<S, T>> getTransitionsTo(S state) {
        List<TransitionRecord<S, T>> result = new ArrayList<>();
        for (TransitionRecord<S, T> record : transitionHistory) {
            if (record.to.equals(state)) {
                result.add(record);
            }
        }
        return Collections.unmodifiableList(result);
    }


    public void clearHistory() {
        transitionHistory.clear();
    }


    public List<TransitionRecord<S, T>> clearAndGetHistory() {
        List<TransitionRecord<S, T>> snapshot = new ArrayList<>(transitionHistory);
        transitionHistory.clear();
        return Collections.unmodifiableList(snapshot);
    }
}