package com.ek;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ek.model.OnlyNoItem;
import com.ek.model.PrintorModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class edit_jl_activity extends AppCompatActivity implements View.OnClickListener{
    EditText FD_width, FD_length,edit_Z_core_kg, edit_Z_kg;
    Spinner edit_print_jk,edit_printor_jk,edit_print_back;

    OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_jl);

        client = new OkHttpClient();

        FD_width = findViewById(R.id.edit_FD_width);
        FD_length = findViewById(R.id.edit_FD_length);
        edit_Z_core_kg = findViewById(R.id.edit_Z_core_kg);
        edit_Z_kg = findViewById(R.id.edit_Z_kg);
        edit_print_jk = findViewById(R.id.edit_print_jk);

        edit_printor_jk = findViewById(R.id.edit_printor_jk);

//        findViewById(R.id.Do_JK).setOnClickListener(this);
//        findViewById(R.id.Do_Print).setOnClickListener(this);
    }

    Boolean Clicking = false;
    @Override
    public void onClick(View v) {
//        if (Clicking.equals(true))
//            return;
//        int viewId = v.getId();
//        switch (viewId) {
//            case R.id.Do_JK:
//
//                break;
//            case R.id.Do_Print:
//
//                break;
//        }
    }

    public void setValues(OnlyNoItem currentOnly ){



    }

}
