package com.ek;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ek.adapter.HistoryJLAdapter;
import com.ek.adapter.OnlyNoListAdapter;
import com.ek.model.HistoryJLModel;
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

public class z_path_Activity extends AppCompatActivity implements View.OnClickListener{
    ListView listView;
    HistoryJLAdapter adapter;
    EditText edit_only_no;
    EditText edit_Z_path;
    OkHttpClient client;
    Button Btn_OutSubmit;

    private  static  String lastNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_z_path);
        edit_only_no = findViewById(R.id.edit_only_no);
        edit_Z_path = findViewById(R.id.edit_Z_path);
        listView = findViewById(R.id.listView);
        client = new OkHttpClient();


        edit_only_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d("监听setOnKeyListener", "keycode =" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK ) {
                    z_path_Activity.this.finish();
//                    if(adapter.getCount() >= 1) {
//                        //dialog();
//                    }
//                    else{
//                        z_path_Activity.this.finish();
//                    }
                    return false;
                }

                if (edit_only_no.getText().toString().isEmpty())
                    return true;
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    submitZPath();
                }
                return false;
            }
        });

        listView.setAdapter(adapter = new HistoryJLAdapter(new ArrayList<HistoryJLModel>()));
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
////                showMenu((OnlyNoItem)
////                        adapter.getItem(position));
//                return false;
//            }
//        });

        getRemember();
        focusToEditText();
    }

    void submitZPath()
    {
        String only_no = edit_only_no.getText().toString();
        String z_path = edit_Z_path.getText().toString();
        if(z_path.isEmpty()){
            Toast.makeText(getApplicationContext(),String.format("[位置]请先输入"), Toast.LENGTH_LONG).show();
//            edit_Z_path.setSelectAllOnFocus(true);
//            edit_Z_path.requestFocus();
            return;
        }

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","SetJLZ_path");
        paramsMap.put("only_no",only_no);
        paramsMap.put("Z_path", z_path );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
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
                        Toast.makeText(getApplicationContext(),String.format("更新[上架位置]出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final NormalResult result = gson.fromJson(respTxt, NormalResult.class);
                if (result != null)
                {
                    if( result.result.equals(true)) {
                        remember();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),String.format("更新[位置][%s]成功!", edit_only_no.getText().toString() ), Toast.LENGTH_LONG).show();
                                HistoryJLModel newItem = new HistoryJLModel();
                                newItem.isNew =true;
                                newItem.showMsg = String.format("更新卷[%1s]位置[%2s]->[%3s]",
                                        edit_only_no.getText().toString(),
                                        result.old_Z_path,
                                        edit_Z_path.getText().toString());
                                adapter.insert(newItem);
                                edit_only_no.setText("");
                                focusToEditText();
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "更新上架位置出错:" + result.msg, Toast.LENGTH_SHORT).show();
                                focusToEditText();
                            }
                        });
                    }
                }
            }
        });
    }


    public void remember(){

        SharedPreferences sp = getSharedPreferences("Z_pathActivity", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString("Z_path", edit_Z_path.getText().toString());

        spEditor.commit();
    }


    public  void getRemember(){
        SharedPreferences sp = getSharedPreferences("Z_pathActivity", MODE_PRIVATE);
        String str = sp.getString("Z_path", "");
        edit_Z_path.setText(str);
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
