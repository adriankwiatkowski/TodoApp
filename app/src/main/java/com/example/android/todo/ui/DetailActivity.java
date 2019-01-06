package com.example.android.todo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.todo.R;

public class DetailActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            int todoId;
            if (intent != null) {
                todoId = intent.getIntExtra(DetailFragment.TODO_ID_KEY, DetailFragment.DEFAULT_TODO_ID);
            } else {
                todoId = DetailFragment.DEFAULT_TODO_ID;
            }
            DetailFragment detailFragment = DetailFragment.newInstance(todoId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, detailFragment, MainActivity.DETAIL_FRAG_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DetailFragment detailFragment = (DetailFragment)
                getSupportFragmentManager().findFragmentByTag(MainActivity.DETAIL_FRAG_TAG);
        if (detailFragment == null || !detailFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
