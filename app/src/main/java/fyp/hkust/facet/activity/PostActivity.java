package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.fragments.BackConfirmationFragment;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;
import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;

public class PostActivity extends AppCompatActivity implements VerticalStepperForm {

    private static final int STEP1_NUM = 0;
    private static final int STEP2_NUM = 1;
    private static final int STEP3_NUM = 2;

    public static final String STATE_1 = "Product Information";
    public static final String STATE_2 = "Product Image";
    //    public static final String STATE_3 = "Product Summary";
    private static final String TAG = "PostActivity";

    private ImageButton mSelectImage;
    private EditText mProductTitle;
    private EditText mProductDesc;
    private EditText mProductBrand;
    private EditText mProductTypeSpinner;
    private Spinner spinner;

    private Button mSubmitBtn;

    private Uri mImageUri;

    private static final int GALLERY_REQUEST = 1;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;

    private VerticalStepperFormLayout verticalStepperForm;
    private boolean confirmBack = true;

    private String saved_title, saved_desc, saved_brand;
    private TextView confirm_product_title,confirm_product_brand,confirm_product_desc;
    private ImageView confirm_product_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_stepper_form);

        initializeActivity();

    }

    private void initializeActivity() {

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_vertical_stepper_form_layout), fontType);
        FontManager.markAsIconContainer(findViewById(R.id.activity_post_layout), fontType);
        FontManager.markAsIconContainer(findViewById(R.id.activity_post_summary_layout), fontType);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        // Vertical Stepper form vars
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        String[] stepsTitles = getResources().getStringArray(R.array.steps_titles);
        //String[] stepsSubtitles = getResources().getStringArray(R.array.steps_subtitles);

        // Here we find and initialize the form
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(R.id.vertical_stepper_form);
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, stepsTitles, this, this)
                //.stepsSubtitles(stepsSubtitles)
                //.materialDesignInDisabledSteps(true) // false by default
                //.showVerticalLineWhenStepsAreCollapsed(true) // false by default
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .displayBottomNavigation(true)
                .showVerticalLineWhenStepsAreCollapsed(true)
                .materialDesignInDisabledSteps(true)
                .init();
    }

    @Override
    public View createStepContentView(int stepNumber) {
        // Here we generate the content view of the correspondent step and we return it so it gets
        // automatically added to the step layout (AKA stepContent)
        View view = null;
        switch (stepNumber) {
            case STEP1_NUM:
                view = createView();
                break;
            case STEP2_NUM:
                view = createView2();
                break;
            case STEP3_NUM:
                view = createSummaryStep();
                break;
        }
        return view;
    }


    private View createView() {

        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        RelativeLayout view =
                (RelativeLayout) inflater.inflate(R.layout.activity_post, null, false);

        mProductTitle = (EditText) view.findViewById(R.id.product_title);
        mProductDesc = (EditText) view.findViewById(R.id.product_desc);
        mProductBrand = (EditText) view.findViewById(R.id.product_brand_edittext);

        spinner = (Spinner) view.findViewById(R.id.product_type_spinner);
        ArrayAdapter<CharSequence> typeList = ArrayAdapter.createFromResource(PostActivity.this,
                R.array.product_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(typeList);

        mProgress = new ProgressDialog(this);
//        mSubmitBtn = (Button) view.findViewById(R.id.submit_btn);
//        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startPosting();
//            }
//        });
        return view;
    }

    private View createView2() {
        LayoutInflater inflater2 = LayoutInflater.from(getBaseContext());
        LinearLayout view2 =
                (LinearLayout) inflater2.inflate(R.layout.activity_post_image, null, false);
        mSelectImage = (ImageButton) view2.findViewById(R.id.imageButton);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        return view2;
    }

    private View createSummaryStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater3 = LayoutInflater.from(getBaseContext());
        LinearLayout summaryLayoutContent = (LinearLayout) inflater3.inflate(R.layout.activity_post_summary, null, false);

        confirm_product_title = (TextView) summaryLayoutContent.findViewById(R.id.confirm_product_title);
        confirm_product_brand = (TextView) summaryLayoutContent.findViewById(R.id.confirm_product_brand);
        confirm_product_desc = (TextView) summaryLayoutContent.findViewById(R.id.confirm_product_desc);
        confirm_product_image = (ImageView) summaryLayoutContent.findViewById(R.id.confirm_product_image);

        return summaryLayoutContent;
    }

    @Override
    public void onStepOpening(int stepNumber) {
        Log.d(TAG, stepNumber + "");
        switch (stepNumber) {
            case STEP1_NUM:
                // When this step is open, we check that the title is correct
//                saved_title = mProductTitle.getText().toString().trim();
//                saved_desc = mProductDesc.getText().toString().trim();
//                Log.d(TAG + " Data", saved_title);
                verticalStepperForm.setStepAsCompleted(stepNumber);
                break;
            case STEP2_NUM:
                confirm_product_title.setText(mProductTitle.getText().toString().trim());
                Log.d(TAG + " product_title" , mProductTitle.getText().toString().trim());
                confirm_product_brand.setText(mProductBrand.getText().toString().trim());
                confirm_product_desc.setText(mProductDesc.getText().toString().trim());
                verticalStepperForm.setStepAsCompleted(stepNumber);

                break;
            case STEP3_NUM:
                verticalStepperForm.setStepAsCompleted(stepNumber);
                break;
        }
    }

