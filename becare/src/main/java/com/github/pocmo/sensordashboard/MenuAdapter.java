package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.model.WellnessTask;

import java.util.ArrayList;

/**
 * Created by neerajpaliwal on 04/05/16.
 */
public class MenuAdapter extends ArrayAdapter<WellnessTask> {

    private Context context;

    public MenuAdapter(Context context) {
        super(context, 0, AppConfig.ALL_TAKSK);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WellnessTask task = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_menu_tiles, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.menu_title);
            viewHolder.description = (TextView) convertView.findViewById(R.id.menu_desc);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.menu_icon);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(openTaskListner);
        viewHolder.name.setText(context.getText(task.getNameResId()));
        viewHolder.description.setText(context.getText(task.getDescription()));
        viewHolder.icon.setImageResource(task.getIconResId());
        viewHolder.taskIndex = position;
        convertView.setTag(viewHolder);

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name, description;
        ImageView icon;
        int taskIndex;
    }

    View.OnClickListener openTaskListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            WellnessTask task = AppConfig.ALL_TAKSK.get( holder.taskIndex);
            Intent intent = new Intent(context, task.getClazzName());
            context.startActivity(intent);
        }
    };
}
