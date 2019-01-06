package com.example.android.todo.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.android.todo.R;
import com.example.android.todo.utilities.NotificationUtils;

public class MainActivity extends AppCompatActivity implements ListFragment.OnTodoSelected {

    public static final String DETAIL_FRAG_TAG = "detail-fragment";

    private Toolbar mToolbar;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwoPane = getResources().getBoolean(R.bool.two_pane);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {

            if (mTwoPane) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_container, new ListFragment())
                        .replace(R.id.item_detail_container,
                                DetailFragment.newInstance(DetailFragment.DEFAULT_TODO_ID),
                                DETAIL_FRAG_TAG)
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_container, new ListFragment())
                        .commit();
            }
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(NotificationUtils.TODO_ID_KEY)) {
            int todoId = intent.getIntExtra(NotificationUtils.TODO_ID_KEY, DetailFragment.DEFAULT_TODO_ID);
            navigateToDetail(todoId);
        }
    }

    @Override
    public void onTodoSelected(int todoId) {
        navigateToDetail(todoId);
    }

    @Override
    public void onNewTodo() {
        navigateToDetail(DetailFragment.DEFAULT_TODO_ID);
    }

    @Override
    public void onDeleteAll() {
        if (mTwoPane) {
            Fragment detailFragment = getSupportFragmentManager().findFragmentByTag(DETAIL_FRAG_TAG);
            if (detailFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(detailFragment)
                        .commit();
            }
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

    private void navigateToDetail(int todoId) {
        if (mTwoPane) {
            DetailFragment detailFragment = DetailFragment.newInstance(todoId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, detailFragment, DETAIL_FRAG_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.TODO_ID_KEY, todoId);
            startActivity(intent);
        }
    }
}
