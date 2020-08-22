package com.example.whatsapplite.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.the_spartan.myapplication.R;
import com.the_spartan.run.MainActivity;
import com.the_spartan.run.helper.SQLiteHandler;
import com.the_spartan.run.helper.SessionManager;
import com.the_spartan.run.volley.AppController;
import com.the_spartan.run.volley.Config_URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment implements OnSignUpListener {
    private static final String TAG = "SignUpFragment";

    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputConfirmPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;


    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_signup, container, false);

        inputEmail = inflate.findViewById(R.id.et_email_reg);
        inputPassword = inflate.findViewById(R.id.et_password_reg);
        inputConfirmPassword = inflate.findViewById(R.id.et_confirm_pass_reg);


        return inflate;
    }

    @Override
    public void signUp() {

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        // Progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getContext());

        // SQLite database handler
        db = new SQLiteHandler(getContext());

        // Check if user is already logged in or not
        if (!email.isEmpty() && !password.isEmpty()) {
            if (!password.equals(confirmPassword)){
                Toast.makeText(getContext(), "Passwords doesn't match!", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(email, password);
            }
        } else {
            Toast.makeText(getContext(), "Please enter your details!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        String name = "saptarshi";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config_URL.URL_REGISTER,
                new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
                        alertDialog.setTitle("Registration Successful!")
                                .setMessage("Please Login to continue")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((MainActivity)getActivity()).switchFragment(new LoginButton(getContext()));
                                    }
                                });
                        alertDialog.show();
                        alertDialog.setCancelable(false);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Log.d("JSON", "ERROR"+errorMsg);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "register");
                params.put("name", name);
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

