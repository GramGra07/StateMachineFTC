package org.mercurialftc.statemachineftc;

@FunctionalInterface
public interface StateChangeCallback {
    void onStateChange();
}