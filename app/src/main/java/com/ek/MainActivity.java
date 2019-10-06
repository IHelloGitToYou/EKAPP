package com.ek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ek.adapter.MainAdapter;
import com.ek.model.DbInfo;
import com.ek.model.NormalResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    MainAdapter adapter;
    OkHttpClient client;
    public static String current_sal_no;
    public static String current_db_no;
    public static String current_psw;
    public static String current_login_id;
    public static String current_NowUnderPassKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView gridView = findViewById(R.id.gridView);
        adapter = new MainAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        client = new OkHttpClient();

        Intent intent = getIntent();
        current_sal_no = intent.getStringExtra(LoginActivity.SENT_SAL_NO_MESSAGE);
        current_db_no = intent.getStringExtra(LoginActivity.SENT_DB_NO_MESSAGE);
        current_psw = intent.getStringExtra(LoginActivity.SENT_PSW_MESSAGE);
        GetLoginId();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String menu =  ((TextView)view.findViewById(R.id.txt_menu)).getText().toString();
        String menuText = (String) adapter.getItem(position);
        Log.d("test", "menu="+ menu + ", menuText=" + menuText);
        if (menuText.equals("销售发货")) {
            startActivity(new Intent(this, sa_Activity.class));
        }
        else if (menuText.equals("托盘管理")) {
            startActivity(new Intent(this, sa_plate_Activity.class));
        }
        else if (menuText.equals("卷料上架")) {
            startActivity(new Intent(this, z_path_Activity.class));
        }
        else if (menuText.equals("卷料出库")){
            startActivity(new Intent(this, TakeOutActivity.class));
        }
        else if (menuText.equals("生产机台")){
            startActivity(new Intent(this, JK_SOActivity.class));
        }
        else if (menuText.equals("配置")){
            startActivity(new Intent(this, SettingActivity.class));
        }
    }


    public void GetLoginId(){

        HashMap<String,String> paramsMap= new HashMap<>();
        paramsMap.put("action","CheckPsw");
        paramsMap.put("sal_no",current_sal_no);
        paramsMap.put("psw", current_psw);
        paramsMap.put("db_no", current_db_no );
        paramsMap.put("logining", "true");

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_SALM))
                .post(builder.build())
                .build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "取登录牌失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String respTxt =  response.body().string();
                    Gson gson = new Gson();
                    final DbInfo dbInfo = gson.fromJson(respTxt, DbInfo.class);
                    if (dbInfo != null )
                    {
                        current_login_id = dbInfo.id;

                        UpLoadNowUnderPassKey();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void UpLoadNowUnderPassKey(){
        current_NowUnderPassKey = GetRandString();
        HashMap<String,String> paramsMap= new HashMap<>();
        paramsMap.put("action","GetVOnlineIdInfo");
        paramsMap.put("NowLoginId",current_login_id);
        paramsMap.put("NowUnderPassKey", current_NowUnderPassKey );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }

        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_VONLINE))
                .post(builder.build())
                .build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginIntent);
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
                         if( result.success.equals(true)) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(getApplicationContext(), "取登录牌成功", Toast.LENGTH_LONG).show();
                                 }
                             });
                         }
                         else{
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(getApplicationContext(), "取登录牌失败", Toast.LENGTH_SHORT).show();
                                 }
                             });
                         }
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String GetRandString(){
        return String.valueOf( Math.ceil(Math.random()*882828));
    }

}
