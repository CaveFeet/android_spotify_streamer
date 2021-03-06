package com.n8.spotifystreamer.coachmarks;

import com.n8.n8droid.viewcontroller.ViewController;

/**
 * Defines behavior that can/should be handled when interacting with {@link CoachmarkFragmentView}
 */
public interface CoachmarkController extends ViewController {
    void onDoneClicked();

    void onShowCoachmarkCheckChanged(boolean isChecked);
}
