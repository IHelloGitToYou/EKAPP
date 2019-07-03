package com.ek.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.HistoryJLModel;

import java.util.List;

public class HistoryJLAdapter extends BaseAdapter {
    List<HistoryJLModel> list;

    public HistoryJLAdapter(List<HistoryJLModel> list)
    {
        this.list = list;
    }

    public void add(HistoryJLModel item)
    {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).isNew = false;
        }
        list.add(item);
        notifyDataSetChanged();
    }

    public void add(Integer index, HistoryJLModel item)
    {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).isNew = false;
        }
        list.add(index, item);

        notifyDataSetChanged();
    }

    public void remove(HistoryJLModel item)
    {
        list.remove(item);
        notifyDataSetChanged();
    }

    public void removeAll()
    {
        list.clear();
        notifyDataSetChanged();
    }


    public List<HistoryJLModel> getList()
    {
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.history_jl_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.get(position).isNew ) {
            holder.text.setBackgroundColor(Color.rgb(0, 250, 20));
        }
        else
        {
            holder.text.setBackgroundColor(Color.rgb(255, 255, 255));
        }

        holder.text.setText(list.get(position).showMsg);
        return convertView;
    }

    class ViewHolder {
        TextView text;

        ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.label_text);
        }
    }
}
