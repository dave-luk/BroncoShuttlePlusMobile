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

import java.util.List;

/**
 * Created by David on 2/4/2016.
 */
public class busViewExpandableListViewAdapter extends BaseExpandableListAdapter{

    private Context context;

    private List<String> headers;
    private List<BusInfo> listA;
    private List<BusInfo> listB1;
    private List<BusInfo> listB2;
    private List<BusInfo> listC;

    public static final class BusViewHolder{
        LinearLayout mainBox;
        TextView busName;
        TextView fullness;
        TextView lastUpdate;
        TextView nextStop;
    }

    public busViewExpandableListViewAdapter(Context ctx, List<String> headers, List<BusInfo> A, List<BusInfo> B1,List<BusInfo> B2,List<BusInfo> C)
    {
        this.context = ctx;
        this.headers = headers;
        this.listA = A;
        this.listB1 = B1;
        this.listB2 = B2;
        this.listC = C;
    }

    public void removeAll()
    {
        listA.clear();
        listB1.clear();
        listB2.clear();
        listC.clear();
        notifyDataSetChanged();
    }

    public void add(List list, String route)
    {
        switch(route)
        {
            case "A":
                listA.addAll(list);
                notifyDataSetChanged();
                break;
            case "B1":
                listB1.addAll(list);
                notifyDataSetChanged();
                break;
            case "B2":
                listB2.addAll(list);
                notifyDataSetChanged();
                break;
            case "C":
                listC.addAll(list);
                notifyDataSetChanged();
                break;
        }
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
                return listA.size();
            case 1:
                return listB1.size();
            case 2:
                return listB2.size();
            case 3:
                return listC.size();
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
        switch(groupPosition)
        {
            case 0:
                return listA.get(childPosition);
            case 1:
                return listB1.get(childPosition);
            case 2:
                return listB2.get(childPosition);
            case 3:
                return listC.get(childPosition);
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
        busViewHolder.fullness.setText(Integer.toString(busInfo.getFullness()) + "%");
        busViewHolder.nextStop.setText(busInfo.getNextStop());
        busViewHolder.lastUpdate.setText(Integer.toString(busInfo.getLastUpdate()) + " s");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
