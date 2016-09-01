package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by David on 8/31/2016.
 */
public class NearestStopAdapter extends ArrayAdapter<StopLocation> {

    protected List<StopLocation> list;
    private Location currLocation;

    public NearestStopAdapter(Context context, List<StopLocation> objects, Location l) {
        super(context, R.layout.item_nearest_stop_item, objects);
        list = objects;
        currLocation = l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        StopLocation location = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_nearest_stop_item, parent, false);
            viewHolder.mainBox = (LinearLayout) convertView.findViewById(R.id.item_nearest_stop_box);
            viewHolder.stopName = (TextView) convertView.findViewById(R.id.item_nearest_stop_name);
            viewHolder.stopDist = (TextView) convertView.findViewById(R.id.item_nearest_stop_distance);
            viewHolder.stopDir_img = (ImageView) convertView.findViewById(R.id.item_nearest_stop_dir_img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.stopName.setText(location.getName());

        //compute dist
        DecimalFormat df = new DecimalFormat(".##");
        Float dist = location.getDist();
        String str = (dist % 1000 > 1) ? df.format(dist / 1000) + " km" : df.format(dist) + " m";
        viewHolder.stopDist.setText(str);

        //computer bearing
        float deg = (currLocation.getBearing() - location.getBearing()) % 360;

        viewHolder.stopDir_img.setImageResource(R.drawable.ic_navigation_icon);
        viewHolder.stopDir_img.setRotation(deg - 45);
        return convertView;
    }

    public int size() {
        return list.size();
    }

    private static class ViewHolder {
        LinearLayout mainBox;
        TextView stopName;
        TextView stopDist;
        ImageView stopDir_img;
    }
}
