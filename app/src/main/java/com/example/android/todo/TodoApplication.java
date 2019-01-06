package com.example.android.todo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class TodoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
