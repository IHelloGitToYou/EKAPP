package com.ek;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ek.adapter.OnlyNoListAdapter;
import com.ek.model.DbInfo;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.google.gson.Gson;

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

public class TakeOutActivity extends AppCompatActivity {

    ListView listView;
    OnlyNoListAdapter adapter;
    EditText edit_only_no;
    OkHttpClient client;

    private  static  String lastNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_out);
        edit_only_no = findViewById(R.id.edit_only_no);
        listView = findViewById(R.id.listView);
        client = new OkHttpClient();

        edit_only_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d("test", "keycode =" + keyCode);
                if(edit_only_no.getText().toString().isEmpty())
                    return true;
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    //Log.d("真的入", "keycode =" + keyCode);
                    tryToAddJL();
                }
                return false;
            }
        });
        listView.setAdapter(adapter = new OnlyNoListAdapter(new ArrayList<OnlyNoItem>()));
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showMenu((OnlyNoItem) adapter.getItem(position));
                return false;
            }
        });
    }

    void tryToAddJL()
    {
        lastNo = edit_only_no.getText().toString();

        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetJL");
        paramsMap.put("only_no",edit_only_no.getText().toString());
        paramsMap.put("wh_no", "" );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_PRDTONLY))
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
                final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onlys != null && onlys.length>0)
                        {
                            if(adapter.hasOnly(onlys[0].only_no) .equals(false)) {
                                adapter.add(onlys[0]);
                                edit_only_no.setText("");
                            }

                            if(onlys[0].state.equals("1") == false ) {
                                Toast.makeText(getApplicationContext(),String.format("卷料[库存状态]非在库", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"卷料已扫描!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("卷料[%s]不存在", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    public void showMenu(final OnlyNoItem item){

        String[] menuText = {"删除"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(menuText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    AlertDialog.Builder bulDelete = new AlertDialog.Builder(TakeOutActivity.this);
                    bulDelete.setTitle("删除");
                    bulDelete.setMessage(String.format(String.format("确定要删除卷[%s]吗？", item.only_no)));

                    bulDelete.setPositiveButton("删吧", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.remove(item);
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

    public void submit(){
        List<OnlyNoItem> onlys = adapter.getList();
        ///ASHX/EK/CK/Ashx_JL.ashx?NowLoginId=67&NowUnderPassKey=7546221&way=F
        Gson gson = new Gson();
        String json = gson.toJson(onlys, OnlyNoItem[].class);

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("PDA_CKOUTJL", "TRUE");
        paramsMap.put("action", "PDA_CKOUTJL");
        paramsMap.put("only_nos", json);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }

        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_JL))
                .post(builder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("提交“出库单”出错"), Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson2 = new Gson();
                final NormalResult result = gson2.fromJson(respTxt, NormalResult.class);
                if (result != null)
                {
                    if( result.success.equals(true)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "成功提交“出库单”"+ result.msg, Toast.LENGTH_LONG).show();
                                adapter.removeAll();
                                //Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                //startActivity(loginIntent);
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "提交“出库单”失败:" + result.msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }


}
