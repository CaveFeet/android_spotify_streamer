package com.n8.spotifystreamer.events;

/**
 * Posted to bus when user checks the checkbox when viewing coachmarks that indicate they do/or don't want to see
 * the coachmarks again.
 */
public class CoachmarkShowAgainEvent {
    private boolean mChecked;

    public CoachmarkShowAgainEvent(boolean checked) {
        mChecked = checked;
    }

    public boolean isShowCoachmarksAgain() {
        return !mChecked;
    }
}
