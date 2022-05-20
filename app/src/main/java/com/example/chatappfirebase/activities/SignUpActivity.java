package com.example.chatappfirebase.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.databinding.ActivitySignUpBinding;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //  This class handles the shared preferences
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateSignIn.setOnClickListener(view -> onBackPressed());

        binding.buttonSignUp.setOnClickListener(v -> {
            //  If all the fields have been filled correctly, you can sign up the user
            if (isValidSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        //  Set loading to true
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();

        //  Storing the data in Firebase
        user.put(Constants.KEY_NAME, binding.inputCreateName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputCreateEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputCreatePassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        //  Create a new database collection
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                    //  Stop the loading image
                    loading(false);
                    //  set up the sharedpreferences to save the state
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputCreateName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {

                    //  Don't stop loading
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    //  encoding the chosen image
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        //  The preview bitmap
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //  Picking image from gallery
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            //  Set the image picked by the user
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.dummyImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidSignUpDetails() {
        //  if no image has been chosen
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;

        } else if (binding.inputCreateName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;

        } else if (binding.inputCreateEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email Address");
            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputCreateEmail.getText().toString()).matches()) {
            //  If the email isn't a valid email address
            showToast("Enter a valid email address");
            return false;

        } else if (binding.inputCreateEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;

        } else if (binding.inputCreateConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;

        } else if (!binding.inputCreatePassword.getText().toString().equals(binding.inputCreateConfirmPassword.getText().toString())) {
            showToast("Passwords don't match");
            return false;

        } else {
            return true;
        }
    }

    //  check whether signing up is loading or not
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.signUpProgress.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.signUpProgress.setVisibility(View.INVISIBLE);
        }
    }







}