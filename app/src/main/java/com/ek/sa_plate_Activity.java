package com.ek;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ek.adapter.OnlyNoListAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class sa_plate_Activity extends AppCompatActivity implements View.OnClickListener{
    ListView listView;
    OnlyNoListAdapter adapter;
    EditText edit_only_no;
    EditText edit_Z_plate;
    OkHttpClient client;
    String LastZ_Plate = "";
    private  static  String lastNo;
    boolean IS_LOADING = false;
    boolean IS_SubmitZPlateIng = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sa_plate);
        edit_only_no = findViewById(R.id.edit_only_no);
        edit_Z_plate = findViewById(R.id.edit_Z_plate);
        listView = findViewById(R.id.listView);
        client = new OkHttpClient();


        edit_only_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.d("监听setOnKeyListener", "keycode =" + keyCode);
//                if (keyCode == KeyEvent.KEYCODE_BACK ) {
//                    sa_plate_Activity.this.finish();
//                    return false;
//                }

                if (edit_only_no.getText().toString().isEmpty())
                    return false;


                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if( IS_LOADING == true || IS_SubmitZPlateIng == true)
                        return false;

                    if(LastZ_Plate.isEmpty()){
                        Toast.makeText(getApplicationContext(),String.format("[托盘]请先输入"), Toast.LENGTH_LONG).show();
                        return  false;
                    }

                    submitZPlate(false, "null", null);
                }
                return false;
            }
        });


        edit_Z_plate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (edit_Z_plate.getText().toString().isEmpty()) {
                        adapter.removeAll();
                        return false;
                    }

                    String Z_plate = edit_Z_plate.getText().toString();
                    if(LastZ_Plate == Z_plate)
                        return false;
                    if( IS_LOADING == true || IS_SubmitZPlateIng == true)
                        return false;

                    IS_LOADING = true;
                    loadOnlys(Z_plate);
                }

                return false;
            }
        });


        listView.setAdapter(adapter = new OnlyNoListAdapter(new ArrayList<OnlyNoItem>()));

        //listView.setAdapter(adapter = new HistoryJLAdapter(new ArrayList<HistoryJLModel>()));
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showMenu((OnlyNoItem) adapter.getItem(position));
                return false;
            }
        });

        getRemember();
        focusToEditText();
    }

    //API 设托盘
    void submitZPlate(final boolean is_clear, String clearing_only_no, final OnlyNoItem clearing_item)
    {
        String z_plate = edit_Z_plate.getText().toString();
        if(z_plate.isEmpty()){
            Toast.makeText(getApplicationContext(),String.format("[托盘]请先输入"), Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        if(is_clear == false) {
            String only_no = edit_only_no.getText().toString();
            paramsMap.put("action", "MarkZ_Plate");
            paramsMap.put("only_no", only_no);
        }
        else {
            paramsMap.put("action", "ClearZ_Plate");
            paramsMap.put("only_no",clearing_only_no);
        }
        paramsMap.put("Z_plate", z_plate );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJK))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IS_SubmitZPlateIng = false;
                        Toast.makeText(getApplicationContext(),String.format("上托出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                IS_SubmitZPlateIng = false;

                final String respTxt =  response.body().string();
                //Log.d(respTxt,"---------");
                final Gson gson = new Gson();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(is_clear == true) {
                            final NormalResult result = gson.fromJson(respTxt, NormalResult.class);
                            if(result.result == true) {
                                Toast.makeText(getApplicationContext(), String.format("移出[%s]成功!", clearing_item.only_no), Toast.LENGTH_LONG).show();
                                edit_only_no.setText("");
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "移出托盘出错:" + result.msg, Toast.LENGTH_SHORT).show();
                            }
                            adapter.remove(clearing_item);
                        }

                        if(is_clear == false) {
                            if(respTxt.startsWith("[")){
                                Toast.makeText(getApplicationContext(), String.format("上托[%s]成功!", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                                OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                                adapter.insert(onlys[0]);
                                edit_only_no.setText("");
                            }else{
                                final NormalResult result2 = gson.fromJson(respTxt, NormalResult.class);
                                Toast.makeText(getApplicationContext(), "上托出错:" + result2.msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        focusToEditText();
                    }
                });

            }
        });
    }


    void loadOnlys(final String Z_plate){
        adapter.removeAll();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action", "GetOnlyByZ_Plate");
        paramsMap.put("Z_plate", Z_plate);
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJK))
                .post(builder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载托盘卷料出错"), Toast.LENGTH_LONG).show();
                        edit_Z_plate.setSelectAllOnFocus(true);
                        edit_Z_plate.requestFocus();
                        IS_LOADING = false;
                        LastZ_Plate = "";
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                IS_LOADING = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LastZ_Plate = Z_plate;
                        if (onlys != null && onlys.length > 0) {
                            for (int i = 0; i < onlys.length; i++) {
                                adapter.add(onlys[i]);
                            }
                        }
                    }
                });
            }
        });
    }



    public void showMenu(final OnlyNoItem item){
        String[] menuText = {"移出"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(menuText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    AlertDialog.Builder bulDelete = new AlertDialog.Builder(sa_plate_Activity.this);
                    bulDelete.setTitle("询问");
                    bulDelete.setMessage(String.format(String.format("确定从托盘中移出卷[%s]吗？", item.only_no)));

                    bulDelete.setPositiveButton("移出吧", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        submitZPlate(true, item.only_no, item);
                        dialog.dismiss();
                        }
                    });

                    bulDelete.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        }
                    });
                    bulDelete.show();
                }
                arg0.dismiss();
            }
        });
        dialog.show();
    }


    public void remember(){

//        SharedPreferences sp = getSharedPreferences("SA_PLATE_ACTIVITY", MODE_PRIVATE);
//        SharedPreferences.Editor spEditor = sp.edit();
//        spEditor.putString("Z_plate", edit_Z_plate.getText().toString());
//
//        spEditor.commit();
    }


    public  void getRemember(){
//        SharedPreferences sp = getSharedPreferences("SA_PLATE_ACTIVITY", MODE_PRIVATE);
//        String str = sp.getString("Z_plate", "");
//        edit_Z_plate.setText(str);
    }

    public void focusToEditText(){
        edit_only_no.setSelectAllOnFocus(true);
        edit_only_no.requestFocus();
    }

    Boolean Clicking = false;
    @Override
    public void onClick(View v) {
        if (Clicking.equals(true))
            return;
        int viewId = v.getId();
        switch (viewId) {
            case R.id.Btn_OutSubmit:

                break;
        }
    }


}
