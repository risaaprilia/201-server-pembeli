package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kota201.jtk.pkl.service.NetworkChangeReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.inputNoPonsel) EditText inputNoPonsel;
    @BindView(R.id.inputPassword) EditText inputPassword;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.linkSignup) View linkSignup;
    @BindString(R.string.my_prefs) String my_prefs;

    Boolean statusValid;
    ProgressDialog progressDialog;
    int role;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        ButterKnife.bind(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        linkSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        if (mAuthTask != null) {
            return;
        }

        btnLogin.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autentikasi...");
        progressDialog.show();

        String mNoPonsel = inputNoPonsel.getText().toString();
        String mPassword = inputPassword.getText().toString();

        mAuthTask = new UserLoginTask(mNoPonsel, md5(mPassword));
        mAuthTask.execute((Void) null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        String noPonsel = inputNoPonsel.getText().toString();
        if(role == 0){
            //jika pedagang
            SharedPreferences.Editor editor = getSharedPreferences(my_prefs, MODE_PRIVATE).edit();
            editor.putInt("Role",0);
            editor.putString("noPonselPedagang",noPonsel);
            editor.apply();
        }else{
            //jika pembeli
            SharedPreferences.Editor editor = getSharedPreferences(my_prefs, MODE_PRIVATE).edit();
            editor.putInt("Role",1);
            editor.putString("noPonselPembeli",noPonsel);
            editor.apply();
        }
        btnLogin.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login gagal", Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String noPonsel = inputNoPonsel.getText().toString();
        String password = inputPassword.getText().toString();

        if (noPonsel.isEmpty() || !Patterns.PHONE.matcher(noPonsel).matches()) {
            inputNoPonsel.setError("Nomor ponsel tidak sesuai");
            valid = false;
        } else {
            inputNoPonsel.setError(null);
        }

        if (password.isEmpty()) {
            inputPassword.setError("Password tidak sesuai");
            valid = false;
        } else if (password.length() < 4) {
            inputPassword.setError("Password tidak kurang dari 6 karakter");
            valid = false;
        }else if(password.length() > 18) {
            inputPassword.setError("Password tidak lebih dari 18 karakter");
            valid = false;
        }else{
            inputPassword.setError(null);
        }

        return valid;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mNoPonsel;
        private final String mPassword;

        UserLoginTask(String noPonsel, String password) {
            mNoPonsel = noPonsel;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            if(!NetworkChangeReceiver.isNetworkAvailable(getBaseContext()))
                return false;

            JSONObject dataToSend = null;
            try {
                dataToSend = new JSONObject()
                        .put("noPonsel", mNoPonsel)
                        .put("password", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Autentikasi_controller/login")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            JSONObject Jobject = null;
            try {
                Response responses = client.newCall(request).execute();
                if (!responses.isSuccessful())
                    throw new IOException("Unexpected code" + responses.toString());

                String jsonData = responses.body().string();
                Jobject = new JSONObject(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Getting JSON Array node
            assert Jobject != null;
            JSONObject contacts = null;
            try {
                contacts = Jobject.getJSONObject("login");
                statusValid = contacts.getBoolean("statusValid");
                Log.i("asik",statusValid.toString());
                role = contacts.getInt("role");
                Log.i("asik", String.valueOf(role));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressDialog.dismiss();

            if (success && statusValid) {
                onLoginSuccess();
            } else {
                onLoginFailed();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressDialog.dismiss();
        }
    }

    //Network
    public boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null && ni.isConnected())
            return true;
        else
            return false;
    }



}
