package com.ek;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ek.adapter.OnlyNoListAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.SelectSoLineModel;
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

public class TakeOutActivity extends AppCompatActivity implements View.OnClickListener{
    ListView listView;
    OnlyNoListAdapter adapter;
    EditText edit_only_no;
    OkHttpClient client;
    Button Btn_OutSubmit;

    private  static  String lastNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_out);
        edit_only_no = findViewById(R.id.edit_only_no);
        listView = findViewById(R.id.listView);
        client = new OkHttpClient();
        Btn_OutSubmit = findViewById(R.id.Btn_OutSubmit);
        Btn_OutSubmit.setOnClickListener(this);

        edit_only_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Log.d("监听setOnKeyListener", "keycode =" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK ) {
                    if(adapter.getCount() >= 1) {
                        dialog();
                    }
                    else{
                        TakeOutActivity.this.finish();
                    }
                    return true;
                }

                if (edit_only_no.getText().toString().isEmpty())
                    return true;
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Toast.makeText(getApplicationContext(),String.format("开始扫描[%s]", edit_only_no.getText().toString() ), Toast.LENGTH_LONG).show();
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

        getRemember();
    }

    EditText edit_qty_win,  edit_qty1_win;

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
                        Toast.makeText(getApplicationContext(),String.format("加载卷料出错"), Toast.LENGTH_LONG).show();
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
                            if(onlys[0].state.equals("1") == false ) {
                                Toast.makeText(getApplicationContext(),String.format("卷料[库存状态]非在库", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                                focusToEditText();
                            }
                            else{
                                if(adapter.hasOnly(onlys[0].only_no).equals(true)) {
                                    Toast.makeText(getApplicationContext(),String.format("卷数[%s]已扫描", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                                    edit_only_no.setText("");
                                    focusToEditText();
                                    return;
                                }

                                //如果是集体卷,要提示选择 卷数
                                if(onlys[0].is_multi.equals("T")) {
                                    selectOnDialog(onlys);
                                    return ;
                                }


                                Toast.makeText(getApplicationContext(),String.format("卷料[%s]扫描成功!", edit_only_no.getText().toString() ), Toast.LENGTH_LONG).show();
                                adapter.insert(onlys[0]);
                                edit_only_no.setText("");

                                remember();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("卷料[%s]不存在", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                        }

                        focusToEditText();
                    }
                });
            }
        });
    }


    public void remember(){
        List<OnlyNoItem> lines = adapter.getList();
        Gson gson = new Gson();
        String json =  gson.toJson(lines);
        SharedPreferences sp = getSharedPreferences("TakeOutLines", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString("TakeOutLines", json);

        spEditor.commit();
    }


    public  void getRemember(){
        SharedPreferences sp = getSharedPreferences("TakeOutLines", MODE_PRIVATE);
        Gson gson = new Gson();
        OnlyNoItem[] lastLines =  gson.fromJson(sp.getString("TakeOutLines", "[]"),OnlyNoItem[].class);

        adapter.removeAll();
        if(lastLines!= null) {
            for (int i = 0; (i < lastLines.length); i++) {
                adapter.add(lastLines[i]);
            }
            adapter.notifyDataSetChanged();
        }
    }



    public void selectOnDialog(final OnlyNoItem[] onlys){
        android.app.AlertDialog.Builder cDialog = new android.app.AlertDialog.Builder(TakeOutActivity.this);
        final View cView = LayoutInflater.from(TakeOutActivity.this).inflate(R.layout.activity_select_qty, null);
        edit_qty_win = cView.findViewById(R.id.edit_qty);
        edit_qty1_win = cView.findViewById(R.id.edit_qty1);
        edit_qty_win.setText(onlys[0].qty  +  "");
        edit_qty1_win.setText(onlys[0].qty1 +  "");

        cDialog.setTitle("输入集合卷[卷数]与[净重]");
        cDialog.setView(cView);
        cDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Integer qty = JK_SOActivity.getInt( edit_qty_win.getText().toString());
                Double qty1 = JK_SOActivity.getDouble( edit_qty1_win.getText().toString());
                if(qty <= 0 || qty1 <= 0){
                    Toast.makeText(getApplicationContext(),String.format("卷数不能少于0", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                    return;
                }
                onlys[0].qty = qty;
                onlys[0].qty1 = qty1;
                adapter.add(onlys[0]);
                edit_only_no.setText("");
                focusToEditText();
            }
        });
        cDialog.show();
    }

    public void showMenu(final OnlyNoItem item){

        String[] menuText = {"删除"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(menuText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    AlertDialog.Builder bulDelete = new AlertDialog.Builder(TakeOutActivity.this);
                    bulDelete.setTitle("询问");
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
        Clicking = true;
        List<OnlyNoItem> onlys = adapter.getList();
        OnlyNoItem[] list= new OnlyNoItem[onlys.size()];
        for (int i = 0; i < onlys.size(); i++) {
            OnlyNoItem item2 =  new OnlyNoItem();
            item2.isSelected = true;
            item2.only_no = onlys.get(i).only_no;
            item2.qty = onlys.get(i).qty;
            item2.qty1 = onlys.get(i).qty1;
            list[i] = item2;
        }

        ///ASHX/EK/CK/Ashx_JL.ashx?NowLoginId=67&NowUnderPassKey=7546221&way=F
        Gson gson = new Gson();
        String json = gson.toJson(list, OnlyNoItem[].class);//[].class
       // Log.d("only_nos", json);

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("PDA_CKOUTJL", "TRUE");
        paramsMap.put("action", "SaveTable");
        paramsMap.put("only_nos", json);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            Log.d("key|" + key, paramsMap.get(key));
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
                        Clicking = false;
                        focusToEditText();
                        Toast.makeText(getApplicationContext(),String.format("提交“出库单”出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Clicking = false;
                final String respTxt =  response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson2 = new Gson();
                        final NormalResult result = gson2.fromJson(respTxt, NormalResult.class);
                        if (result != null)
                        {
                            if(result.msg.startsWith("成功")){
                            //if( result.success.equals(true)) {
                                focusToEditText();
                                Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_LONG).show();
                                adapter.removeAll();
                                remember();
                            }
                            else{
                                focusToEditText();
                                Toast.makeText(getApplicationContext(), "提交“出库单”失败:" + result.msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

            }
        });
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
                if(adapter.getCount() > 0){
                    submit();
                }
                break;
        }
    }

    protected void dialog() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TakeOutActivity.this);
        builder.setMessage("确定要退出吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TakeOutActivity.this.finish();
                    }
                });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}
