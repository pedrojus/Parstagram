package com.example.parstagram;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int CAPTURE_IMAGE_REQUEST_CODE = 42;
    private ProgressBar pb;
    private EditText etDescription;
    private ImageView ivPostImage;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnLogout;
    File photoFile;

    public String photoFileName = "photo.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // finding the UI elements from xml
        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnLogout = findViewById(R.id.btnLogout);
        pb = findViewById(R.id.pb);

        //queryPosts();

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
                    Toast.makeText(MainActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(MainActivity.this, "There is no image.", Toast.LENGTH_SHORT).show();
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
                if (intent.resolveActivity(getPackageManager()) != null) {
                    Toast.makeText(view.getContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                    loginActivityLauncher.launch(intent);
                }
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    ActivityResultLauncher<Intent> loginActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this, "Unable to log out!", Toast.LENGTH_SHORT).show();

                }
            });

    private void launchCamera() {
        // retrieves the activity of the Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this,
                "com.example.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraActivityResultLauncher.launch(intent);
        }
    }

    private File getPhotoFileUri(String photoFileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
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
                            ImageView ivPreview = (ImageView) findViewById(R.id.ivPostImage);
                            ivPostImage.setImageBitmap(takenImage);
                        } else {
                            Toast.makeText(MainActivity.this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                        }
                }
            });

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        pb.setVisibility(ProgressBar.VISIBLE);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.e(TAG, "Issue saving post " + e);
                    return;
                }
                pb.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(MainActivity.this, "Post successful!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Post was successful!");
                etDescription.setText("");
                ivPostImage.setImageResource(0);
            }
        });
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + " username: " + post.getUser().getUsername());
                }
            }
        });
    }
}