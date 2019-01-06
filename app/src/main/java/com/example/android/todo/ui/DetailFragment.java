package com.example.android.todo.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.todo.R;
import com.example.android.todo.data.TodoRepository;
import com.example.android.todo.data.database.TodoEntry;
import com.example.android.todo.ui.dialogs.DeleteAlertDialogFragment;
import com.example.android.todo.ui.dialogs.TimePickerDialogFragment;
import com.example.android.todo.ui.dialogs.UnsavedAlertDialogFragment;
import com.example.android.todo.utilities.InjectorUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailFragment extends Fragment
        implements TimePickerDialogFragment.OnTimePicked,
        UnsavedAlertDialogFragment.UnsavedListener, DeleteAlertDialogFragment.DeleteListener {

    public static final String TODO_ID_KEY = "todo_id";
    public static final int DEFAULT_TODO_ID = -1;

    private static final int IMPORTANCE_HIGH = 1;
    private static final int IMPORTANCE_MEDIUM = 2;
    private static final int IMPORTANCE_LOW = 3;

    private static final String TIME_DIALOG_TAG = "time_tag";
    private static final String DELETE_DIALOG_TAG = "delete_tag";
    private static final String UNSAVED_DIALOG_TAG = "unsaved_tag";
    private static final String SAVED_TIME_KEY = "saved_time";

    private TextView mTitleTv;
    private CalendarView mCalendarView;
    private FloatingActionButton mFab;
    private TextView mDateTv;
    private EditText mDescriptionEdit;
    private RadioGroup mImportanceGroup;

    private TodoRepository mRepository;

    private Calendar mCalendar;

    private int mTodoId;

    private Date mDate = new Date();
    private String mDescription = "";
    private int mImportance = -1;

    public static DetailFragment newInstance(int todoId) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(TODO_ID_KEY, todoId);
        detailFragment.setArguments(args);
        return detailFragment;
    }

    public DetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleTv = rootView.findViewById(R.id.title_tv);
        mCalendarView = rootView.findViewById(R.id.calendar_view);
        mFab = rootView.findViewById(R.id.detail_fab);
        mDateTv = rootView.findViewById(R.id.date_tv);
        mDescriptionEdit = rootView.findViewById(R.id.todo_edit);
        mImportanceGroup = rootView.findViewById(R.id.importance_group);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRepository = InjectorUtils.provideRepository(getContext().getApplicationContext());

        mTodoId = getArguments() != null ? getArguments().getInt(TODO_ID_KEY, DEFAULT_TODO_ID) : DEFAULT_TODO_ID;

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        mCalendar = Calendar.getInstance();

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_TIME_KEY)) {
            long savedTime = savedInstanceState.getLong(SAVED_TIME_KEY);
            mCalendar.setTimeInMillis(savedTime);
            mCalendarView.setDate(savedTime);
            mDateTv.setText(getFormattedDate(new Date(mCalendar.getTimeInMillis())));
        }

        mCalendarView.setMinDate(System.currentTimeMillis() - 1000);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mDateTv.setText(getFormattedDate(new Date(mCalendar.getTimeInMillis())));

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag(TIME_DIALOG_TAG);
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                }
                DialogFragment timeFragment = new TimePickerDialogFragment();
                timeFragment.setTargetFragment(DetailFragment.this, 0);
                timeFragment.show(fragmentManager, TIME_DIALOG_TAG);
            }
        });

        mRepository.getItem(mTodoId).observe(this, new Observer<TodoEntry>() {
            @Override
            public void onChanged(@Nullable TodoEntry todoEntry) {
                if (todoEntry != null) {

                    mTitleTv.setText(R.string.update_todo_title);

                    // Set global variables for further checks.
                    mDate = todoEntry.getDate();
                    mDescription = todoEntry.getDescription();
                    mImportance = todoEntry.getImportance();

                    // If savedInstanceState is null, user hasn't written anything down.
                    if (savedInstanceState == null) {
                        mDescriptionEdit.setText(mDescription);
                        setImportance(mImportance);
                    } else if (!savedInstanceState.containsKey(SAVED_TIME_KEY)) {
                        // If there isn't saved time, fill out with saved in database.
                        mCalendar.setTime(mDate);
                        mCalendarView.setDate(mCalendar.getTimeInMillis());
                        mDateTv.setText(getFormattedDate(mDate));
                    }
                } else {
                    mTitleTv.setText(R.string.new_todo_title);
                }
            }
        });
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mDateTv.setText(getFormattedDate(new Date(mCalendar.getTimeInMillis())));
    }

    @Override
    public void unsavedPositiveClick() {
        finishFragment();
    }

    @Override
    public void deletePositiveClick() {
        deleteItem();
    }

    /**
     * Get user input and save TodoEntry into database.
     */
    private void saveItem() {
        // Read from input fields.
        final Date date = getDate();
        final String description = mDescriptionEdit.getText().toString().trim();
        final int importance = getImportance();

        if (date.getTime() < System.currentTimeMillis()) {
            Toast.makeText(getContext(), getString(R.string.date_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), getString(R.string.todo_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mTodoId == DEFAULT_TODO_ID) {

            TodoEntry todoEntry = new TodoEntry(date, description, importance);

            // Insert new TodoEntry into database.
            mRepository.insertItem(todoEntry);

            // If this fragment is visible to user - finish it.
            finishFragment();

            Toast.makeText(getContext(), getString(R.string.insert_successful), Toast.LENGTH_SHORT).show();
        } else {
            // Observe LiveData in order to get current TodoEntry.
            final LiveData<TodoEntry> todoEntryLiveData = mRepository.getItem(mTodoId);
            todoEntryLiveData.observe(this, new Observer<TodoEntry>() {
                @Override
                public void onChanged(@Nullable TodoEntry todoEntry) {
                    // Remove observer. There is no need to observe it anymore.
                    todoEntryLiveData.removeObserver(this);
                    if (todoEntry != null) {
                        todoEntry.setDate(date);
                        todoEntry.setDescription(description);
                        todoEntry.setImportance(importance);

                        // If TodoEntry isnt null update TodoEntry.
                        mRepository.updateItem(todoEntry);
                        Toast.makeText(getContext(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void deleteItem() {
        // Observe LiveData in order to get current TodoEntry.
        final LiveData<TodoEntry> todoEntryLiveData = mRepository.getItem(mTodoId);
        todoEntryLiveData.observe(this, new Observer<TodoEntry>() {
            @Override
            public void onChanged(@Nullable TodoEntry todoEntry) {
                // Remove observer. There is no need to observe it anymore.
                todoEntryLiveData.removeObserver(this);
                if (todoEntry != null) {
                    // If TodoEntry isnt null delete TodoEntry.
                    mRepository.deleteItem(todoEntry);
                    finishFragment();
                }
            }
        });
    }

    private void finishFragment() {
        if (getActivity() != null) {
            if (getResources().getBoolean(R.bool.two_pane)) {
                // If twoPane mode remove fragment.
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(DetailFragment.this)
                        .commit();
            } else {
                // Otherwise, finish activity.
                getActivity().finish();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);
        // If this is a new TodoEntry, hide the "Delete" menu item.
        if (mTodoId == DEFAULT_TODO_ID) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mDate.equals(getDate()) || !TextUtils.equals(mDescription, getDescription()) || mImportance != getImportance()) {
                    showUnsavedChangesDialog();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed() {
        if (!mDate.equals(getDate()) || !TextUtils.equals(mDescription, getDescription()) || mImportance != getImportance()) {
            showUnsavedChangesDialog();
            return true;
        }
        return false;
    }

    private void showUnsavedChangesDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(UNSAVED_DIALOG_TAG);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment timeFragment = new UnsavedAlertDialogFragment();
        timeFragment.setTargetFragment(DetailFragment.this, 0);
        timeFragment.show(fragmentManager, UNSAVED_DIALOG_TAG);
    }

    private void showDeleteConfirmationDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DELETE_DIALOG_TAG);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment timeFragment = new DeleteAlertDialogFragment();
        timeFragment.setTargetFragment(DetailFragment.this, 0);
        timeFragment.show(fragmentManager, DELETE_DIALOG_TAG);
    }

    private String getFormattedDate(Date date) {
        return DateFormat.getDateTimeInstance().format(date);
    }

    private Date getDate() {
        long time = mCalendar.getTimeInMillis();
        return new Date(time);
    }

    private String getDescription() {
        return mDescriptionEdit.getText().toString().trim();
    }

    private int getImportance() {
        int importance;
        int resId = mImportanceGroup.getCheckedRadioButtonId();
        switch (resId) {
            case R.id.importance_high:
                importance = IMPORTANCE_HIGH;
                break;
            case R.id.importance_medium:
                importance = IMPORTANCE_MEDIUM;
                break;
            case R.id.importance_low:
                importance = IMPORTANCE_LOW;
                break;
            default:
                importance = IMPORTANCE_MEDIUM;
                break;
        }
        return importance;
    }

    private void setImportance(int importance) {
        int resId;
        switch (importance) {
            case IMPORTANCE_HIGH:
                resId = R.id.importance_high;
                break;
            case IMPORTANCE_MEDIUM:
                resId = R.id.importance_medium;
                break;
            case IMPORTANCE_LOW:
                resId = R.id.importance_low;
                break;
            default:
                resId = R.id.importance_medium;
                break;
        }
        mImportanceGroup.check(resId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SAVED_TIME_KEY, mCalendar.getTimeInMillis());
    }
}
