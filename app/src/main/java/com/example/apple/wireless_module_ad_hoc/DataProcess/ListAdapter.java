package com.example.apple.wireless_module_ad_hoc.DataProcess;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.apple.wireless_module_ad_hoc.R;

import java.util.ArrayList;

/**
 * Created by easy_MAI on 16/10/27.
 */

public class ListAdapter extends BaseAdapter{

    Context context;
    ArrayList<String> strings;

    public ListAdapter(Context context, ArrayList<String> stringList){
        this.context=context;
        strings=new ArrayList<>(stringList);

    }

    public  void setList(ArrayList<String> stringList){
        strings=stringList;
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=LayoutInflater.from(context);
        convertView=inflater.inflate(R.layout.list_item,null);

        TextView textView=(TextView)convertView.findViewById(R.id.item);
        textView.setText(strings.get(position));

        return convertView;
    }
}
