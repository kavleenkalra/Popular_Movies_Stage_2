package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MovieInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_information);

        if(savedInstanceState==null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle args=new Bundle();
            args.putParcelable("movieObj",getIntent().getParcelableExtra("movieObj"));

            MovieInformationActivityFragment fragment=new MovieInformationActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container,fragment)
                    .commit();
        }
    }

}
