package fyp.hkust.facet;

<<<<<<< HEAD
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
=======
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
>>>>>>> b0e6327961cf20098884c5f17836f8ea290ebe90
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
<<<<<<< HEAD

public class PostActivity extends AppCompatActivity {


    private ImageButton mSelectBtn;
    private EditText mProductTitle;
    private EditText mProductDesc;
    private Button mSubmitBtn;

    private static final int GALLERY_REQUEST = 1;

=======
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mProductTitle;
    private EditText mProductDesc;

    private Button mSubmitBtn;

    private Uri mImageUri;

    private static final int GALLERY_REQUEST = 1;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

>>>>>>> b0e6327961cf20098884c5f17836f8ea290ebe90
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

<<<<<<< HEAD
        mSelectBtn = (ImageButton) findViewById(R.id.imageButton);
        mProductTitle = (EditText) findViewById(R.id.photo_title);
        mProductDesc = (EditText) findViewById(R.id.photo_desc);
        mSubmitBtn = (Button) findViewById(R.id.submit_btn);

        mSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

=======
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");

        mSelectImage = (ImageButton) findViewById(R.id.imageButton);
        mProductTitle = (EditText) findViewById(R.id.photo_title);
        mProductDesc = (EditText) findViewById(R.id.photo_desc);

        mSubmitBtn = (Button) findViewById(R.id.submit_btn);

        mProgress = new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
>>>>>>> b0e6327961cf20098884c5f17836f8ea290ebe90
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
<<<<<<< HEAD
=======

>>>>>>> b0e6327961cf20098884c5f17836f8ea290ebe90
                startPosting();
            }
        });
    }

    private void startPosting() {
<<<<<<< HEAD
        String titl_val =

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            mSelectBtn.setImageURI(imageUri);
=======

        mProgress.setMessage("Posting to Product...");
        mProgress.show();
        final String title_val = mProductTitle.getText().toString().trim();
        final String desc_val = mProductDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null)
        {
            StorageReference filepath = mStorage.child("Product_Image").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //create a random id
                    DatabaseReference newProduct = mDatabase.push();

                    newProduct.child("title").setValue(title_val);
                    newProduct.child("dsec").setValue(desc_val);
                    newProduct.child("image").setValue(downloadUrl.toString());

                    mProgress.dismiss();

                    startActivity(new Intent(PostActivity.this,MainActivity.class));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
>>>>>>> b0e6327961cf20098884c5f17836f8ea290ebe90
        }
    }
}
