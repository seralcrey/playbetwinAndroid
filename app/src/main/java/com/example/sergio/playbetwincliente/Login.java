package com.example.sergio.playbetwincliente;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Login extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void abrir(View v){
        Intent i = new Intent(this, Principal.class);
        startActivity(i);
    }
}
