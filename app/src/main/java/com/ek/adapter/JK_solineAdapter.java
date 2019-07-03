package com.ek.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.SelectSoLineModel;

import java.util.List;

public class JK_solineAdapter extends BaseAdapter {
    List<SelectSoLineModel> list;

    public JK_solineAdapter(List<SelectSoLineModel> list)
    {
        this.list = list;
    }

    public void add(SelectSoLineModel item)
    {
        list.add(item);
        notifyDataSetChanged();
    }

    public void remove(SelectSoLineModel item)
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

    public List<SelectSoLineModel> getList()
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

    public void ClearSelected(){
        for (int i = 0; i < list.size() ; i++) {
            list.get(i).isSelected = false;
        }
        notifyDataSetChanged();
    }

    public SelectSoLineModel GetSelecteted(){
        for (int i = 0; i < list.size() ; i++) {
            if(list.get(i).isSelected == true)
                return list.get(i);
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
            convertView = View.inflate(parent.getContext(), R.layout.jk_soline_itm, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.get(position).isSelected ) {
            holder.label_Z_work_no.setBackgroundColor(Color.rgb(0, 255, 20));
            holder.label_prd_no.setBackgroundColor(Color.rgb(0, 255, 20));
            //holder.label_FD_width.setBackgroundColor(Color.rgb(0, 255, 20));
        }
        else
        {
            holder.label_Z_work_no.setBackgroundColor(Color.rgb(255, 255, 255));
            holder.label_prd_no.setBackgroundColor(Color.rgb(255, 255, 255));
            // holder.label_FD_width.setBackgroundColor(Color.rgb(255, 255, 255));
        }
        holder.edit_isSelected.setChecked( list.get(position).isSelected);

        holder.label_prd_no.setText(list.get(position).prd_no);
        holder.label_Z_work_no.setText(list.get(position).Z_work_no+"");
        holder.label_FD_width.setText(list.get(position).FD_width+"*" + list.get(position).FD_length);
        holder.label_FD_core.setText(list.get(position).FD_core+"");
        holder.label_so_no.setText(list.get(position).so_no+"");
        holder.label_show_qty.setText(list.get(position).qty_jk + " / 已缴:" + list.get(position).qty);
        holder.label_Z_print.setText(list.get(position).Z_print + "");
        return convertView;
    }

    class ViewHolder {
        TextView text, label_Z_work_no,  label_prd_no, label_FD_width,  label_FD_core, label_so_no, label_show_qty,label_Z_print;

        CheckBox edit_isSelected;
        ViewHolder(View view) {
            edit_isSelected  = (CheckBox) view.findViewById(R.id.edit_isSelected);

            label_Z_work_no = (TextView) view.findViewById(R.id.label_Z_work_no);
            label_prd_no = (TextView) view.findViewById(R.id.label_prd_no);
            label_FD_width = (TextView) view.findViewById(R.id.label_FD_width);

            label_FD_core = (TextView) view.findViewById(R.id.label_FD_core);
            label_so_no = (TextView) view.findViewById(R.id.label_so_no);
            label_show_qty = (TextView) view.findViewById(R.id.label_show_qty);
            label_Z_print = (TextView) view.findViewById(R.id.label_Z_print);
        }
    }
}
