package com.webmarke8.app.gencartdriver.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.webmarke8.app.gencartdriver.Adapter.HistoryAdapter;
import com.webmarke8.app.gencartdriver.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class History extends Fragment {


    RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    List<History> models;
    private RecyclerView.LayoutManager mLayoutManager;

    public History() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new HistoryAdapter(getActivity(), models);
        recyclerView.setAdapter(adapter);
        return view;
    }
}
