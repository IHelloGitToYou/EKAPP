package com.ek;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    EditText  edit_WebHostUrl,edit_PrintIp;
    Button Btn_EditSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        edit_WebHostUrl = findViewById(R.id.edit_WebHostUrl);
        edit_PrintIp = findViewById(R.id.edit_PrintIp);
        Btn_EditSetting = findViewById(R.id.Btn_EditSetting);


        getValues();

        Btn_EditSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValues();
            }
        });
    }

    public void getValues(){
        SharedPreferences sp = getSharedPreferences("EKSetting", MODE_PRIVATE);
        String str1 = sp.getString("WebHostUrl", WebApi.HOST);
        String str2 = sp.getString("PrintIp", WebPrintorApi.HOST_IP);
        edit_WebHostUrl.setText(str1);
        edit_PrintIp.setText(str2);
    }

    public void setValues(){
        SharedPreferences sp = getSharedPreferences("EKSetting", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();

        String str1 = edit_WebHostUrl.getText().toString();
        String str2 = edit_PrintIp.getText().toString();;
        ed.putString("WebHostUrl", str1);
        ed.putString("PrintIp", str2);

        ed.commit();
    }


}
