package com.example.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("sTmRutKdyfXkNgvE7LothbN1VQGSFMdNq3l14i1v")
                .clientKey("tuSiigno03qJcMfbaS0FTWYtk6OZKRwHUpv9nr6E")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
