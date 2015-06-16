package com.n8.spotifystreamer.coachmarks;

import com.n8.spotifystreamer.BaseFragmentView;

/**
 * Defines behavior that can/should be handled when interacting with {@link CoachmarkFragmentView}
 */
public interface CoachmarkController extends BaseFragmentView.Controller {
    void onDoneClicked();

    void onShowCoachmarkCheckChanged(boolean isChecked);
}
