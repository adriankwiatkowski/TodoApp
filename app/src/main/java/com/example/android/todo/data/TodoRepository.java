package com.example.android.todo.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.todo.data.database.TodoDatabase;
import com.example.android.todo.data.database.TodoEntry;
import com.example.android.todo.utilities.NotificationUtils;

import java.util.Date;
import java.util.List;

public class TodoRepository {

    private static final String LOG_TAG = TodoRepository.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static TodoRepository sInstance;

    private TodoDatabase mDb;

    private Context mContext;

    private TodoRepository(Context context, TodoDatabase todoDatabase) {
        mContext = context.getApplicationContext();
        mDb = todoDatabase;
    }

    public synchronized static TodoRepository getInstance(Context context, TodoDatabase todoDatabase) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new TodoRepository(context.getApplicationContext(), todoDatabase);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    @SuppressLint("StaticFieldLeak")
    public void insertItem(final TodoEntry todoEntry) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                long id = mDb.todoDao().insertItem(todoEntry);
                final LiveData<TodoEntry> todoEntryLiveData = getItem(id);
                todoEntryLiveData.observeForever(new Observer<TodoEntry>() {
                    @Override
                    public void onChanged(@Nullable TodoEntry todoEntry) {
                        if (todoEntry != null) {
                            todoEntryLiveData.removeObserver(this);
                            NotificationUtils.setAlarm(mContext, todoEntry);
                        }
                    }
                });
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateItem(final TodoEntry todoEntry) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mDb.todoDao().updateItem(todoEntry);
                NotificationUtils.setAlarm(mContext, todoEntry);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteItem(final TodoEntry todoEntry) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mDb.todoDao().deleteItem(todoEntry);
                cancelNotification(todoEntry);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteAllItems(final List<TodoEntry> todoEntries) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cancelAllNotifications(todoEntries);
                mDb.todoDao().deleteAllItems();
                return null;
            }
        }.execute();
    }

    private void cancelNotification(TodoEntry todoEntry) {
        Date todoDate = todoEntry.getDate();
        if (todoDate.getTime() < System.currentTimeMillis()) {
            NotificationUtils.cancelTodoNotification(mContext, todoEntry);
        } else {
            NotificationUtils.cancelAlarm(mContext, todoEntry);
        }
    }

    private void cancelAllNotifications(List<TodoEntry> todoEntries) {
        for (TodoEntry todoEntry : todoEntries) {
            cancelNotification(todoEntry);
        }
    }

    public LiveData<TodoEntry> getItem(long todoId) {
        return mDb.todoDao().getItem(todoId);
    }

    public LiveData<List<TodoEntry>> getItems() {
        return mDb.todoDao().getAllItems();
    }
}
