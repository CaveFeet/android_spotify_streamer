package com.n8.spotifystreamer.coachmarks;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.n8.spotifystreamer.BaseFragmentController;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.events.CoachmarkShowAgainEvent;
import com.n8.spotifystreamer.events.CoachmarksDoneEvent;

public class CoachmarkFragmentController extends BaseFragmentController<CoachmarkFragmentView> implements CoachmarkController {

    public CoachmarkFragmentController() {
        super();
    }

    @Override
    public void onDoneClicked() {
        BusProvider.getInstance().post(new CoachmarksDoneEvent());
    }

    @Override
    public void onShowCoachmarkCheckChanged(boolean isChecked) {
        BusProvider.getInstance().post(new CoachmarkShowAgainEvent(isChecked));
    }
}
