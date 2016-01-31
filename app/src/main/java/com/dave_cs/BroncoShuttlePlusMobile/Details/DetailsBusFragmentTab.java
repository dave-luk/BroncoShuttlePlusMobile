package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsBusFragmentTab extends android.support.v4.app.Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.details_bus_fragment_layout, container, false);
            ListView listView = (ListView) v.findViewById(R.id.busList);
            return v;
        }
}

