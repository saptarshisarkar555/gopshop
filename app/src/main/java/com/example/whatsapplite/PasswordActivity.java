package com.example.whatsapplite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    EditText passwordEditText;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PasswordActivity.this);
        final String password = preferences.getString("pass", null);


        boolean isPasswordOn = preferences.getBoolean("isPasswordOn", false);

        if (password == null || !isPasswordOn) {
            startActivity(new Intent(PasswordActivity.this, MainActivity.class));
            finish();
        }

        passwordEditText = findViewById(R.id.password_edit_text);
        submitButton = findViewById(R.id.password_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordInput = passwordEditText.getText().toString();
                if (passwordInput == null) {
                    Toast.makeText(PasswordActivity.this, "Plese type your password", Toast.LENGTH_SHORT).show();
                } else {
                    if (passwordInput.equals(password)) {
                        Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PasswordActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}