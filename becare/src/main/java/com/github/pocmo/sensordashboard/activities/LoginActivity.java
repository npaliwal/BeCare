package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.NewUserPostData;
import com.github.pocmo.sensordashboard.model.QueryPostData;
import com.github.pocmo.sensordashboard.model.UserQueryResponse;
import com.github.pocmo.sensordashboard.network.BecareHiveApi;
import com.github.pocmo.sensordashboard.network.HiveHelper;
import com.google.inject.Inject;

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
    public static int LOGIN_RESULT_SKIPPED = 2;

    //Common UI elements
    TextView headerText;
    Button submitBtn;
    TextView skipToApp;

    //Login related  UI elements
    ViewGroup loginContnr;
    EditText loginUserName, loginUserPassword;
    TextView register, forgotPassword;

    //Register related  UI elements
    ViewGroup registerContnr;
    EditText regUserName;
    TextView alreadyResgistered;

    String userNameInput = "";
    String userProfileNameInput = "";
    String userPswdInput = "";
    private boolean showRegister = false;
    private String registerType = null;
    private BecareHiveApi api = null;

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

        headerText = (TextView)findViewById(R.id.header_text);
        submitBtn  = (Button)findViewById(R.id.btn_submit);
        skipToApp = (TextView)findViewById(R.id.tv_skip_registration);

        registerContnr = (ViewGroup)findViewById(R.id.register_contnr);
        regUserName = (EditText)registerContnr.findViewById(R.id.et_reg_name);
        alreadyResgistered = (TextView)registerContnr.findViewById(R.id.tv_already_registered);

        loginContnr = (ViewGroup)findViewById(R.id.login_cntnr);
        loginUserName = (EditText)loginContnr.findViewById(R.id.et_username);
        loginUserPassword = (EditText)loginContnr.findViewById(R.id.et_userpswd);
        register        = (TextView)loginContnr.findViewById(R.id.tv_register);
        forgotPassword  = (TextView)loginContnr.findViewById(R.id.tv_forgot_pswd);

        alreadyResgistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegister = false;
                switchView();
            }
        });

        skipToApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferenceStorage.setSkipReg(true);
                setResult(LOGIN_RESULT_SKIPPED);
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegister = true;
                switchView();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
    }

    private void switchView(){
        if(showRegister) {
            loginContnr.setVisibility(View.GONE);
            registerContnr.setVisibility(View.VISIBLE);
            headerText.setText("Register");
        }else{
            loginContnr.setVisibility(View.VISIBLE);
            registerContnr.setVisibility(View.GONE);
            headerText.setText("Login");
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_patient:
                if (checked)
                    this.registerType = getString(R.string.patient);
                    break;
            case R.id.radio_doctor:
                if (checked)
                    this.registerType = getString(R.string.doctor);
                    break;
        }
    }

    private void createNewUserProfile(NewUserPostData data){
        data.setupForProfile(userNameInput, userProfileNameInput, registerType);
        api.createUser(data, new Callback<UserQueryResponse>() {

            @Override
            public void success(UserQueryResponse userQueryResponse, Response response) {
                Toast.makeText(LoginActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();
                if(userQueryResponse != null){
                    int count = userQueryResponse.getUserCount();
                    if(count > 0){
                        UserQueryResponse.UserData userData = userQueryResponse.getAllUsers().get(0);
                        //preferenceStorage.setUserId(userData.getUserBecareId());
                        setResult(LOGIN_RESULT_SUCCESS);
                        finish();
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, "Something went wrong. Retry!!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewUser(){
        api = getApi();
        NewUserPostData data = new NewUserPostData(userNameInput, userPswdInput);
        api.createUser(data, new Callback<UserQueryResponse>() {

            @Override
            public void success(UserQueryResponse userQueryResponse, Response response) {
                Toast.makeText(LoginActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();
                if(userQueryResponse != null){
                    int count = userQueryResponse.getUserCount();
                    if(count > 0){
                        UserQueryResponse.UserData userData = userQueryResponse.getAllUsers().get(0);
                        //preferenceStorage.setUserId(userData.getUserBecareId());
                        setResult(LOGIN_RESULT_SUCCESS);
                        finish();
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, "Something went wrong. Retry!!", Toast.LENGTH_LONG).show();
            }
        });

        createNewUserProfile(data);
    }

    private BecareHiveApi getApi(){
        if(api != null)
            return api;

        //Creating a rest adapter
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(HiveHelper.HIVE_ROOT)
                .build();

        api = adapter.create(BecareHiveApi.class);
        return api;
    }

    public void onSubmit(){
        userNameInput = "";
        userPswdInput = "";
        String message = null;
        QueryPostData data = null;

        if(showRegister){
            userNameInput = regUserName.getText().toString();
            if(TextUtils.isEmpty(userNameInput)){
                message = "Please enter valid username";
            } else if(this.registerType == null){
                message = "Please select either patient / doctor";
            }else{
                data = new QueryPostData(userNameInput);
            }
        }else{
            userNameInput = loginUserName.getText().toString();
            userPswdInput = loginUserPassword.getText().toString();
            if(TextUtils.isEmpty(userNameInput) || TextUtils.isEmpty(userPswdInput)){
                message = "Please enter valid username and password";
            }else {
                data = new QueryPostData(userNameInput, userPswdInput);
            }
        }

        if(!TextUtils.isEmpty(message)){
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            return;
        }


        api = getApi();
        api.getUsers(data, new Callback<UserQueryResponse>() {
            @Override
            public void success(UserQueryResponse userQueryResponse, Response response) {
                String message = null;
                if(userQueryResponse != null){
                    int count = userQueryResponse.getUserCount();
                    if(count > 0){
                        if(showRegister) {
                            message = "Username already exist";
                        }else {
                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_LONG).show();
                            UserQueryResponse.UserData userData = userQueryResponse.getAllUsers().get(0);
                            //preferenceStorage.setUserId(userData.getUserBecareId());
                            setResult(LOGIN_RESULT_SUCCESS);
                            finish();
                        }
                    }else{
                        if(showRegister){
                            message = "Creating new user profile";
                            createNewUser();
                        }else {
                            message = "Username and password does not match";
                        }
                    }
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, "Something went wrong. Retry!!", Toast.LENGTH_LONG).show();
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
