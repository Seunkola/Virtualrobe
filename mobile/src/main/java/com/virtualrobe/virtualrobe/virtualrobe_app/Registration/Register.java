package com.virtualrobe.virtualrobe.virtualrobe_app.Registration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Register extends AppCompatActivity {

    @BindView(R.id.email)
    EditText inputEmail;

    @BindView(R.id.password)
    EditText inputPassword;

    @BindView(R.id.fullname)
    EditText inputFullname;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);
        ButterKnife.bind(this);

        /*Subscribe to welcome message service*/
        FirebaseMessaging.getInstance().subscribeToTopic("newusers");

        auth = FirebaseAuth.getInstance();

    }

   @OnClick(R.id.btn_reset_password)
    public void ResetPassword(){
       startActivity(new Intent(Register.this, ResetPasswordActivity.class));
   }

   @OnClick(R.id.sign_in_button)
    public void SignIn(){
       startActivity(new Intent(Register.this,MainActivity.class));
       finish();
   }

   @OnClick(R.id.sign_up_button)
    public void SignUp(View view){
       String email = inputEmail.getText().toString().trim();
       String password = inputPassword.getText().toString().trim();
       String fullname = inputFullname.getText().toString().trim();

       if (TextUtils.isEmpty(email)) {
           Snackbar.make(view,"Enter email address!",Snackbar.LENGTH_SHORT).show();
           return;
       }

       if (TextUtils.isEmpty(password)) {
          Snackbar.make(view, "Enter password!", Snackbar.LENGTH_SHORT).show();
           return;
       }

       if (password.length() < 6) {
           Snackbar.make(view, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
           return;
       }

       if (TextUtils.isEmpty(fullname)){
           Snackbar.make(view, "Enter your fullname!", Snackbar.LENGTH_SHORT).show();
       }

       progressBar.setVisibility(View.VISIBLE);
       /*create user*/
       auth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               // If sign in fails, display a message to the user. If sign in succeeds
               // the auth state listener will be notified and logic to handle the
               // signed in user can be handled in the listener.
               if (!task.isSuccessful()) {
                  Snackbar.make(view, "Authentication failed." + task.getException(),
                           Toast.LENGTH_SHORT).show();
               } else {
                   Snackbar.make(view, "Account created Successfully",Snackbar.LENGTH_SHORT).show();
                   progressBar.setVisibility(View.GONE);

                   /*Sign in user*/
                   //saving user login details on device
                   SharedPreferences sharedPreferences = getSharedPreferences("virtualrobe", MODE_PRIVATE);
                   SharedPreferences.Editor e = sharedPreferences.edit();
                   e.putString("username", "");
                   e.putInt("status", 1);
                   e.apply();

                   // Go to newsfeed
                   Intent intent = new Intent(Register.this, MainActivity.class);
                   intent.putExtra("Fullname",fullname);
                   startActivity(intent);
                   finish();
               }
           }
       });
   }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
