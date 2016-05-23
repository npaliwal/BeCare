package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
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

    EditText userName, userPassword;
    TextView register, forgotPassword;
    Button submitBtn;

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

        userName        = (EditText)findViewById(R.id.et_username);
        userPassword    = (EditText)findViewById(R.id.et_userpswd);
        register        = (TextView)findViewById(R.id.tv_register);
        forgotPassword  = (TextView)findViewById(R.id.tv_forgot_pswd);
        submitBtn       = (Button)findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUser();
            }
        });
    }

    public void checkUser(){
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
