package com.ek.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.OnlyNoItem;
import com.ek.model.SoLineModel;

import java.util.List;

public class SOLineListAdapter extends BaseAdapter {
    List<SoLineModel> list;

    public SOLineListAdapter(List<SoLineModel> list)
    {
        this.list = list;
    }

    public void add(SoLineModel item)
    {
        list.add(item);
        notifyDataSetChanged();
    }

    public void remove(SoLineModel item)
    {
        list.remove(item);
        notifyDataSetChanged();
    }

    public void removeAll()
    {
        list.clear();
        notifyDataSetChanged();
    }

    public Boolean has(String so_no, Integer itm){
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).so_no.equals( so_no)  && list.get(i).itm.equals( itm) )
                return true;
        }

        return false;
    }

    public List<SoLineModel> getList()
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
            convertView = View.inflate(parent.getContext(), R.layout.select_soline_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String showText = list.get(position).Z_work_no + " 货号:"
                +  list.get(position).prd_no.toString() + "   "
                +  list.get(position).FD_width.toString() + "*" +  list.get(position).FD_length.toString()
                +  "纸芯:"+list.get(position).FD_core.toString()
                +  "订单号:"+ list.get(position).so_no.toString()
                +  "卷数:"+ list.get(position).qty_jk.toString()  + "/" + list.get(position).qty.toString() ;

        holder.text.setText(showText);
        holder.box_prd_no.setText(list.get(position).prd_no);
        if (list.get(position).isSelected ) {
            holder.box_prd_no.setBackgroundColor(Color.rgb(0, 255, 20));
        }
        else
        {
            holder.box_prd_no.setBackgroundColor(Color.rgb(255, 255, 255));
        }
        holder.box_FD_width.setText(list.get(position).FD_width+"");
        return convertView;
    }

    class ViewHolder {
        TextView text, box_prd_no, box_FD_width;


        ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.tv_showbox);
            box_prd_no = (TextView) view.findViewById(R.id.lable_prd_no);
            box_FD_width= (TextView) view.findViewById(R.id.lable_FD_width);
        }
    }
}
