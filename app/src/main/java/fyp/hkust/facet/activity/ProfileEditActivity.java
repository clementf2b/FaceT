package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.TypefaceSpan;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String TAG = ProfileEditActivity.class.getSimpleName();
    private MaterialEditText usernameEdittext;
    private MaterialEditText passwordEdittext;
    private MaterialEditText confirmPasswordEdittext;
    private MaterialEditText emailEdittext;

    private CircleImageView editProfilepic;
    private EmojiPopup emojiPopup;
    private EmojiEditText aboutMeEdittext;
    private Spinner skinTypeSpinner;
    private View rootView;
    private ImageView emojiButton;
    private FirebaseUser user;
    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button maleButton;
    private Button femaleButton;
    private boolean gender = true;

    private ProgressDialog mProgress;
    private String oldpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        rootView = findViewById(R.id.activity_profile_edit_layout);
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_profile_edit_layout), fontType);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SpannableString s = new SpannableString("Edit Profile");
        s.setSpan(new TypefaceSpan(ProfileEditActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageProfileImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        user = FirebaseAuth.getInstance().getCurrentUser();

        usernameEdittext = (MaterialEditText) findViewById(R.id.username_edittext);
        passwordEdittext = (MaterialEditText) findViewById(R.id.password_edittext);
        passwordEdittext.setTransformationMethod(PasswordTransformationMethod.getInstance());

        confirmPasswordEdittext = (MaterialEditText) findViewById(R.id.confirm_password_edittext);
        confirmPasswordEdittext.setTransformationMethod(PasswordTransformationMethod.getInstance());

        emailEdittext = (MaterialEditText) findViewById(R.id.email_edittext);
        maleButton = (Button) findViewById(R.id.male_btn);
        femaleButton = (Button) findViewById(R.id.female_btn);
        editProfilepic = (CircleImageView) findViewById(R.id.editprofilepic);
        aboutMeEdittext = (EmojiEditText) findViewById(R.id.about_me_edittext);
        skinTypeSpinner = (Spinner) findViewById(R.id.skin_type_spinner);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);

        emailEdittext.setEnabled(false);

        aboutMeEdittext.setScroller(new Scroller(getApplicationContext()));
        aboutMeEdittext.setVerticalScrollBarEnabled(true);
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderClickFunction(1);
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderClickFunction(2);
            }
        });

        ArrayAdapter<CharSequence> skinTypeArrayList = ArrayAdapter.createFromResource(ProfileEditActivity.this,
                R.array.skin_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(skinTypeArrayList);

        editProfilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.keepSynced(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(ProfileEditActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        // username change listener
        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                emailEdittext.setText(user.getEmail());
                if (dataSnapshot.getValue() != null) {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final User user_data = dataSnapshot.getValue(User.class);
                    Log.e(user_data.getName(), "User data is null!");
                    usernameEdittext.setText(user_data.getName());

                    if (user_data.getPassword() != null) {
                        oldpass = user_data.getPassword();
                        passwordEdittext.setText(user_data.getPassword());
                        confirmPasswordEdittext.setText(user_data.getPassword());
                    }
                    if (user_data.getAboutMe() != null)
                        aboutMeEdittext.setText(user_data.getAboutMe());
                    Picasso.with(getApplicationContext()).load(user_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(editProfilepic, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext())
                                    .load(user_data.getImage())
                                    .centerCrop()
                                    .fit()
                                    .into(editProfilepic);
                        }
                    });
                    // Check for null
                    if (user == null) {
                        Log.e(TAG, "User data is null!");
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read username value.", error.toException());
            }
        });

        setUpEmojiPopup();
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
            this.finish();
        }
    }

    @Override
    protected void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }
        super.onStop();
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
                    @Override
                    public void onEmojiBackspaceClicked(final View v) {
                        emojiPopup.dismiss();
                        Log.d(TAG, "Clicked on Backspace");
                    }
                })
                .setOnEmojiClickedListener(new OnEmojiClickedListener() {
                    @Override
                    public void onEmojiClicked(final Emoji emoji) {
                        Log.d(TAG, "Clicked on emoji");
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        emojiButton.setImageResource(R.drawable.ic_keyboard);
                    }
                })
                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(final int keyBoardHeight) {
                        Log.d(TAG, "Opened soft keyboard");
                    }
                })
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        emojiPopup.dismiss();
                        emojiButton.setImageResource(R.drawable.emoji_one_category_people);
                    }
                })
                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        emojiPopup.dismiss();
                        Log.d(TAG, "Closed soft keyboard");
                    }
                })
                .build(aboutMeEdittext);
    }

    private void saveData() {
        final String email = emailEdittext.getText().toString().trim();
        final String name = usernameEdittext.getText().toString().trim();
        final String password = passwordEdittext.getText().toString().trim();
        final String gender_result;
        if (gender == true)
            gender_result = "Male";
        else
            gender_result = "Female";
        final String user_id = mAuth.getCurrentUser().getUid();
        final String aboutMe = aboutMeEdittext.getText().toString();

        if (!TextUtils.isEmpty(name)) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Finishing Setup ...");
            mProgress.show();

            if (mImageUri != null) {
                StorageReference filepath = mStorageProfileImage.child(mImageUri.getLastPathSegment());

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String downloadUri = taskSnapshot.getDownloadUrl().toString();
                        mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);
                    }
                });
            }
            mDatabaseUsers.child(user_id).child("name").setValue(name);
            mDatabaseUsers.child(user_id).child("password").setValue(password);
            mDatabaseUsers.child(user_id).child("uid").setValue(mAuth.getCurrentUser().getUid());

            mDatabaseUsers.child(user_id).child("gender").setValue(gender_result);
            mDatabaseUsers.child(user_id).child("aboutMe").setValue(aboutMe);

            //change the password in the firebase
            String provider = user.getProviders().get(0);
            Log.d("provider", provider + " " + user.getProviders().size());
            if (password.length() > 0) {
                user.updatePassword(password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User password updated. new password: " + password);
                                }
                            }
                        });
            }

            mProgress.dismiss();

            Intent mainIntent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }

    public void genderClickFunction(int result) {

        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (result == 1) {

            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                maleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                femaleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
            } else {
                maleButton.setBackground(getResources().getDrawable(R.drawable.border_button_with_bg));
                femaleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
            }
            maleButton.setTextColor(getResources().getColor(R.color.white));
            femaleButton.setTextColor(getResources().getColor(R.color.font_color_pirmary));
            gender = true;
            Log.d(TAG + " Gender", " male btn clicked");

        } else if (result == 2) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                femaleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                maleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
            } else {
                femaleButton.setBackground(getResources().getDrawable(R.drawable.border_button_with_bg));
                maleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
            }
            maleButton.setTextColor(getResources().getColor(R.color.font_color_pirmary));
            femaleButton.setTextColor(getResources().getColor(R.color.white));
            gender = false;
            Log.d(TAG + " Gender", " female btn clicked");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                editProfilepic.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG + "crop_error", error.toString());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.save_btn) {
            saveData();
        }
        if (item.getItemId() == android.R.id.home) {
            // app icon in action bar clicked; goto parent activity.
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
