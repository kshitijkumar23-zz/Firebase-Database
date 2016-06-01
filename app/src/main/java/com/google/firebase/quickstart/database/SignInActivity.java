package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.models.User;

import java.util.Random;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;
    private Button mGetStartedButton;
    private Button mLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mSignInButton = (Button) findViewById(R.id.button_sign_in);
        mSignUpButton = (Button) findViewById(R.id.button_sign_up);
        mGetStartedButton = (Button) findViewById(R.id.button_get_started);
        mLinkButton = (Button) findViewById(R.id.button_link);

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
        mGetStartedButton.setOnClickListener(this);
        mLinkButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (TextUtils.isEmpty(email))
        {
            return "Anonymous" + generateRandomInt();
        } else if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_sign_up:
                signUp();
                break;
            case R.id.button_get_started:
                getStarted();
                break;
            case R.id.button_link:
                linkWithCredential();
                break;
        }
    }

    private void getStarted()
    {
        if (mAuth.getCurrentUser() != null)
        {
            onAuthSuccess(mAuth.getCurrentUser());
        } else
        {
            signInAnonymously();
        }
    }

    private void signInAnonymously()
    {
        showProgressDialog();
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                hideProgressDialog();

                if (task.isSuccessful())
                {
                    onAuthSuccess(task.getResult().getUser());

                    Toast.makeText(SignInActivity.this, "Signed in Anonymously" , Toast.LENGTH_SHORT).show();

                    // Go to MainActivity
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                } else
                {
                    Toast.makeText(SignInActivity.this, "Anonymous Sign in failed  - " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void linkWithCredential()
    {
        Log.d(TAG, "linkWithCredential");
        if (!validateForm())
        {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        final FirebaseUser previousUser = mAuth.getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        hideProgressDialog();

                        if (task.isSuccessful())
                        {
                            Toast.makeText(SignInActivity.this, "Linked " + previousUser.getUid() + " to " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else
                        {
                            Toast.makeText(SignInActivity.this, "Linking failed - " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private int generateRandomInt()
    {
        Random random = new Random();
        int Low = 10;
        int High = 100000;
        return random.nextInt(High - Low) + Low;
    }
}
