package com.project.markpollution;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    public static GoogleApiClient googleApiClient;
    private SignInButton signInButton;
    private String url_insertUser = "http://indi.com.vn/dev/markpollution/InsertUser.php";
    private String url_checkUserByEmail = "http://indi.com.vn/dev/markpollution/RetrieveUserByEmail.php?email=";
    private String url_retrieve_pollutionPoint = "http://indi.com.vn/dev/markpollution/RetrievePollutionPoint.php";
    private Intent intent;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_signin);
        changeStatusBarColor();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .enableAutoManage(this, this)
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        signInButton.setVisibility(View.INVISIBLE);

        getAllPollutionPoint();
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                final GoogleSignInAccount acc = result.getSignInAccount();

                String urlCheckUserExistOrNot = url_checkUserByEmail + acc.getEmail();
                StringRequest stringReq = new StringRequest(Request.Method.GET, urlCheckUserExistOrNot, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("user doesn't exist")){     // When user exists
                            // Save id_user to SharedPreferences
                            saveUserIDtoSharedPreferences(null, response, true);
                            intent.putExtra("name", acc.getDisplayName());
                            intent.putExtra("email", acc.getEmail());
                            intent.putExtra("avatar", acc.getPhotoUrl().toString());
                            startActivity(intent); finish();
                        }else{
                            insertUser(acc.getDisplayName(), acc.getEmail(), acc.getPhotoUrl().toString());
                            intent.putExtra("name", acc.getDisplayName());
                            intent.putExtra("email", acc.getEmail());
                            intent.putExtra("avatar", acc.getPhotoUrl().toString());
                            startActivity(intent); finish();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SigninActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Volley", error.getMessage());
                    }
                });

                Volley.newRequestQueue(this).add(stringReq);
            }else {
                Toast.makeText(this, result.getStatus().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserIDtoSharedPreferences(String email, final String id_user, boolean isUserExist){
        if(isUserExist){
            SharedPreferences sharedPreferences = getSharedPreferences("sharedpref_id_user",MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("sharedpref_id_user", id_user);
            edit.commit();
        }else{
            String urlCheck = url_checkUserByEmail + email;
            StringRequest stringReq = new StringRequest(Request.Method.GET, urlCheck, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    SharedPreferences sharedPreferences = getSharedPreferences("sharedpref_id_user",MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("sharedpref_id_user", response);
                    edit.commit();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SigninActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            Volley.newRequestQueue(this).add(stringReq);
        }
    }

    private void insertUser(final String name, final String email, final String avatar){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_insertUser, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("insert success")){
                    Toast.makeText(SigninActivity.this, "Account has registered successful", Toast.LENGTH_SHORT).show();
                    saveUserIDtoSharedPreferences(email,null, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SigninActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("name_user", name);
                params.put("email", email);
                params.put("avatar", avatar);
                params.put("is_admin", "0");

                return params;
            }
        };

        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(stringRequest);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getAllPollutionPoint(){
        showProgressDialog("Loading data...");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_retrieve_pollutionPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                intent = new Intent(SigninActivity.this, MainActivity.class);
                intent.putExtra("po_data", response);
                signInButton.setVisibility(View.VISIBLE);
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Toast.makeText(SigninActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.getMessage());
            }
        });
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void showProgressDialog(String msg){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog(){
        progressDialog.hide();
    }
}
