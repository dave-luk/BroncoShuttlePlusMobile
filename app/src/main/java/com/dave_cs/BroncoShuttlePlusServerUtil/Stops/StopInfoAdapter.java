package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;

import java.util.List;

/**
 * Created by David on 1/30/2016.
 */
public class StopInfoAdapter extends ArrayAdapter<StopInfo> {

    private static class ViewHolder{
        LinearLayout mainBox;
        TextView stopName;
        TextView nextBus;
        TextView nextBusTime;
    }

    public StopInfoAdapter(Context context, List<StopInfo> stops)
    {
        super(context, R.layout.item_stop_item, stops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StopInfo stopInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_stop_item, parent, false);
            viewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_stop_box);
            viewHolder.stopName = (TextView) convertView.findViewById(R.id.item_stop_name);
            viewHolder.nextBus = (TextView) convertView.findViewById(R.id.item_stop_next);
            viewHolder.nextBusTime = (TextView) convertView.findViewById(R.id.item_stop_next_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object

        viewHolder.stopName.setText(stopInfo.getName());
        viewHolder.nextBus.setText(stopInfo.getNextBusOfRoute());
        viewHolder.nextBusTime.setText(Integer.toString(stopInfo.getTimeToNext()));
        // Return the completed view to render on screen
        return convertView;
    }
}
