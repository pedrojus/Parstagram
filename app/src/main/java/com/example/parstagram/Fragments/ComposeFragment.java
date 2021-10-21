package com.example.parstagram.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.parstagram.Activities.LoginActivity;
import com.example.parstagram.Activities.MainActivity;
import com.example.parstagram.Post;
import com.example.parstagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class ComposeFragment extends Fragment {

    public static final int CAPTURE_IMAGE_REQUEST_CODE = 42;
    private static final String TAG = "ComposeFragment";
    private ProgressBar pb;
    private EditText etDescription;
    private ImageView ivPostImage;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnLogout;
    File photoFile;
    public String photoFileName = "photo.jpg";

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // finding the UI elements from xml
        etDescription = view.findViewById(R.id.etDescription);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnLogout = view.findViewById(R.id.btnLogout);
        pb = view.findViewById(R.id.pb);

        // "Take Picture" button action
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        launchCamera();
                    }
                });
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(getContext(), "There is no image.", Toast.LENGTH_SHORT).show();
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser, photoFile);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOutInBackground();
                ParseUser currentUser = ParseUser.getCurrentUser();
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    Toast.makeText(view.getContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                    loginActivityLauncher.launch(intent);
                }
            }
        });
    }
    private void launchCamera () {
        // retrieves the activity of the Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(getContext(),
                "com.example.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            cameraActivityResultLauncher.launch(intent);
        }
    }

    private File getPhotoFileUri (String photoFileName){
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory!");
        }
        return new File(mediaStorageDir.getPath() + File.separator + photoFileName);
    }

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // get camera photo from disk
                        Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        // load taken image into a preview
                        ImageView ivPreview = (ImageView) getView().findViewById(R.id.ivPostImage);
                        ivPostImage.setImageBitmap(takenImage);
                    } else {
                        Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void savePost (String description, ParseUser currentUser, File photoFile){
        Post post = new Post();
        post.setDescription(description);
        if (photoFile != null)
            post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        pb.setVisibility(ProgressBar.VISIBLE);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue saving post " + e);
                    return;
                }
                pb.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(getContext(), "Post successful!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Post was successful!");
                etDescription.setText("");
                ivPostImage.setImageResource(0);
            }
        });
    }

    ActivityResultLauncher<Intent> loginActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getContext(), "Unable to log out!", Toast.LENGTH_SHORT).show();

                }
            });


}