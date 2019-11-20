package com.ek.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ek.R;
import com.ek.model.OnlyNoItem;
import com.ek.model.Sa_SelectModel;

import java.util.ArrayList;
import java.util.List;

public class Sa_SelectAdapter extends BaseAdapter {
    List<Sa_SelectModel> list;

    public Sa_SelectAdapter(List<Sa_SelectModel> list)
    {
        this.list = list;
    }

    public void add(Sa_SelectModel item)
    {
        list.add(item);
        updateSort();
        //notifyDataSetChanged();
    }

    public void insert(Sa_SelectModel item)
    {
        list.add(0, item);
        updateSort();

        notifyDataSetChanged();
    }

    private  void updateSort(){
        for (int i = 0; i < list.size(); i++) {
            list.get(i).sort = i + 1;
        }
    }

    public void remove(Sa_SelectModel item)
    {
        list.remove(item);
        updateSort();
        notifyDataSetChanged();
    }

    public void removeAll()
    {
        list.clear();
        notifyDataSetChanged();
    }

    public Boolean hasOnly(String only_no){
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).jls.size(); j++) {
                if(list.get(i).jls.get(j).only_no.equals( only_no))
                    return true;
            }
        }

        return false;
    }


    public  List<String> getAllOnlyNo(){
        List<String> onlys = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).jls.size(); j++) {
                onlys.add(list.get(i).jls.get(j).only_no);
            }
        }

        return onlys;
    }

    public List<Sa_SelectModel> getList()
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

        //Double qty1 = Math.round((list.get(position).qty1) *100.00)/100.00;

        Sa_SelectModel model =  list.get(position);

        String showText =  "     " +model.sort + "：" + (model.type == "JL" ? "卷" :"托盘" )+ "   " + model.no + "   " + model.valid_qty.toString() ;//+ () + model.error_qty.toString();
        if(model.error_qty != 0){
            showText += String.format("(异常%s卷)", model.error_qty.toString());
        }

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
