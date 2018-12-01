package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.virtualrobe.virtualrobe.virtualrobe_app.Registration.Login;
import com.virtualrobe.virtualrobe.virtualrobe_app.Registration.Register;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Start_up_Page extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.log)
    public void Login(){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @OnClick(R.id.sign)
    public void Signup(){
        startActivity(new Intent(getApplicationContext(),Register.class));
        finish();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
}
