package com.billy.presentegram;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends AppCompatActivity {



    @BindView(R.id.tv_create_account)
    TextView mCreateAccount;

    @BindView(R.id.et_email)
    TextInputLayout mEmail;

    @BindView(R.id.et_password)
    TextInputLayout mPassword;

    @BindView(R.id.pb_signIn)
    ProgressBar signInProgressbar;

    @BindView(R.id.btn_signIn)
    Button signInBtn;
    FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();


        if (savedInstanceState != null){
           mEmail.getEditText().setText(savedInstanceState.getString(this.getResources().getString(R.string.email)));
           mPassword.getEditText().setText(savedInstanceState.getString(this.getResources().getString(R.string.password)));
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mEmail.getEditText().getText() != null)
            outState.putString(this.getResources().getString(R.string.email), mEmail.getEditText().getText().toString());

        if (mPassword.getEditText().getText() != null)
            outState.putString(this.getResources().getString(R.string.username), mPassword.getEditText().getText().toString());
    }

    @OnClick(R.id.tv_create_account)
    public void createAccount() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);


    }

    @OnClick(R.id.btn_signIn)
    public void signIn(){

        String email = mEmail.getEditText().getText().toString().trim();
        String password =  mPassword.getEditText().getText().toString().trim();
        final Intent intent = new Intent(this,HomeActivity.class);

        if (checkCredentials()){
            signInBtn.setVisibility(View.GONE);
            signInProgressbar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,
                    new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String userId = task.getResult().getUser().getUid();
                        intent.putExtra("userId",userId);
                        startActivity(intent);
                        finish();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {



                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.credential_error)
                            ,Toast.LENGTH_SHORT).show();

                    signInBtn.setVisibility(View.VISIBLE);
                    signInProgressbar.setVisibility(View.GONE);


                }
            });
        }


    }

    private boolean checkCredentials(){

        String email = mEmail.getEditText().getText().toString().trim();
        String password =  mPassword.getEditText().getText().toString().trim();

        if ( email.isEmpty() | password.isEmpty()){

            Toast.makeText(this,getResources().getString(R.string.credential_empty),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