//    private void checkTyping() {
//        if (mProductTitle.length() >= 3 && mProductTitle.length() <= 20) {
//            verticalStepperForm.setActiveStepAsCompleted();
//        } else {
//            // This error message is optional (use null if you don't want to display an error message)
//            String errorMessage = "The Title must have between 3 and 40 characters";
//            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
//        }
//
//        if (mProductDesc.length() >= 10 && mProductDesc.length() <= 120) {
//            verticalStepperForm.setActiveStepAsCompleted();
//        } else {
//            // This error message is optional (use null if you don't want to display an error message)
//            String errorMessage = "The Title must have between 3 and 40 characters";
//            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
//        }
//    }

    @Override
    public void sendData() {

        startPosting();
    }

    private void startPosting() {

        mProgress.setMessage("Posting to Product...");
        mProgress.show();
        final String title_val = mProductTitle.getText().toString().trim();
        final String desc_val = mProductDesc.getText().toString().trim();
        final String brand_val = mProductBrand.getText().toString().trim();
        Log.d(TAG + " PostData", title_val + " : " + desc_val);
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && !TextUtils.isEmpty(brand_val) && mImageUri != null) {
            StorageReference filepath = mStorage.child("Product_Image").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //create a random id

                    final DatabaseReference newProduct = mDatabase.push();

                    Log.d(TAG + " image", downloadUrl.toString());
                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newProduct.child("title").setValue(title_val);
                            newProduct.child("desc").setValue(desc_val);
                            newProduct.child("brand").setValue(brand_val);
                            newProduct.child("image").setValue(downloadUrl.toString());
                            newProduct.child("uid").setValue(mCurrentUser.getUid());

                            newProduct.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mProgress.dismiss();

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
            confirm_product_image.setImageURI(mImageUri);
        }
    }

    // SAVING AND RESTORING THE STATE

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Saving title field
        if (mProductTitle != null && mProductDesc != null) {
            savedInstanceState.putString("product_title", mProductTitle.getText().toString());
//            Log.d(TAG + " product_title", mProductTitle.getText().toString().trim());
            savedInstanceState.putString("product_desc", mProductDesc.getText().toString());
            savedInstanceState.putString("product_brand", mProductBrand.getText().toString());
        }

//        // Saving description field
//        if(descriptionEditText != null) {
//            savedInstanceState.putString(STATE_2, descriptionEditText.getText().toString());
//        }


        // The call to super method must be at the end here
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        // Restoration of title field
        if (savedInstanceState.containsKey(STATE_1)) {
            mProductTitle.setText(savedInstanceState.getString("product_title"));
            mProductDesc.setText(savedInstanceState.getString("product_desc"));
            mProductBrand.setText(savedInstanceState.getString("product_brand"));
        }

        // Restoration of description field
        if (savedInstanceState.containsKey(STATE_2)) {
        }

        // The call to super method must be at the end here
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void confirmBack() {
        if (confirmBack && verticalStepperForm.isAnyStepCompleted()) {
            BackConfirmationFragment backConfirmation = new BackConfirmationFragment();
            backConfirmation.setOnConfirmBack(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmBack = true;
                }
            });
            backConfirmation.setOnNotConfirmBack(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmBack = false;
                    finish();
                }
            });
            backConfirmation.show(getSupportFragmentManager(), null);
        } else {
            confirmBack = false;
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && confirmBack) {
            confirmBack();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        confirmBack();
    }

}
