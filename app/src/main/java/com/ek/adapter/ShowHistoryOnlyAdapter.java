package com.ek.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.OnlyNoItem;

import java.util.List;

public class ShowHistoryOnlyAdapter extends BaseAdapter {
    List<OnlyNoItem> list;

    public ShowHistoryOnlyAdapter(List<OnlyNoItem> list)
    {
        this.list = list;
    }

    public void add(OnlyNoItem item)
    {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).isSelected = false;
        }
        list.add(item);
        notifyDataSetChanged();
    }

    public void add(Integer index, OnlyNoItem item)
    {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).isSelected = false;
        }
        list.add(index, item);

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

    public void setSelctItem(int position) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).isSelected = false;
            notifyDataSetChanged();
        }
        list.get(position).isSelected= true;
        notifyDataSetChanged();
    }

    public  OnlyNoItem getSelectFirst(){
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).isSelected == true){
                return list.get(i);
            }
        }
        return null;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.show_history_only_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.get(position).isSelected ) {
            holder.label_only_no.setBackgroundColor(Color.rgb(0, 250, 20));
            holder.label_prd_no.setBackgroundColor(Color.rgb(0, 250, 20));
            holder.label_FD_width.setBackgroundColor(Color.rgb(0, 250, 20));
        }
        else
        {
            holder.label_only_no.setBackgroundColor(Color.rgb(255, 255, 255));
            holder.label_prd_no.setBackgroundColor(Color.rgb(255, 255, 255));
            holder.label_FD_width.setBackgroundColor(Color.rgb(255, 255, 255));
        }

        holder.label_only_no.setText(list.get(position).only_no);
        holder.label_prd_no.setText(list.get(position).prd_no+"");
        holder.label_Z_work_no.setText(list.get(position).Z_work_no+"");
        holder.label_FD_width.setText(list.get(position).FD_width+"*" + list.get(position).FD_length);
        holder.label_FD_core.setText(list.get(position).FD_core+"");
        holder.label_so_no.setText(list.get(position).lock_table_no+"");
        String temp = list.get(position).Z_core_kg  +"+"+ list.get(position).qty1 + "=" + list.get(position).Z_kg +"kg ";
        if(list.get(position).qty > 1)
            temp += "(" + list.get(position).qty  + "卷)";

        holder.label_show_qty.setText(temp);
        //holder.label_Z_print.setText(list.get(position).Z_print);


        return convertView;
    }

    class ViewHolder {
        TextView label_only_no, label_Z_work_no,  label_prd_no, label_FD_width,  label_FD_core, label_so_no, label_show_qty;


        ViewHolder(View view) {
            label_only_no = (TextView) view.findViewById(R.id.label_only_no);
            label_Z_work_no = (TextView) view.findViewById(R.id.label_Z_work_no);
            label_prd_no = (TextView) view.findViewById(R.id.label_prd_no);
            label_FD_width = (TextView) view.findViewById(R.id.label_FD_width);

            label_FD_core = (TextView) view.findViewById(R.id.label_FD_core);
            label_so_no = (TextView) view.findViewById(R.id.label_so_no);
            label_show_qty = (TextView) view.findViewById(R.id.label_show_qty);

        }
    }
}
