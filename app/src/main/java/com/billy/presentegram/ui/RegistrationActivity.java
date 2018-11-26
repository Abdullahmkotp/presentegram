package com.billy.presentegram.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.billy.presentegram.R;
import com.billy.presentegram.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegistrationActivity extends AppCompatActivity {

    private final String TAG = RegistrationActivity.this.getClass().getSimpleName();

    @BindView(R.id.tv_dob)
    TextView mDateOfBirth;

    @BindView(R.id.et_name)
    TextInputLayout mUserName;

    @BindView(R.id.sp_gender)
    Spinner mGender;

    @BindView(R.id.et_email_reg)
    TextInputLayout mEmail;

    @BindView(R.id.et_password_reg)
    TextInputLayout mPassword;

    @BindView(R.id.btn_register)
    Button mRegister;

    @BindView(R.id.pb_register)
    ProgressBar pb_Register;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {

            mUserName.getEditText().setText(savedInstanceState.getString(this.getResources().getString(R.string.username)));

            mGender.setSelection(savedInstanceState.getInt(this.getResources().getString(R.string.gender)));

            mEmail.getEditText().setText(savedInstanceState.getString(this.getResources().getString(R.string.email)));

            mDateOfBirth.setText(savedInstanceState.getString(this.getResources().getString(R.string.dateOfBirth)));

            mUserName.getEditText().setText(savedInstanceState.getString(this.getResources().getString(R.string.username)));

        }


        mDateOfBirth = findViewById(R.id.tv_dob);
        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUserName.getEditText().getText() != null)
            outState.putString(this.getResources().getString(R.string.username), mUserName.getEditText().getText().toString());

        outState.putInt(this.getResources().getString(R.string.gender), mGender.getSelectedItemPosition());

        if (mEmail.getEditText().getText() != null)
            outState.putString(this.getResources().getString(R.string.email), mUserName.getEditText().getText().toString());

        if (mDateOfBirth.getText() != null)
            outState.putString(this.getResources().getString(R.string.dateOfBirth), mDateOfBirth.getText().toString());

        if (mPassword.getEditText().getText() != null)
            outState.putString(this.getResources().getString(R.string.username), mPassword.getEditText().getText().toString());
    }

    @OnClick(R.id.btn_register)
    void registerNewUser() {

        final String email = mEmail.getEditText().getText().toString().trim();
        String password = mPassword.getEditText().getText().toString().trim();
        final String username = mUserName.getEditText().getText().toString().trim();
        final String gender = mGender.getSelectedItem().toString();
        final String dateOfBirth = mDateOfBirth.getText().toString();


        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();


        final Intent intent = new Intent(this, HomeActivity.class);

        if (dataValidation()) {

            pb_Register.setVisibility(View.VISIBLE);
            mRegister.setVisibility(View.GONE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {


                                String userId = task.getResult().getUser().getUid();
                                User user = new User(userId, username, gender, dateOfBirth);
                                mReference.child("users").child(userId).setValue(user);
                                intent.putExtra("user", userId);
                                startActivity(intent);
                                finish();


                            } else {

                                Log.w(TAG, "createUserWithEmail:failed \n" + task.getException());

                                try {
                                    if (task.getException() != null)
                                        throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(getApplicationContext()
                                            , getResources()
                                                    .getString(R.string.password_error), Toast.LENGTH_SHORT).show();

                                    pb_Register.setVisibility(View.GONE);
                                    mRegister.setVisibility(View.VISIBLE);

                                } catch (FirebaseAuthInvalidCredentialsException | FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(getApplicationContext()
                                            , getResources()
                                                    .getString(R.string.credential_error), Toast.LENGTH_SHORT).show();
                                    mEmail.setError(getResources().getString(R.string.duplicated_email_error));
                                    pb_Register.setVisibility(View.GONE);
                                    mRegister.setVisibility(View.VISIBLE);

                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getApplicationContext()
                                            , getResources()
                                                    .getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                                    pb_Register.setVisibility(View.GONE);
                                    mRegister.setVisibility(View.VISIBLE);

                                } catch (Exception e) {

                                    Log.e(TAG, e.getMessage());

                                }


                            }
                        }

                    });
        } else {
            return;
        }
    }

    public boolean dataValidation() {

        String email = mEmail.getEditText().getText().toString().trim();
        mEmail.setError(null);
        String password = mPassword.getEditText().getText().toString().trim();
        mPassword.setError(null);
        String age = mDateOfBirth.getText().toString();
        String userName = mUserName.getEditText().getText().toString().trim();
        mUserName.setError(null);

        if (userName.length() > 15) {

            mUserName.setError(getResources().getString(R.string.username_error));
            return false;
        } else if (userName.length() < 3) {
            mUserName.setError(getResources().getString(R.string.username_error_short));
            return false;
        } else if (age.equals(getResources().getString(R.string.date_example))) {
            Toast.makeText(this, getResources().getString(R.string.birth_date_error), Toast.LENGTH_LONG).show();
            return false;
        } else if (email.isEmpty()) {
            mEmail.setError(getResources().getString(R.string.email_error));
            return false;
        } else if (password.isEmpty() | password.length() < 6) {
            mPassword.setError(getResources().getString(R.string.password_error));
            return false;
        }


        return true;
    }

    private void showDatePicker() {
        DatePickerFragment pickerFragment = new DatePickerFragment();
        pickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            TextView mDateOfBirth;
            mDateOfBirth = getActivity().findViewById(R.id.tv_dob);
            mDateOfBirth
                    .setText(TextUtils.concat(String.valueOf(day)
                            + "/" + String.valueOf(month + 1)
                            + "/" + String.valueOf(year)));

        }

    }
}
