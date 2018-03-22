package com.webmarke8.app.gencartdriver.Adapter;

/**
 * Created by Asus on 2/1/2017.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.webmarke8.app.gencartdriver.Fragments.Earnings;
import com.webmarke8.app.gencartdriver.Fragments.History;
import com.webmarke8.app.gencartdriver.Fragments.History_Details;
import com.webmarke8.app.gencartdriver.R;

import java.util.List;

public class EarningsAdapter extends RecyclerView.Adapter<EarningsAdapter.ViewHolder> {

    Activity context;
    List<Earnings> models;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        TextView Click;

        public ViewHolder(View v) {
            super(v);

        }
    }


    public EarningsAdapter(Activity context, List<Earnings> models) {
        this.context = context;
        this.models = models;
        ;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_earning, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {


    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}