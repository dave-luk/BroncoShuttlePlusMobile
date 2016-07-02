package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.util.ArrayList;

/**
 * Created by David on 2/4/2016.
 */
public class busViewExpandableListViewAdapter extends BaseExpandableListAdapter{

    private Context context;

    private ArrayList<String> headers;
    private ArrayList<ArrayList<BusInfo>> masterList;

    public busViewExpandableListViewAdapter(Context ctx, ArrayList<String> headers, ArrayList<ArrayList<BusInfo>> masterList)
    {
        this.context = ctx;
        this.headers = headers;
        this.masterList = masterList;
    }

    public void removeAll()
    {
        headers.clear();
        headers.clear();
        notifyDataSetChanged();
    }

    public void add(ArrayList list, String route)
    {
        masterList.get(headers.indexOf(route)).addAll(list);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return masterList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return masterList.get(groupPosition).get(childPosition);
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

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_bus_group, null);
        }

        TextView header = (TextView) convertView.findViewById(R.id.busLblListHeader);
        header.setTypeface(null, Typeface.BOLD);
        header.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        BusInfo busInfo = (BusInfo) getChild(groupPosition,childPosition);
        BusViewHolder busViewHolder;

        if (convertView == null) {
            busViewHolder = new BusViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_bus_item, parent,false);
            convertView.setTag(busViewHolder);

            busViewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_bus_box);
            busViewHolder.busName = (TextView) convertView.findViewById(R.id.item_bus_name);
            busViewHolder.fullness = (TextView) convertView.findViewById(R.id.item_bus_fullness);
            busViewHolder.lastUpdate = (TextView) convertView.findViewById(R.id.item_bus_lastUpdate);
            busViewHolder.nextStop = (TextView) convertView.findViewById(R.id.item_bus_nextStop);
        }
        else{
            busViewHolder = (BusViewHolder) convertView.getTag();
        }

        busViewHolder.busName.setText(busInfo.getBusName());
        String fullnessStr;
        int x;
        if ((x = busInfo.getFullness()) < 77) {
            fullnessStr = Integer.toString(x) + "% full. approx. " + (30 - x * (30) / 100) + "/30 seats left";
        } else {
            fullnessStr = Integer.toString(x) + "% full. Full seated.";
        }

        busViewHolder.fullness.setText(fullnessStr);
        busViewHolder.nextStop.setText("To : " + busInfo.getNextStop());
        busViewHolder.lastUpdate.setText(Integer.toString(busInfo.getLastUpdate()) + " s ago");
        busViewHolder.nextStop.setText("To : " + busInfo.getNextStop());
        busViewHolder.lastUpdate.setText(Integer.toString(busInfo.getLastUpdate()) + " s ago");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static final class BusViewHolder {
        LinearLayout mainBox;
        TextView busName;
        TextView fullness;
        TextView lastUpdate;
        TextView nextStop;
    }
}
