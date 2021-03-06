package com.n8.spotifystreamer;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class BusProvider {

    private static class LazyHolder {
        private static final Bus INSTANCE = new MainThreadBus();
    }

    public static Bus getInstance() {
        return LazyHolder.INSTANCE;
    }

    private BusProvider(){ }

    /**
      * Be able to post from any thread to main thread
      */
    public static class MainThreadBus extends Bus {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                handler.post(new Runnable() {
                        @Override
                        public void run() {
                                MainThreadBus.super.post(event);
                            }
                    });
            }
        }
    }

}
