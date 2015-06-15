package com.n8.spotifystreamer.events;

public class CoachmarkShowAgainEvent {
    private boolean mChecked;

    public CoachmarkShowAgainEvent(boolean checked) {
        mChecked = checked;
    }

    public boolean isShowCoachmarksAgain() {
        return !mChecked;
    }
}
