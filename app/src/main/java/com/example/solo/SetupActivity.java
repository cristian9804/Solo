package com.example.solo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText Username, Fullname, ContryName;
    private Button SaveInformation;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    String currentUserID;
    final static int Gallery_Pick =1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);


        Username = (EditText) findViewById(R.id.setup_username);
        Fullname = (EditText) findViewById(R.id.setup_full_name);
        ContryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformation = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);


        SaveInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && requestCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (requestCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();
            }
        }
    }

    private void SaveAccountSetupInformation()
    {
        String username = Username.getText().toString();
        String fullname = Fullname.getText().toString();
        String country = ContryName.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new Account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey there, I am using Solo");
            userMap.put("gender","none");
            userMap.put("dob","null");
            userMap.put("relationshipstatus","none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account has been created successfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured:"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
