package com.example.parstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.example.parstagram.Post;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private EditText etDescription;
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        //queryPosts();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser);
            }
        });
    }

    private void savePost(String description, ParseUser currentUser) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.e(TAG, "Issue saving post " + e);
                    return;
                }
                Log.i(TAG, "Post was successful!");
                etDescription.setText("");
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