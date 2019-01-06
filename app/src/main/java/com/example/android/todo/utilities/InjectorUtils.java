package com.example.android.todo.utilities;

import android.content.Context;

import com.example.android.todo.data.TodoRepository;
import com.example.android.todo.data.database.TodoDatabase;

public class InjectorUtils {

    public static TodoRepository provideRepository(Context context) {
        TodoDatabase database = TodoDatabase.getInstance(context.getApplicationContext());
        return TodoRepository.getInstance(context.getApplicationContext(), database);
    }
}
