package com.dave_cs.BroncoShuttlePlusMobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.dave_cs.BroncoShuttlePlusMobile.Application.ApplicationReadyRelay;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by David on 12/22/2016.
 */

public class SplashActivity extends Activity implements Observer {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_CHECK_DURATION = 50;
    @Bind(R.id.wheel_image)
    ImageView wheelImage;
    private int remainingTime = 7000;
    private boolean finished = false;
    private DataUpdateApplication application = DataUpdateApplication.getInstance();
    private Handler transitionHandler = new Handler();
    private Runnable transitionRunnable = new Runnable() {
        @Override
        public void run() {
            if (remainingTime <= 0) {
                Intent transition = new Intent(SplashActivity.this, MainMenuActivity.class);

                application.applicationReadyRelay.deleteObserver(SplashActivity.this);

                startActivity(transition);
                finish();
            } else {
                remainingTime -= SPLASH_CHECK_DURATION;
                transitionHandler.postDelayed(this, SPLASH_CHECK_DURATION);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "created!");
        //set up observer for ready
        application.applicationReadyRelay.addObserver(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ButterKnife.bind(this);
        //post setup.
        transitionHandler.postDelayed(transitionRunnable, SPLASH_CHECK_DURATION);

        //rotate wheel image
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        wheelImage.setAnimation(rotateAnimation);

        //set up imageView for gif
        ImageView gifView = (ImageView) findViewById(R.id.gif_view);
        Ion.with(gifView)
                .error(R.drawable.ic_bronco_shuttle)
                .animateGif(AnimateGifMode.ANIMATE)
                .load("file:///android_asset/ic_animated_logo.gif");
    }


    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof ApplicationReadyRelay) {
            Log.i(TAG, "Updating!");
            if (!finished) {
                Log.i(TAG, "finishing splash!");
                finished = true;
                remainingTime = 0;
                transitionHandler.removeCallbacks(transitionRunnable);
                transitionHandler.post(transitionRunnable);
            }
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "pausing!");
        if (!finished) {
            transitionHandler.removeCallbacks(transitionRunnable);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Resuming!");
        transitionHandler.postDelayed(transitionRunnable, SPLASH_CHECK_DURATION);
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "Stopping!");
        super.onStop();
    }
}
