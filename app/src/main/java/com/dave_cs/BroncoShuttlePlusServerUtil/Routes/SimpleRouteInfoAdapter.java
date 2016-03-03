package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.util.ArrayList;

/**
 * Created by David on 1/21/2016.
 */
public class SimpleRouteInfoAdapter extends ArrayAdapter<SimpleRouteInfo> {
    public SimpleRouteInfoAdapter(Context context, ArrayList<SimpleRouteInfo> users) {
        super(context, R.layout.item_route_item, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SimpleRouteInfo simpleRouteInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_route_item, parent, false);
            viewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_route_box);
            viewHolder.routeName = (TextView) convertView.findViewById(R.id.item_route_name);
            viewHolder.routeHours = (TextView) convertView.findViewById(R.id.item_route_hours);
            viewHolder.busCount = (TextView) convertView.findViewById(R.id.item_route_bus);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object\
        int bg = (simpleRouteInfo.isInService()) ? Color.parseColor("#7724AB84") : Color.parseColor("#77851010");

        viewHolder.mainBox.setBackgroundColor(bg);

        viewHolder.routeName.setText(simpleRouteInfo.getRouteName());
        viewHolder.routeHours.setText(simpleRouteInfo.getRouteHours());
        if (simpleRouteInfo.getRouteHours().equals("OUT OF SERVICE") || !simpleRouteInfo.isInService()) {
            viewHolder.busCount.setText("");
        }
        else{
            viewHolder.busCount.setText("Bus on route: " + simpleRouteInfo.getBusCount());
        }
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        LinearLayout mainBox;
        TextView routeName;
        TextView routeHours;
        TextView busCount;
    }
}