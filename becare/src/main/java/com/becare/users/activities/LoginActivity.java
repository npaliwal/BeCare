package com.becare.users.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.becare.users.PreferenceStorage;
import com.becare.users.R;
import com.becare.users.events.BusProvider;
import com.becare.users.events.LoginCompleted;
import com.becare.users.model.NewUserPostData;
import com.becare.users.model.QueryPostData;
import com.becare.users.model.UserQueryResponse;
import com.becare.users.network.BecareHiveApi;
import com.becare.users.network.HiveHelper;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.inject.Inject;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    public static int REQUEST_LOGIN_CODE = 1;
    public static int LOGIN_RESULT_FAIL = 0;
    public static int LOGIN_RESULT_SUCCESS = 1;
    public static int LOGIN_RESULT_SKIPPED = 2;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = LoginActivity.class.getSimpleName();

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

    private GoogleApiClient mGoogleApiClient;

    CallbackManager callbackManager;

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

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Plus.SCOPE_PLUS_PROFILE)
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_google);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.sign_in_facebook);
        //loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());
                                        try {
                                            // Application code
                                            String email = object.getString("email");
                                            String name = object.optString("name");
                                            preferenceStorage.setUserInfo(name, email);
                                            preferenceStorage.setUserId(email);
                                            BusProvider.postOnMainThread(new LoginCompleted(name));
                                        }catch (Exception e){
                                            Log.d(TAG, e.getMessage());
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                        setResult(LOGIN_RESULT_SUCCESS);
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "user cancelled");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, error.getMessage());
                    }
                });

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
        findViewById(R.id.sign_in_google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("loginActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            preferenceStorage.setUserInfo(acct.getDisplayName(), acct.getEmail());
            preferenceStorage.setUserId(acct.getEmail());
            setResult(LOGIN_RESULT_SUCCESS);
            finish();
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
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
                if (userQueryResponse != null) {
                    int count = userQueryResponse.getUserCount();
                    if (count > 0) {
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
                if (userQueryResponse != null) {
                    int count = userQueryResponse.getUserCount();
                    if (count > 0) {
                        UserQueryResponse.UserData userData = userQueryResponse.getAllUsers().get(0);
                        preferenceStorage.setUserId(userData.getUserBecareId());
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
                            preferenceStorage.setUserId(userData.getUserBecareId());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
