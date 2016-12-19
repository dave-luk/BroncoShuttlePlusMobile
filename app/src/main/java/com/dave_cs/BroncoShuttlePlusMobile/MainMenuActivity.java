package com.dave_cs.BroncoShuttlePlusMobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusMobile.Details.ViewPagerDetailsViewActivity;
import com.dave_cs.BroncoShuttlePlusMobile.LiveMaps.LiveMapsActivity;
import com.dave_cs.BroncoShuttlePlusMobile.Options.OptionsActivity;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "MainMenuActivity";
    @Bind(R.id.liveMap_ImageButton)
    protected ImageButton mLiveMapButton;
    @Bind(R.id.detailsView_ImageButton)
    protected ImageButton mDetailsButton;
    @Bind(R.id.navigation_ImageButton)
    protected ImageButton mNavButton;
    @Bind(R.id.options_ImageButton)
    protected ImageButton mOptionsButton;
    @Bind(R.id.copyRight_TextView)
    protected TextView crText;
    @Bind(R.id.splash_screen)
    android.support.design.widget.CoordinatorLayout splash;
    @Bind(R.id.main_pane)
    RelativeLayout mainPane;
    //TODO: review splash screen construction
    private Handler uiCallback = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mainPane.setAlpha(0f);
            mainPane.setVisibility(View.VISIBLE);

            mainPane.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);

            splash.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            splash.setVisibility(View.GONE);
                        }
                    });

            setUp();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        //TODO: come back to this splash later
        final android.support.design.widget.CoordinatorLayout splash = (android.support.design.widget.CoordinatorLayout) findViewById(R.id.splash_screen);
//
//        splash.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                uiCallback.sendEmptyMessage(0);
//                return true;
//            }
//        });
        splash.setVisibility(View.GONE);
        mainPane.setVisibility(View.VISIBLE);
        setUp();
    }

    private void setUp() {
        mLiveMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "liveMap Pressed!");
                startActivity(new Intent(MainMenuActivity.this, LiveMapsActivity.class));
            }
        });

        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "details Pressed!");
                startActivity(new Intent(MainMenuActivity.this, ViewPagerDetailsViewActivity.class));
            }
        });

        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "navigation Pressed!");
                Toast.makeText(MainMenuActivity.this, getResources().getText(R.string.menu_wip), Toast.LENGTH_SHORT).show();
            }
        });

        mOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Options Pressed!");
                startActivity(new Intent(MainMenuActivity.this, OptionsActivity.class));
            }
        });

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            crText.setText(String.format(getResources().getString(R.string.main_menu_copyright), packageInfo.versionName));
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: clean up reading from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bgColorSettings = prefs.getString("bgColor", "green");
        int resID = getResources().getIdentifier(bgColorSettings, "color", getPackageName());
        if (findViewById(R.id.main_pane) != null)
            if (resID != 0)
                findViewById(R.id.main_pane).setBackgroundColor(ContextCompat.getColor(findViewById(R.id.main_pane).getContext(), resID));
            else {
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString("bgColor", "green");
                prefEditor.apply();
                findViewById(R.id.main_pane).setBackgroundColor(ContextCompat.getColor(findViewById(R.id.main_pane).getContext(), R.color.green));
            }
    }

}
