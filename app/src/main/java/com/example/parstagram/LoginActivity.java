package com.example.parstagram;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnSignIn;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Sign In Button pressed...");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Sign Up Button Pressed...");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                registerUser(username, password);
            }
        });

    }

    private void registerUser(String username, String password) {
        Log.i(TAG, "Attempting to register user...");
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    goMainActivity();
                    Toast.makeText(LoginActivity.this, "Account creation success!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (e != null) {
                        switch (e.getCode()) {
                            case ParseException.USERNAME_TAKEN:
                                Toast.makeText(LoginActivity.this, "Username taken! Please login to your account or sign up with a different username.",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case ParseException.USERNAME_MISSING:
                                Toast.makeText(LoginActivity.this, "Username missing! Please enter a username.",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Unknown error has occured!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user...");
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    goMainActivity();
                    Toast.makeText(LoginActivity.this, "Success!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (e != null) {
                        switch(e.getCode()) {
                            case ParseException.VALIDATION_ERROR:
                                Toast.makeText(LoginActivity.this, "Invalid username or password!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Unknown error has occurred!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }

            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
