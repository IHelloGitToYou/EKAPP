package com.ek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ek.model.DbInfo;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    public  final  static String SENT_SAL_NO_MESSAGE = "SENT_SAL_NO_MESSAGE";
    public  final  static String SENT_DB_NO_MESSAGE = "SENT_DB_NO_MESSAGE";
    public  final  static String SENT_PSW_MESSAGE = "SENT_PSW_MESSAGE";
    OkHttpClient client;
    EditText edit_user, edit_ps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btn_login = findViewById(R.id.btn_login);
        edit_user = findViewById(R.id.edit_user);
        edit_ps = findViewById(R.id.edit_ps);

        btn_login.setOnClickListener(this);
        client = new OkHttpClient();

        SharedPreferences sp = getSharedPreferences("LoginActivity", MODE_PRIVATE);
        SharedPreferences.Editor spEditor =sp.edit();

        edit_user.setText( sp.getString("LoginUSER", "0000").toString());
        edit_ps.setText( sp.getString("LoginPS", "2").toString());
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.btn_login:
                login();
                break;
        }
    }

    void setSettingValues(){
        SharedPreferences sp2 = getSharedPreferences("EKSetting", MODE_PRIVATE);
        String str1 = sp2.getString("WebHostUrl", WebApi.HOST);
        String str2 = sp2.getString("PrintIp", WebPrintorApi.HOST_IP);

        WebApi.setHostUrl(str1);
        WebPrintorApi.SetPrintHost_IP(str2);
    }

    public void clearSettingValues(){
        SharedPreferences sp = getSharedPreferences("EKSetting", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();

        String str1 = "";
        String str2 = "";;
        ed.putString("WebHostUrl", str1);
        ed.putString("PrintIp", str2);

        ed.commit();
    }

    void login()
    {
        setSettingValues();

        final String sal_no = edit_user.getText().toString();
        final String psw = edit_ps.getText().toString();
        // 清配置
        if(sal_no.equals("999999999")){
            //clearSettingValues();
            startActivity(new Intent(this, SettingActivity.class));
            return;
        }

        OkHttpClient client = new OkHttpClient();
        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("action","LoadDBInfo");
        paramsMap.put("before_login","true");
        paramsMap.put("sal_no", sal_no);
        paramsMap.put("psw", edit_ps.getText().toString() );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_LOGIN))
                .post(builder.build())
                .build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String respTxt =  response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            final DbInfo[] dbInfos = gson.fromJson(respTxt, DbInfo[].class);
                            if (dbInfos == null || dbInfos.length <= 0) {
                                Toast.makeText(getApplicationContext(), "用户或者密码错误", Toast.LENGTH_LONG).show();
                                return;
                            }

                            SharedPreferences sp = getSharedPreferences("LoginActivity", MODE_PRIVATE);
                            SharedPreferences.Editor spEditor = sp.edit();

                            spEditor.putString("LoginUSER", sal_no);
                            spEditor.putString("LoginPS", edit_ps.getText().toString());
                            spEditor.commit();

                            //Toast.makeText(getApplicationContext(), dbInfos[0].db_name, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(SENT_SAL_NO_MESSAGE, sal_no);
                            intent.putExtra(SENT_DB_NO_MESSAGE, dbInfos[0].db_no);
                            intent.putExtra(SENT_PSW_MESSAGE, psw);

                            startActivity(intent);



                        }

                    });
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }


    }




    public static String CommonUrlEncode(String str){
        String strEncode = "";
        try {
            strEncode = URLEncoder.encode(str, "UTF-8");
           // Log.d("URL", );
        } catch (UnsupportedEncodingException e) {
            Log.d("URL_错了",  str);
            strEncode = str;
        }

        return  strEncode;
    }

}
