package com.ek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ek.adapter.ShowHistoryOnlyAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.PageResultModel;
import com.ek.model.SelectSoLineModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowHistoryOnlyActivity extends AppCompatActivity implements View.OnClickListener{
    public static final int SELECTED_COMPLETE = 2;
    ShowHistoryOnlyAdapter adapter;
    OkHttpClient client;
    EditText edit_prd_no, edit_Z_work_no, edit_so_no;
    CheckBox edit_show_jk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history_only);

        client = new OkHttpClient();
        adapter = new ShowHistoryOnlyAdapter(new ArrayList<OnlyNoItem>());

        Button btnSearch = findViewById(R.id.btn_search);
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btn_update = findViewById(R.id.btn_update);
        //Button btn_reprint = findViewById(R.id.btn_reprint);

        btnSearch.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        //btn_reprint.setOnClickListener(this);

        edit_so_no = findViewById(R.id.edit_so_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        edit_show_jk = findViewById(R.id.edit_show_jk);

        ListView lv = findViewById(R.id.listView);
        lv.setAdapter(adapter);

//        OnlyNoItem temp = new OnlyNoItem();
//        temp.only_no = "Tjl123456";
//        temp.prd_no = "VgB12+2";
//        temp.FD_width = 820d;
//        temp.FD_length = 2000d;
//        temp.qty = 1;
//        temp.qty1 = 26.3d;
//        temp.FD_core = 3.0d;
//        temp.lock_table_id = "SO";
//        temp.lock_table_no = "SO190166001";
//        temp.lock_table_itm = "1";
//        temp.Z_work_no = "97888-001";
//
//        adapter.add(temp);
//
//        OnlyNoItem temp2 = new OnlyNoItem();
//        temp2.only_no = "Tjl1234534346";
//        temp2.qty = 1;
//        temp2.qty1 = 12.1;
//        temp.prd_no = "VgB12+3";
//        adapter.add(temp2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OnlyNoItem line = (OnlyNoItem) adapter.getItem(position);
                adapter.setSelctItem(position);
            }
        });
    }



    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.btn_delete:
                android.content.DialogInterface.OnClickListener c1 = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete();
                    }
                };
                beforeDelete(c1);
                break;
            case R.id.btn_update:
                Toast.makeText(getApplicationContext(),String.format("功能开发中，亲"), Toast.LENGTH_LONG);
                break;
            case R.id.btn_search:
                LoadData();
                break;
        }
    }


    void LoadData()
    {
//        string machine = Request["machine"];
//        string Z_work_no = Request["Z_work_no"];
//        string FD_width = Request["FD_width"];
//        string prd_no = Request["prd_no"];
//        string wh_no = Request["wh_no"];
//        string sal_no = Request["sal_no"];
//        string only_no = Request["only_no"];
//        string table_dd = Request["table_dd"];

        //机台, 成品 或 退料？ machine GetNotFinishBack GetNotFinishJK
        boolean is_JK = edit_show_jk.isChecked();
        String prd_no = edit_prd_no.getText().toString();
        String z_work_no = edit_Z_work_no.getText().toString();
        String so_no = edit_so_no.getText().toString();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        if(is_JK == true)
            paramsMap.put("action","GetNotFinishJK");
        else
            paramsMap.put("action","GetNotFinishBack");

        paramsMap.put("Z_work_no", z_work_no);
        paramsMap.put("so_no", so_no);
        paramsMap.put("prd_no", prd_no);
        paramsMap.put("wh_no", "");
        paramsMap.put("sal_no", "");
        paramsMap.put("only_no", "");
        paramsMap.put("table_dd", "");

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            Log.d("test", "key="+key+", val="+paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJOB))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载卷料出错"), Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                Log.d("gson", respTxt);

                final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.removeAll();
                        if (onlys != null && onlys.length>0)
                        {
                            for(int i = 0; i<onlys.length; ++i) {
                                adapter.add(onlys[i]);
                            }
                        }
                    }
                });
            }
        });
    }


    void doDelete(){

        OnlyNoItem only = adapter.getSelectFirst();
        final String only_no = only.only_no;

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","DeleteJL");
        paramsMap.put("only_no", only.only_no);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            Log.d("test", "key="+key+", val="+paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJOB))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("删除卷料出错"), Toast.LENGTH_LONG);
                        LoadData();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                Log.d("gson", respTxt);
                final NormalResult model = gson.fromJson(respTxt, NormalResult.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(model.result == true) {
                            Toast.makeText(getApplicationContext(),String.format("成功删除卷料["+ only_no + "]"), Toast.LENGTH_LONG);
                            LoadData();
                        }
                        else
                            Toast.makeText(getApplicationContext(),model.msg, Toast.LENGTH_LONG);
                    }
                });
            }
        });

    }

    //提示 超缴库 确认
    //, android.content.DialogInterface.OnClickListener callback2
    protected void beforeDelete(android.content.DialogInterface.OnClickListener callback) {

        OnlyNoItem only = adapter.getSelectFirst();
        if(only == null){
            Toast.makeText(getApplicationContext(),String.format("请先选择卷料 "), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ShowHistoryOnlyActivity.this);
        builder.setMessage("确定删除吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认",
                callback);

        builder.setNegativeButton("取消",new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),String.format("选  取消 "), Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

}
