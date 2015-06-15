package com.n8.spotifystreamer.coachmarks;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.spotifystreamer.R;

public class CoachmarkFragment extends Fragment {

    private CoachmarkFragmentController mController;

    public CoachmarkFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CoachmarkFragmentView view = (CoachmarkFragmentView) inflater.inflate(R.layout.fragment_coachmark, container, false);

        if (mController == null) {
            mController = new CoachmarkFragmentController((AppCompatActivity)getActivity());
        }

        view.setController(getActivity(), mController);
        mController.onCreateView(view);

        return view;
    }
}
