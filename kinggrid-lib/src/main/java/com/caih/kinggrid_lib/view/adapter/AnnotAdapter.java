package com.caih.kinggrid_lib.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebensz.eink.R;


public class AnnotAdapter extends BaseAdapter {

    private Context context;
    private String[] commonAnnots;

    public AnnotAdapter(Context context, String[] commonAnnots){
        this.context = context;
        this.commonAnnots = commonAnnots;
    }

    @Override
    public int getCount() {
        return commonAnnots.length;
    }

    @Override
    public Object getItem(int i) {
        return commonAnnots[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.list_item_annot, viewGroup, false);
            viewHolder.tvAnnot = view.findViewById(R.id.tvAnnot);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvAnnot.setText(commonAnnots[i]);
        return view;
    }

    class ViewHolder{
        TextView tvAnnot;
    }
}
