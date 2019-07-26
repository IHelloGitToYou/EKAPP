package com.ek;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.ek.adapter.HistoryJLAdapter;
import com.ek.adapter.SOLineListAdapter;
import com.ek.model.OnlyNoItem;
import com.ek.model.SelectSoLineModel;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class HistoryJLActivity extends AppCompatActivity {

    OkHttpClient client;
    HistoryJLAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_jl_layout);
        ListView lv = findViewById(R.id.listView);

//        client = new OkHttpClient();
//        adapter = new HistoryJLAdapter(new ArrayList<OnlyNoItem>());
//
//        OnlyNoItem temp = new OnlyNoItem();
//        temp.only_no = "Tjl123456";
//        adapter.add(temp);
    }

}
