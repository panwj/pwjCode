package com.ex.simi;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class PictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_activity);

        String path = getIntent().getStringExtra("picture_path");
        ImageView img = findViewById(R.id.image);
        try {
            Glide.with(getApplicationContext())
                    .load(path)
                    .centerInside()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
