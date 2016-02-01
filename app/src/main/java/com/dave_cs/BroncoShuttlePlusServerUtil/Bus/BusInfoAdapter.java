package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.util.List;

/**
 * Created by David on 1/27/2016.
 */
public class BusInfoAdapter extends ArrayAdapter<BusInfo>{

    private static class ViewHolder{
        LinearLayout mainBox;
        TextView busName;
        TextView fullness;
        TextView lastUpdate;
        TextView nextStop;
    }

    public BusInfoAdapter(Context context, List<BusInfo> bus) {
        super(context, R.layout.item_bus_item, bus);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BusInfo busInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_bus_item, parent, false);
            viewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_bus_box);
            viewHolder.busName = (TextView) convertView.findViewById(R.id.item_bus_name);
            viewHolder.fullness = (TextView) convertView.findViewById(R.id.item_bus_fullness);
            viewHolder.lastUpdate = (TextView) convertView.findViewById(R.id.item_bus_lastUpdate);
            viewHolder.nextStop = (TextView) convertView.findViewById(R.id.item_bus_nextStop);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object

        viewHolder.busName.setText(busInfo.getBus());
        viewHolder.fullness.setText(Integer.toString(busInfo.getFullness()) + "%");
        viewHolder.nextStop.setText(busInfo.getNextStop());
        // Return the completed view to render on screen
        return convertView;
    }
}
