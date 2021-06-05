package com.liaou.getrssidemo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class BlDeviceListAdapter extends ArrayAdapter<BlDevice> {
    public BlDeviceListAdapter(Activity context, ArrayList<BlDevice> bldevice) {
        super(context, 0, bldevice);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.bldevice_list_item, parent, false);
        }
        BlDevice currentItem = getItem(position);
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.bl_name);
        nameTextView.setText(currentItem.name);
        TextView uuidTextView = (TextView) listItemView.findViewById(R.id.bl_uuid);
        uuidTextView.setText(String.valueOf(currentItem.uuid));
        TextView macView = (TextView) listItemView.findViewById(R.id.bl_mac);
        macView.setText(String.valueOf(currentItem.mac));
        return listItemView;
    }
}