package com.example.whatsapplite;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PasswordSettingsActivity extends AppCompatActivity {

    EditText setPasswordEditText;
    EditText confirmPasswordEditText;
    EditText oldPasswordEditText;
    TextView setPasswordTextView;
    TextView setOrChangePassTextView;
    TextView confirmPasswordTextView;
    View lowerDivider;
    Button saveButton;
    Switch passwordSwitch;
    LinearLayout oldPasswordLinearLayout;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    String oldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_settings);

        Toolbar toolbar = findViewById(R.id.password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Password");

        preferences = PreferenceManager.getDefaultSharedPreferences(PasswordSettingsActivity.this);
        editor = preferences.edit();

        oldPassword = preferences.getString("pass", null);
        boolean isPasswordOn = preferences.getBoolean("isPasswordOn", false);

        passwordSwitch = findViewById(R.id.password_switch);
        if(isPasswordOn)
            passwordSwitch.setChecked(true);
        else
            passwordSwitch.setChecked(false);


        oldPasswordEditText = findViewById(R.id.old_password_edittext);
        setPasswordTextView = findViewById(R.id.set_password_textview);
        setOrChangePassTextView = findViewById(R.id.set_change_pass_tv);
        confirmPasswordTextView = findViewById(R.id.confirm_password_tv);
        lowerDivider = findViewById(R.id.lower_divider);

        oldPasswordLinearLayout = findViewById(R.id.old_password_linear_layout);

        if(oldPassword != null){
            setOrChangePassTextView.setText("Change PIN");
        }

        setPasswordEditText = findViewById(R.id.set_password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext);
        saveButton = findViewById(R.id.save_password_button);

        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    editor.putBoolean("isPasswordOn", true);
                } else {
                    editor.putBoolean("isPasswordOn", false);
                }
                editor.apply();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String oldPasswordInput = oldPasswordEditText.getText().toString();
                String savedOldPassword = preferences.getString("pass", null);
                String pass1 = setPasswordEditText.getText().toString();
                String pass2 = confirmPasswordEditText.getText().toString();

                if(savedOldPassword == null)
                    savedOldPassword = "null";

                if(pass1 == null || pass2 == null){
                    Toast.makeText(PasswordSettingsActivity.this, "fields cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(pass1.length() < 4 || pass2.length() < 4){
                    Toast.makeText(PasswordSettingsActivity.this, "PIN should be at least 4 digits or more", Toast.LENGTH_SHORT).show();
                }

                else if(oldPassword == null){
                    if(pass1.equals(pass2)){
                        editor.putString("pass", pass1);
                        editor.apply();
                        Toast.makeText(PasswordSettingsActivity.this, "PIN saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PasswordSettingsActivity.this, "PINs don't match", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    if(oldPasswordInput == null){
                        Toast.makeText(PasswordSettingsActivity.this, "Old PIN field cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if(!savedOldPassword.equals(oldPasswordInput))
                        Toast.makeText(PasswordSettingsActivity.this, "Incorrect old PIN", Toast.LENGTH_SHORT).show();
                    else if(!pass1.equals(pass2))
                        Toast.makeText(PasswordSettingsActivity.this, "PINs don't match", Toast.LENGTH_SHORT).show();
                    else{
                        editor.putString("pass", pass1);
                        editor.apply();
                        Toast.makeText(PasswordSettingsActivity.this, "PIN saved!", Toast.LENGTH_SHORT).show();

                        setPasswordTextView.setVisibility(View.INVISIBLE);
                        setPasswordEditText.setVisibility(View.INVISIBLE);
                        confirmPasswordEditText.setVisibility(View.INVISIBLE);
                        confirmPasswordTextView.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.INVISIBLE);
                        oldPasswordLinearLayout.setVisibility(View.INVISIBLE);
                        lowerDivider.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        setOrChangePassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPasswordTextView.setVisibility(View.VISIBLE);
                setPasswordEditText.setVisibility(View.VISIBLE);
                confirmPasswordEditText.setVisibility(View.VISIBLE);
                confirmPasswordTextView.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                lowerDivider.setVisibility(View.INVISIBLE);

                if(oldPassword != null)
                {
                    oldPasswordLinearLayout.setVisibility(View.VISIBLE);
                    setPasswordTextView.setText("New PIN");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){

            try{
                String pass = null;
                pass = setPasswordEditText.getText().toString();
                Log.d("setpassword", pass);
                System.out.println("setpas" + pass.length());
                if(pass.length() == 0 && oldPassword == null) {
                    editor.putBoolean("isPasswordOn", false);
                    editor.apply();

                    passwordSwitch.setChecked(false);
                }

            } finally {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
