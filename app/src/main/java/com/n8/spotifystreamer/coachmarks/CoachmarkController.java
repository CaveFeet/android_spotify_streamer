package com.n8.spotifystreamer.coachmarks;

import com.n8.spotifystreamer.BaseFragmentView;

public interface CoachmarkController extends BaseFragmentView.Controller {
    void onDoneClicked();

    void onShowCoachmarkCheckChanged(boolean isChecked);
}
