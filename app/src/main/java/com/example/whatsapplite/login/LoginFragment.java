import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.whatsapplite.login.OnLoginListener;
import com.the_spartan.myapplication.R;
import com.the_spartan.run.activities.HomeActivity;
import com.the_spartan.run.helper.SQLiteHandler;
import com.the_spartan.run.helper.SessionManager;
import com.the_spartan.run.volley.AppController;
import com.the_spartan.run.volley.Config_URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

♦package com.example.whatsapplite.login;

public class LoginFragment extends Fragment implements OnLoginListener {
    private static final String TAG = "LoginFragment";

    private EditText etPassword;
    private EditText etEmail;

    private ProgressDialog pDialog;
    private SessionManager session;

    private SQLiteHandler db;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        session = new SessionManager(getContext());

        if (session.isLoggedIn()){
            Intent homeIntent = new Intent(getContext(), HomeActivity.class);
            startActivity(homeIntent);
        }

        View inflate = inflater.inflate(R.layout.fragment_login, container, false);
        inflate.findViewById(R.id.forgot_password).setOnClickListener(v -> Toast.makeText(getContext(), "Forgot password clicked", Toast.LENGTH_SHORT).show());

        etEmail = inflate.findViewById(R.id.et_email);
        etPassword = inflate.findViewById(R.id.et_password);

        return inflate;
    }

    @Override
    public void login() {
        pDialog = new ProgressDialog(getContext());
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getContext());

        // Check if user is already logged in or not
//        if (session.isLoggedIn()) {
//            // User is already logged in. Take him to main activity
//            Intent intent = new Intent(getContext(), HomeActivity.class);
//            startActivity(intent);
//        }

        // Login button Click Event


        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        // Check for empty data in the form
        if (email.trim().length() > 0 && password.trim().length() > 0) {
            // login user
            checkLogin(email, password);
        } else {
            // Prompt user to enter credentials
            Toast.makeText(getContext(), "Please enter the credentials!", Toast.LENGTH_SHORT).show();
        }


        // Link to Register Screen


    }

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        db = new SQLiteHandler(getContext());

        StringRequest strReq = new StringRequest(Method.POST,
                Config_URL.URL_LOGIN,
                new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
//                        String id = user.getString("id");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");


                        //delete previous logins
                        db.deleteUsers();

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at);
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Launch main activity
                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getContext(),"JSON ERROR" + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getContext(),"Login error" + error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing()) pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing()) pDialog.dismiss();
    }
}

