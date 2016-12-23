package com.dave_cs.BroncoShuttlePlusMobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DetailsViewHeaderData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.LiveMapData;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 12/22/2016.
 */

public class SplashActivity extends Activity implements Observer {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 7000;

    private boolean mapReady = false;
    private boolean detailsReady = false;

    private Handler transitionHandler = new Handler();
    private Runnable transitionRunnable = new Runnable() {
        @Override
        public void run() {
            Intent transition = new Intent(SplashActivity.this, MainMenuActivity.class);
            startActivity(transition);
            finish();
        }
    };

//    private Thread transitionThread = new Thread(
//            new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(SPLASH_DURATION);
//                    } catch (InterruptedException e) {
//                        //post now.
//                    }
//                    checkState.interrupt();
//                    Intent transition = new Intent(SplashActivity.this, MainMenuActivity.class);
//                    startActivity(transition);
//                }
//            });

//    private Thread checkState = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            while (true) {
//                if (mapReady && detailsReady) {
//                    if(transitionThread.isAlive()) {
//                        transitionThread.interrupt();
//                    }else{
//                        break;
//                    }
//                }
//            }
//        }
//    });

    private DataUpdateApplication application = DataUpdateApplication.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "created!");
        application.liveMapData.addObserver(this);
        application.detailsViewData.detailsViewHeaderData.addObserver(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        transitionHandler.postDelayed(transitionRunnable, SPLASH_DURATION);

        WebView gifView = (WebView) findViewById(R.id.gif_view);
        gifView.loadUrl("file:///android_asset/ic_animated_logo.gif");
        gifView.getSettings().setBuiltInZoomControls(false);
        gifView.getSettings().setDisplayZoomControls(false);
        gifView.getSettings().setUseWideViewPort(true);
        gifView.setInitialScale(1);
        gifView.setPadding(0, 0, 0, 0);

        ProgressBar loader = (ProgressBar) findViewById(R.id.loading);
        loader.setIndeterminate(true);

//        transitionThread.run();
//        checkState.run();
    }


    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof DetailsViewHeaderData) {
            detailsReady = true;
        } else if (observable instanceof LiveMapData) {
            if (!((LiveMapData) observable).liveMapStaticRoutePackages.isEmpty()) {
                mapReady = true;
            }
        }
    }

    @Override
    protected void onPause() {
        transitionHandler.removeCallbacks(transitionRunnable);
        super.onPause();
    }
}
