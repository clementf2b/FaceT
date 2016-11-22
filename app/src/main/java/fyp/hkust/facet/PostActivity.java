package fyp.hkust.facet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSelectImage = (ImageButton) findViewById(R.id.imageButton);
        mProductTitle = (EditText) findViewById(R.id.photo_title);
        mProductDesc = (EditText) findViewById(R.id.photo_desc);

        mSubmitBtn = (Button) findViewById(R.id.submit_btn);

        mProgress = new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {

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

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //create a random id
                    final DatabaseReference newProduct = mDatabase.push();


                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newProduct.child("title").setValue(title_val);
                            newProduct.child("desc").setValue(desc_val);
                            newProduct.child("image").setValue(downloadUrl.toString());
                            newProduct.child("uid").setValue(mCurrentUser.getUid());

                            newProduct.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        startActivity(new Intent(PostActivity.this,MainActivity.class));
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

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
        }
    }
}
