package com.example.android.todo.utilities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.android.todo.R;
import com.example.android.todo.data.TodoRepository;
import com.example.android.todo.data.database.TodoEntry;
import com.example.android.todo.receivers.AlarmReceiver;
import com.example.android.todo.ui.DetailFragment;
import com.example.android.todo.ui.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    private static final String TODO_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    public static final String TODO_ID_KEY = "todo_id";
    private static final String TODO_DESCRIPTION_KEY = "todo_description";

    private static final long INTERVAL_ALARM_TIME = TimeUnit.SECONDS.toMillis(30);

    public static void setTodoNotification(Context context, Intent alarmIntent) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TODO_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.todo_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        PendingIntent pendingIntent = contentIntent(context, alarmIntent);

        String notificationTitle = context.getString(R.string.app_name);

        String notificationText = alarmIntent.getStringExtra(TODO_DESCRIPTION_KEY);

        int notificationId = alarmIntent.getIntExtra(TODO_ID_KEY, DetailFragment.DEFAULT_TODO_ID);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, TODO_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentIntent(pendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public static void cancelTodoNotification(Context context, TodoEntry todoEntry) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(todoEntry.getId());
    }

    private static PendingIntent contentIntent(Context context, Intent alarmIntent) {

        Intent startActivityIntent = new Intent(context, MainActivity.class);

        int todoId = alarmIntent.getIntExtra(TODO_ID_KEY, DetailFragment.DEFAULT_TODO_ID);

        startActivityIntent.putExtra(TODO_ID_KEY, todoId);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(startActivityIntent);

        return taskStackBuilder.getPendingIntent(
                todoId,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent alarmIntent(Context context, int todoId, String todoDescription) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(TODO_ID_KEY, todoId);
        intent.putExtra(TODO_DESCRIPTION_KEY, todoDescription);
        return PendingIntent.getBroadcast(
                context,
                todoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void setAlarms(final Context context) {

        TodoRepository repository = InjectorUtils.provideRepository(context);

        final LiveData<List<TodoEntry>> todoListLiveData = repository.getItems();

        todoListLiveData.observeForever(new Observer<List<TodoEntry>>() {
            @Override
            public void onChanged(@Nullable List<TodoEntry> todoEntries) {

                todoListLiveData.removeObserver(this);

                if (todoEntries != null) {
                    for (TodoEntry todoEntry : todoEntries) {
                        setAlarm(context, todoEntry);
                    }
                }
            }
        });
    }

    public static void setAlarm(Context context, TodoEntry todoEntry) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int todoId = todoEntry.getId();
        String todoDescription = todoEntry.getDescription();
        long alarmTime = todoEntry.getDate().getTime();
        PendingIntent alarmPendingIntent = alarmIntent(context, todoId, todoDescription);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    alarmTime + INTERVAL_ALARM_TIME,
                    alarmPendingIntent);
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    alarmPendingIntent);
        }
    }

    public static void cancelAlarm(Context context, TodoEntry todoEntry) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int todoId = todoEntry.getId();
        String todoDescription = todoEntry.getDescription();
        PendingIntent alarmPendingIntent = alarmIntent(context, todoId, todoDescription);

        alarmManager.cancel(alarmPendingIntent);
    }
}
