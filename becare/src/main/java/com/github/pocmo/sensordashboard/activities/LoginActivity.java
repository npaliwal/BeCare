package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.QueryPostData;
import com.github.pocmo.sensordashboard.model.UserQueryResponse;
import com.github.pocmo.sensordashboard.network.BecareHiveApi;
import com.github.pocmo.sensordashboard.network.HiveHelper;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public class LoginActivity extends Activity {
    public static int REQUEST_LOGIN_CODE = 1;
    public static int LOGIN_RESULT_FAIL = 0;
    public static int LOGIN_RESULT_SUCCESS = 1;

    ViewGroup registerContnr, loginContnr;
    EditText userName, userPassword;
    TextView register, forgotPassword;
    Button submitBtn;

    TextView alreadyResgistered;
    Spinner registerAs;

    @Inject
    PreferenceStorage preferenceStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        preferenceStorage = new PreferenceStorage(LoginActivity.this);

        registerContnr = (ViewGroup)findViewById(R.id.register_contnr);
        loginContnr = (ViewGroup)findViewById(R.id.login_cntnr);

        userName        = (EditText)loginContnr.findViewById(R.id.et_username);
        userPassword    = (EditText)loginContnr.findViewById(R.id.et_userpswd);
        register        = (TextView)loginContnr.findViewById(R.id.tv_register);
        forgotPassword  = (TextView)loginContnr.findViewById(R.id.tv_forgot_pswd);
        submitBtn       = (Button)loginContnr.findViewById(R.id.btn_submit);

        alreadyResgistered = (TextView)registerContnr.findViewById(R.id.tv_already_registered);
        registerAs = (Spinner)registerContnr.findViewById(R.id.sp_register_as);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Register As");
        categories.add("Doctor");
        categories.add("Patient");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        registerAs.setAdapter(dataAdapter);

        alreadyResgistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(false);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(true);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void switchView(boolean showRegister){
        if(showRegister) {
            loginContnr.setVisibility(View.GONE);
            registerContnr.setVisibility(View.VISIBLE);
        }else{
            loginContnr.setVisibility(View.VISIBLE);
            registerContnr.setVisibility(View.GONE);
        }
    }

    public void attemptLogin(){
        String userNameInput = userName.getText().toString();
        String userPswdInput = userPassword.getText().toString();
        if(TextUtils.isEmpty(userNameInput) || TextUtils.isEmpty(userPswdInput)){
            Toast.makeText(LoginActivity.this, "Please enter valid user id and password", Toast.LENGTH_LONG).show();
            return;
        }

        QueryPostData data = new QueryPostData(userNameInput, userPswdInput);

        //Creating a rest adapter
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(HiveHelper.HIVE_ROOT)
                .build();

        //Creating an object of our api interface
        BecareHiveApi api = adapter.create(BecareHiveApi.class);

        //Defining the method
        api.getUsers(data, new Callback<UserQueryResponse>() {

            @Override
            public void success(UserQueryResponse userQueryResponse, Response response) {
                if(userQueryResponse != null){
                    int count = userQueryResponse.getUserCount();
                    if(count > 0){
                        Toast.makeText(LoginActivity.this, "Number of users = " + count, Toast.LENGTH_LONG).show();
                        UserQueryResponse.UserData userData = userQueryResponse.getAllUsers().get(0);
                        preferenceStorage.setUserId(userData.getUserBecareId());
                        setResult(LOGIN_RESULT_SUCCESS);
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this, "User id and password does not match", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(LOGIN_RESULT_FAIL);
        finish();
    }

}
