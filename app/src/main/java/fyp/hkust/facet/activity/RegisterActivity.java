package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.TypefaceSpan;

public class RegisterActivity extends AppCompatActivity {


    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button mRegisterBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    private ImageButton passwordVisibleButton;
    private ImageButton confirmPasswordVisibleButton;
    private boolean pw_shown;
    private boolean cpw_shown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SpannableString s = new SpannableString("FaceT");
        s.setSpan(new TypefaceSpan(RegisterActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_register_layout), fontType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mNameField = (EditText) findViewById(R.id.usernamefield);
        mEmailField = (EditText) findViewById(R.id.emailfield);
        mPasswordField = (EditText) findViewById(R.id.passwordfield);
        mConfirmPasswordField = (EditText) findViewById(R.id.confirmpasswordfield);
        passwordVisibleButton = (ImageButton) findViewById(R.id.passwordfield_visible_button);
        confirmPasswordVisibleButton = (ImageButton) findViewById(R.id.confirmpasswordfield_visible_button);
        mRegisterBtn = (Button) findViewById(R.id.joinus_btn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

        //show pw
        pw_shown = false;
        passwordVisibleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pw_shown == true) {
                    //view pw
                    mPasswordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisibleButton.setImageResource(R.mipmap.ic_visibility_white_24dp);
                    pw_shown = false;
                } else {
                    //hide pw
                    mPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisibleButton.setImageResource(R.mipmap.ic_visibility_off_white_24dp);
                    pw_shown = true;
                }
            }
        });

        cpw_shown = false;
        confirmPasswordVisibleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cpw_shown == true) {
                    //view pw
                    mConfirmPasswordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmPasswordVisibleButton.setImageResource(R.mipmap.ic_visibility_white_24dp);
                    cpw_shown = false;
                } else {
                    //hide pw
                    mConfirmPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmPasswordVisibleButton.setImageResource(R.mipmap.ic_visibility_off_white_24dp);
                    cpw_shown = true;
                }
            }
        });
    }

    private void startRegister() {
        final String name = mNameField.getText().toString().trim();
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgress.setMessage("Signing Up ...");
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String user_id = mAuth.getCurrentUser().getUid();

                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        current_user_db.child("name").setValue(name);
                        current_user_db.child("email").setValue(email);
                        current_user_db.child("password").setValue(password);
                        current_user_db.child("image").setValue("default");

                        mProgress.dismiss();

                        Intent mainIntent = new Intent(RegisterActivity.this, ProfileEditActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
