package com.crazysunj.cardslide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class TestActivity extends AppCompatActivity {

    private static final String URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        Glide.with(this).load(getIntent().getStringExtra(URL)).into(imageView);
    }

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, TestActivity.class);
        intent.putExtra(URL, url);
        context.startActivity(intent);
    }
}
