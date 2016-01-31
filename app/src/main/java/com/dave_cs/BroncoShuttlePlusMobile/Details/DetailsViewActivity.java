package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;

import com.dave_cs.BroncoShuttlePlusServerUtil.OnSwipeTouchListener;
import com.dave_cs.BroncoShuttlePlusMobile.R;

public class DetailsViewActivity extends FragmentActivity {

    private FragmentTabHost mDetailsTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details_view);

        findViewById(R.id.details_view_main_pane).setOnTouchListener(new OnSwipeTouchListener(DetailsViewActivity.this) {
            @Override
            public void onSwipeRight() {
                if (mDetailsTabHost.getCurrentTab() == 0) {
                    finish();
                } else {
                    mDetailsTabHost.setCurrentTab(mDetailsTabHost.getCurrentTab() - 1);
                }
                //   Call.Details.this.overridePendingTransition(R.anim.nothing, R.anim.nothing);
            }

            @Override
            public void onSwipeLeft() {
                if (mDetailsTabHost.getCurrentTab() < 2) {
                    mDetailsTabHost.setCurrentTab(mDetailsTabHost.getCurrentTab() + 1);
                }
            }

        });

        mDetailsTabHost = (FragmentTabHost)findViewById(R.id.tabHost);
        mDetailsTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("routes").setIndicator("Routes"), DetailsRouteFragmentTab.class, null);
        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("stops").setIndicator("Stops"), DetailsStopFragmentTab.class, null);
        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("bus").setIndicator("Shuttles"), DetailsBusFragmentTab.class, null);
    }
}
