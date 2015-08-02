package com.n8.spotifystreamer.coachmarks;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.events.CoachmarkShowAgainEvent;
import com.n8.spotifystreamer.events.CoachmarksDoneEvent;

public class CoachmarkFragment extends BaseViewControllerFragment<CoachmarkFragmentView> implements CoachmarkController {

    public CoachmarkFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_coachmark;
    }

    @Override
    protected void setViewController() {
        mView.setController(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        String title = String.format(getString(R.string.coachmark_title), getString(R.string.app_name));
        mView.mToolbar.setTitle(title);

        return mView;
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
