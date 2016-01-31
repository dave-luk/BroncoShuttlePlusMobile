package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;

import java.util.List;

/**
 * Created by David on 1/30/2016.
 */
public class AdvRouteViewExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private List<BusInfo> busInfoList;
    private List<StopInfo> stopInfoList;

    public AdvRouteViewExpandableListViewAdapter(Context context, List<String> headers, List<BusInfo> busInfos, List<StopInfo> stopInfos)
    {
        this.context = context;
        this.headers = headers;
        this.busInfoList = busInfos;
        this.stopInfoList = stopInfos;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch(groupPosition)
        {
            case 0:
                return busInfoList.size();
            case 1:
                return stopInfoList.size();
            default:
                return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        switch (groupPosition)
        {
            case 0:
                return busInfoList.get(childPosition);
            case 1:
                return stopInfoList.get(childPosition);
            default:
                return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_route_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        switch(groupPosition) {
            case 0:
                BusInfo busInfo = busInfoList.get(childPosition);

                if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_bus_item, parent,false);
                }

//                LinearLayout mainBox = (LinearLayout) convertView.findViewById(R.id.item_bus_box);
                TextView busName = (TextView) convertView.findViewById(R.id.item_bus_name);
                TextView fullness = (TextView) convertView.findViewById(R.id.item_bus_fullness);
                TextView lastUpdate = (TextView) convertView.findViewById(R.id.item_bus_lastUpdate);
                TextView nextStop = (TextView) convertView.findViewById(R.id.item_bus_nextStop);

                busName.setText(busInfo.getBusName());
                fullness.setText(Integer.toString(busInfo.getFullness()) + "%");
                nextStop.setText(busInfo.getNextStop());
                lastUpdate.setText(Integer.toString(busInfo.getLastUpdate()) + " s");
                break;
            case 1:
                StopInfo stopInfo = stopInfoList.get(childPosition);

                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    convertView = inflater.inflate(R.layout.item_stop_item, parent, false);
                }
                // Populate the data into the template view using the data object
//                LinearLayout mainBox = (LinearLayout) convertView.findViewById(R.id.item_stop_box);
                TextView stopName = (TextView) convertView.findViewById(R.id.item_stop_name);
                TextView nextBus = (TextView) convertView.findViewById(R.id.item_stop_next);
                TextView nextBusTime = (TextView) convertView.findViewById(R.id.item_stop_next_time);

                stopName.setText(stopInfo.getName());
                nextBus.setText(stopInfo.getNextBusOfRoute());
                nextBusTime.setText(Integer.toString(stopInfo.getTimeToNext()));
                break;
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return (busInfoList.isEmpty() && stopInfoList.isEmpty());
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }
}
