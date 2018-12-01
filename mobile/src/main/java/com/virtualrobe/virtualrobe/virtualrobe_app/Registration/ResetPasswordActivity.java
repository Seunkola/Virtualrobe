package com.virtualrobe.virtualrobe.virtualrobe_app.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetPasswordActivity extends AppCompatActivity {
    @BindView(R.id.email)
    EditText inputEmail;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        ButterKnife.bind(this);

        auth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_back)
    public void Back(){
        finish();
    }

    @OnClick(R.id.btn_reset_password)
    public void Resetpassword(View view){
        String email = inputEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(view, "Enter your registered email id", Snackbar.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(view, "We have sent you instructions to reset your password!",Snackbar.LENGTH_SHORT).show();
                            startActivity(new Intent(ResetPasswordActivity.this,MainActivity.class));
                            finish();
                        } else {
                            Snackbar.make(view, "Failed to send reset email!", Snackbar.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
