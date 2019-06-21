package com.ek.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.OnlyNoItem;

import java.util.List;

public class OnlyNoListAdapter extends BaseAdapter {
    List<OnlyNoItem> list;

    public OnlyNoListAdapter(List<OnlyNoItem> list)
    {
        this.list = list;
    }

    public void add(OnlyNoItem item)
    {
        list.add(item);
        notifyDataSetChanged();
    }

    public void remove(OnlyNoItem item)
    {
        list.remove(item);
        notifyDataSetChanged();
    }

    public void removeAll()
    {
        list.clear();
        notifyDataSetChanged();
    }

    public Boolean hasOnly(String only_no){
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).only_no.equals( only_no))
                return true;
        }

        return false;
    }

    public List<OnlyNoItem> getList()
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
            convertView = View.inflate(parent.getContext(), R.layout.only_no_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String showText = list.get(position).only_no + "   "+ list.get(position).FD_width.toString()
                + "*" +  list.get(position).FD_length.toString() + " 货号:" +  list.get(position).prd_no.toString()+
                "  " + list.get(position).Z_kg.toString() + "kg"  ;

        holder.text.setText(showText);
        return convertView;
    }

    class ViewHolder {
        TextView text;

        ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.tv_only_no);
        }
    }
}
