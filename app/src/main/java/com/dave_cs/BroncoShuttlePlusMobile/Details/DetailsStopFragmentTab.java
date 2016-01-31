package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsStopFragmentTab extends android.support.v4.app.Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.details_stop_fragment_layout, container, false);
            ListView listView = (ListView) v.findViewById(R.id.stopList);
            //do propagation here
            return v;
        }

}
