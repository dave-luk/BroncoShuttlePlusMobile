package com.dave_cs.BroncoShuttlePlusMobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
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
    @Bind(R.id.main_pane)
    RelativeLayout mainPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setUp();
    }

    private void setUp() {
        mLiveMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "liveMap Pressed!");
                startActivity(new Intent(MainMenuActivity.this, LiveMapsActivity.class));
            }
        });

        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "details Pressed!");
                startActivity(new Intent(MainMenuActivity.this, ViewPagerDetailsViewActivity.class));
            }
        });

        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "navigation Pressed!");
                Toast.makeText(MainMenuActivity.this, getResources().getText(R.string.menu_wip), Toast.LENGTH_SHORT).show();
            }
        });

        mOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Options Pressed!");
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
        restorePreferences();
    }

    private void restorePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bgColorSettings = prefs.getString("bgColor", "green");
        //restore bg color
        int resID = getResources().getIdentifier(bgColorSettings, "color", getPackageName());
        if (mainPane != null)
            if (resID != 0)
                try {
                    mainPane.setBackgroundColor(ContextCompat.getColor(mainPane.getContext(), resID));
                } catch (Resources.NotFoundException nfe) {
                    mainPane.setBackgroundColor(ContextCompat.getColor(mainPane.getContext(), R.color.green));
                }
            else {
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString("bgColor", "green");
                prefEditor.apply();
                mainPane.setBackgroundColor(ContextCompat.getColor(mainPane.getContext(), R.color.green));
            }
    }

}
