package com.ek.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ek.R;

public class MainAdapter extends BaseAdapter {

    String[] menus = new String[]{"卷料上架", "卷料出库", "生产机台","配置"};
    int[] icons = new int[]{ R.drawable.zpath , R.drawable.roll, R.drawable.roll, R.drawable.setting };

    @Override
    public int getCount() {
        return menus.length;
    }

    @Override
    public Object getItem(int position) {
        return menus[position].toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.menu_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(menus[position]);
        holder.icon.setImageResource(icons[position]);

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView text;

        ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.icon);
            text = (TextView) view.findViewById(R.id.txt_menu);
        }
    }
}
