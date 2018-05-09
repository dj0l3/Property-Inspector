package com.sakthi.propertyinspector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakthi.propertyinspector.R;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PropertyInfo;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Satheesh on 12/06/17.
 * Sanghish IT Solutions
 * satheesh@sanghish.com
 */
public class ListInspectedFilesAdapter extends RecyclerView.Adapter<InspectedFilesViewHolder> {

    private ArrayList<InspectedFiles> propertyInfoArrayList;
    private Context context;
    private LayoutInflater inflater;
    private View.OnClickListener onClickListener;

    public ListInspectedFilesAdapter(ArrayList<InspectedFiles> propertyInfoArrayList, Context context, View.OnClickListener onClickListener) {
        this.propertyInfoArrayList = propertyInfoArrayList;
        this.context = context;
        this.onClickListener = onClickListener;
        inflater = LayoutInflater.from(context);
    }

    public void refreshProperty(ArrayList<InspectedFiles> propertyInfoArrayList){
        this.propertyInfoArrayList = propertyInfoArrayList;
        notifyDataSetChanged();
    }

    @Override
    public InspectedFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.view_holder_inspected_propertys, parent, false);
        return new InspectedFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InspectedFilesViewHolder holder, int position) {
        holder.onBind(propertyInfoArrayList.get(position), position, onClickListener);
    }

    @Override
    public int getItemCount() {
        return propertyInfoArrayList != null ? propertyInfoArrayList.size() : 0;
    }
}
