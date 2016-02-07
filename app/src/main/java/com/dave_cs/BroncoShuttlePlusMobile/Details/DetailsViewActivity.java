package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Intent;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TabHost;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusServerUtil.OnSwipeTouchListener;
import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.util.ArrayList;
import java.util.List;

public class DetailsViewActivity extends FragmentActivity {

    private FragmentTabHost mDetailsTabHost;

    private List<OnSwipeTouchListener> listenerList;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(listenerList == null)
            listenerList = new ArrayList<>();

        setContentView(R.layout.activity_details_view);

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(DetailsViewActivity.this) {
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
        };

        registerSwipeListener(swipeTouchListener);
        findViewById(R.id.details_view_main_pane).setOnTouchListener(swipeTouchListener);

        mDetailsTabHost = (FragmentTabHost)findViewById(R.id.tabHost);
        mDetailsTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("routes").setIndicator("Routes"), DetailsRouteFragmentTab.class, null);
        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("stops").setIndicator("Stops"), DetailsStopFragmentTab.class, null);
        mDetailsTabHost.addTab(mDetailsTabHost.newTabSpec("bus").setIndicator("Shuttles"), DetailsBusFragmentTab.class, null);

        mDetailsTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Toast.makeText(DetailsViewActivity.this, "selected tab: " + tabId, Toast.LENGTH_SHORT).show();
                mDetailsTabHost.getTabContentView().invalidate();
                switch (tabId)
                {
                    case "routes":

                }
            }
        });
    }

    public void registerSwipeListener(OnSwipeTouchListener listener)
    {
        listenerList.add(listener);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent me)
    {
        for( OnSwipeTouchListener swipeTouchListener : listenerList)
        {
            swipeTouchListener.onTouch(null,me);
        }
        return super.dispatchTouchEvent(me);
    }

}
