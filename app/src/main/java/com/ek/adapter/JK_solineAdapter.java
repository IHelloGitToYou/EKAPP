package com.ek.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.SoLineModel;

import java.util.List;

public class JK_solineAdapter extends BaseAdapter {
    List<SoLineModel> list;

    public JK_solineAdapter(List<SoLineModel> list)
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
            convertView = View.inflate(parent.getContext(), R.layout.jk_soline_itm, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.edit_isSelected.setChecked( list.get(position).isSelected);

        holder.box_prd_no.setText(list.get(position).prd_no);
        holder.box_FD_width.setText(list.get(position).FD_width+"");
        return convertView;
    }

    class ViewHolder {
        TextView text, box_prd_no, box_FD_width;
        CheckBox edit_isSelected;
        ViewHolder(View view) {
            edit_isSelected  = (CheckBox) view.findViewById(R.id.edit_isSelected);
            box_prd_no = (TextView) view.findViewById(R.id.lable_prd_no);
            box_FD_width= (TextView) view.findViewById(R.id.lable_FD_width);
        }
    }
}
