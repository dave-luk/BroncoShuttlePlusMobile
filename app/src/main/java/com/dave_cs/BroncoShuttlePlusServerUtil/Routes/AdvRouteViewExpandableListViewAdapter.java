package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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

    public AdvRouteViewExpandableListViewAdapter(Context context, List<String> headers, List<BusInfo> busInfos, List<StopInfo> stopInfos) {
        this.context = context;
        this.headers = headers;
        this.busInfoList = busInfos;
        this.stopInfoList = stopInfos;
    }

    public void removeAll() {
        busInfoList.clear();
        stopInfoList.clear();
        notifyDataSetChanged();
    }

    public void add(List list)
    {
        if (!list.isEmpty()) {
            if (list.get(0) instanceof BusInfo) {
                busInfoList.addAll(list);
            } else if (list.get(0) instanceof StopInfo) {
                stopInfoList.addAll(list);
            }
            notifyDataSetChanged();
        }
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
        switch (groupPosition) {
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

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.routelblListHeader);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        switch (groupPosition) {
            case 0:
                BusInfo busInfo = busInfoList.get(childPosition);
                BusViewHolder busViewHolder;

                if (convertView == null || convertView.getTag() instanceof StopViewHolder) {
                    busViewHolder = new BusViewHolder();

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_bus_item, parent, false);
                    convertView.setTag(busViewHolder);

                    busViewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_bus_box);
                    busViewHolder.busName = (TextView) convertView.findViewById(R.id.item_bus_name);
                    busViewHolder.fullness = (TextView) convertView.findViewById(R.id.item_bus_fullness);
                    busViewHolder.lastUpdate = (TextView) convertView.findViewById(R.id.item_bus_lastUpdate);
                    busViewHolder.nextStop = (TextView) convertView.findViewById(R.id.item_bus_nextStop);
                } else {
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

                break;
            case 1:
                StopInfo stopInfo = stopInfoList.get(childPosition);
                StopViewHolder stopViewHolder;

                if (convertView == null || convertView.getTag() instanceof BusViewHolder) {
                    stopViewHolder = new StopViewHolder();

                    LayoutInflater inflater = LayoutInflater.from(context);
                    convertView = inflater.inflate(R.layout.item_stop_item, parent, false);
                    convertView.setTag(stopViewHolder);

                    stopViewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_stop_box);
                    stopViewHolder.stopName = (TextView) convertView.findViewById(R.id.item_stop_name);
                    stopViewHolder.nextBus = (TextView) convertView.findViewById(R.id.item_stop_next);
                    stopViewHolder.nextBusTime = (TextView) convertView.findViewById(R.id.item_stop_next_time);
                } else {
                    stopViewHolder = (StopViewHolder) convertView.getTag();
                }
                // Populate the data into the template view using the data object
//                LinearLayout mainBox = (LinearLayout) convertView.findViewById(R.id.item_stop_box);
                stopViewHolder.stopName.setText(stopInfo.getName());
                int timeToNext = stopInfo.getTimeToNext();
                String nextBus, nextTime;
                if (timeToNext < 0) {
                    nextBus = "OUT OF SERVICE";
                    nextTime = "";
                } else {
                    nextBus = stopInfo.getNextBusOfRoute() + " bus in";
                    nextTime = Integer.toString(timeToNext) + " s";
                }

                stopViewHolder.nextBus.setText(nextBus);
                stopViewHolder.nextBusTime.setText(nextTime);
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

    public static final class BusViewHolder {
        LinearLayout mainBox;
        TextView busName;
        TextView fullness;
        TextView lastUpdate;
        TextView nextStop;
    }

    public static final class StopViewHolder {
        LinearLayout mainBox;
        TextView stopName;
        TextView nextBus;
        TextView nextBusTime;
    }
}
