package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.Arrays;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.CheckConnectivity;
import fyp.hkust.facet.util.FontManager;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private RelativeLayout activity_login_layout;

    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private Button mLoginBtn;
    private Button mGoRegister;
    private Button mFacebookBtn;
    private Button mGoogleBtn;
    private ImageButton passwordVisibleButton;
    private CheckBox mRemeberPasswordCheckBox;
    private boolean pw_shown;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;

    private ProgressDialog mProgress;

    private static final int RC_SIGN_IN = 1;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth.AuthStateListener mAuthListener;

    //facebook login use
    private CallbackManager mCallbackManager;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_login);

        activity_login_layout = (RelativeLayout) findViewById(R.id.activity_login_layout);

        CheckConnectivity check = new CheckConnectivity();
        Boolean conn = check.checkNow(this.getApplicationContext());
        if (conn == true) {
            //run your normal code path here
            Log.d(TAG, "Network connected");
        } else {
            //Send a warning message to the user
            Snackbar snackbar = Snackbar.make(activity_login_layout, "No Internet Access", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_login_layout), fontType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mProgress = new ProgressDialog(this);

        mLoginEmailField = (EditText) findViewById(R.id.loginemailfield);
        mLoginEmailField.setBackgroundColor(111);
        mLoginPasswordField = (EditText) findViewById(R.id.loginpasswordfield);
        passwordVisibleButton = (ImageButton) findViewById(R.id.passwordfield_visible_button);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mGoRegister = (Button) findViewById(R.id.goregister_btn);
        mGoogleBtn = (Button) findViewById(R.id.signingooglebtn);
        mFacebookBtn = (Button) findViewById(R.id.facebook_login_btn);
        mRemeberPasswordCheckBox = (CheckBox) findViewById(R.id.remember_pw_checkbox);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        mLoginEmailField.setText(sharedPreferences.getString("email", ""));
        mLoginPasswordField.setText(sharedPreferences.getString("password", ""));

        mRemeberPasswordCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "check box is Checked");
                }
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRemeberPasswordCheckBox.isChecked())
                    saveLoginData();
                checklogin();
            }
        });

        //if clicked the simple login will fade in and social login will disappear
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.signingooglebtn));
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.passwordfield_visible_button));
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.loginemailfield));
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.loginpasswordfield));
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.login_btn));
        YoYo.with(Techniques.FlipInX).duration(1000).playOn(findViewById(R.id.facebook_login_btn));

        //show pw
        pw_shown = false;
        passwordVisibleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pw_shown == true) {
                    //view pw
                    mLoginPasswordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisibleButton.setImageResource(R.mipmap.ic_visibility_white_24dp);
                    pw_shown = false;
                } else {
                    //hide pw
                    mLoginPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisibleButton.setImageResource(R.mipmap.ic_visibility_off_white_24dp);
                    pw_shown = true;
                }
            }
        });

        mGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
//                regIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(regIntent);
            }
        });

        //------------Google Sign in----------

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        //accessToken之後或許還會用到 先存起來
                        accessToken = loginResult.getAccessToken();
                        handleFacebookAccessToken(accessToken);
                        Log.d("FB", "access token got : " + accessToken);
                        //send request and call graph api
                        GraphRequest request = GraphRequest.newMeRequest(
                                accessToken,

                                new GraphRequest.GraphJSONObjectCallback() {
                                    //當RESPONSE回來的時候
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        //讀出姓名 ID FB個人頁面連結
                                        Log.d(TAG + "FB", "complete");
                                        Log.d("FB object", object.toString());
                                        Log.d("FB graphResponse", response.toString());
                                        Log.d("FB", object.optString("name"));
                                        Log.d("FB", object.optString("link"));
                                        Log.d("FB", object.optString("id"));
                                        Log.d("FB", object.optString("email"));
                                        Log.d("FB", object.optString("birthday"));
                                    }
                                });
                        //包入你想要得到的資料 送出request
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,link,email,birthday,gender,picture");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        mFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Facebook Login
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(
                        "public_profile", "email", "user_birthday", "user_friends"));
            }
        });

    }

    private void saveLoginData() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!mLoginEmailField.getText().toString().trim().equals("") || !mLoginPasswordField.getText().toString().trim().equals("")) {
            editor.putString("email", mLoginEmailField.getText().toString());
            editor.putString("password", mLoginPasswordField.getText().toString());
            editor.commit();
            Snackbar snackbar = Snackbar.make(activity_login_layout, "Login data is remember", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
        return true;
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            mProgress.setMessage("Starting Sign in ...");
            mProgress.show();

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                mProgress.dismiss();
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mProgress.dismiss();
                            checkUserExist();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mProgress.dismiss();
                            checkUserExist();
                        }

                    }
                });
    }

    private void checklogin() {

        String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgress.setMessage("Checking Login ...");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mProgress.dismiss();
                        checkUserExist();
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    } else {
                        Intent setupIntent = new Intent(LoginActivity.this, ProfileEditActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Login Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        mGoogleApiClient.disconnect();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }
}
