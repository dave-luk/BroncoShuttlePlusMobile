package com.dave_cs.BroncoShuttlePlusMobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.dave_cs.BroncoShuttlePlusMobile.Details.DetailsViewActivity;
import com.dave_cs.BroncoShuttlePlusMobile.Options.OptionsActivity;


public class MainMenuActivity extends AppCompatActivity {

    private Intent currObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton mLiveMapButton = (ImageButton) findViewById(R.id.liveMapImageButton);
        ImageButton mDetailsButton = (ImageButton) findViewById(R.id.detailsViewImageButton);
        ImageButton mNavButton = (ImageButton) findViewById(R.id.navigationImageButton);
        ImageButton mOptionsButton = (ImageButton) findViewById(R.id.optionsImageButton);

        mLiveMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("debug", "liveMap Pressed!");
            }
        });

        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("debug", "details Pressed!");
                currObj = (new Intent(MainMenuActivity.this, DetailsViewActivity.class));
                startActivity(currObj);
            }
        });

        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("debug", "navigation Pressed!");
            }
        });

        mOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("debug", "Options Pressed!");
                currObj = (new Intent(MainMenuActivity.this, OptionsActivity.class));
                startActivity(currObj);
            }
        });

    }

    @Override
    protected  void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bgColorSettings = prefs.getString("bgColor", "green");
        int resID = getResources().getIdentifier(bgColorSettings,"color",getPackageName());
        if(resID != 0)
            findViewById(R.id.main_pane).setBackgroundColor(ContextCompat.getColor(findViewById(R.id.main_pane).getContext(),resID));
        else {
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString("bgColor","green");
            prefEditor.commit();
            findViewById(R.id.main_pane).setBackgroundColor(ContextCompat.getColor(findViewById(R.id.main_pane).getContext(), R.color.green));
        }
    }

}
