package org.gentrifiedApps.statemachineftc;

@FunctionalInterface
public interface StateChangeCallback {
    void onStateChange();
}