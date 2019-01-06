package com.example.android.todo.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.todo.R;
import com.example.android.todo.adapters.TodoAdapter;
import com.example.android.todo.data.TodoRepository;
import com.example.android.todo.data.database.TodoEntry;
import com.example.android.todo.ui.dialogs.DeleteAlertDialogFragment;
import com.example.android.todo.utilities.InjectorUtils;

import java.util.List;

public class ListFragment extends Fragment
        implements TodoAdapter.OnTodoClick, DeleteAlertDialogFragment.DeleteListener {

    private static final String DELETE_DIALOG_TAG = "delete_all_dialog";

    public interface OnTodoSelected {
        void onTodoSelected(int todoId);
        void onNewTodo();
        void onDeleteAll();
    }

    private OnTodoSelected mCallback;

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;

    private TodoAdapter mAdapter;

    private TodoRepository mRepository;

    private boolean mIsEmpty = true;

    public ListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mFab = rootView.findViewById(R.id.fab);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRepository = InjectorUtils.provideRepository(getContext().getApplicationContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new TodoAdapter(getContext(), this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onNewTodo();
            }
        });

        mRepository.getItems().observe(this, new Observer<List<TodoEntry>>() {
            @Override
            public void onChanged(@Nullable List<TodoEntry> todoEntries) {
                mAdapter.setData(todoEntries);
                mIsEmpty = todoEntries == null || todoEntries.isEmpty();
                getActivity().invalidateOptionsMenu();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                TodoEntry todoEntry = mAdapter.getTodoList().get(viewHolder.getAdapterPosition());
                mRepository.deleteItem(todoEntry);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onTodoClick(int todoId) {
        mCallback.onTodoSelected(todoId);
    }

    @Override
    public void deletePositiveClick() {
        final LiveData<List<TodoEntry>> todoEntriesLive = mRepository.getItems();
        todoEntriesLive.observe(this, new Observer<List<TodoEntry>>() {
            @Override
            public void onChanged(@Nullable List<TodoEntry> todoEntries) {
                todoEntriesLive.removeObserver(this);
                if (todoEntries != null) {
                    mRepository.deleteAllItems(todoEntries);
                    mIsEmpty = true;
                    getActivity().invalidateOptionsMenu();
                    mCallback.onDeleteAll();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
        MenuItem menuItem = menu.findItem(R.id.action_delete_all);
        if (mIsEmpty) {
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DELETE_DIALOG_TAG);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment timeFragment = new DeleteAlertDialogFragment();
        timeFragment.setTargetFragment(ListFragment.this, 0);
        timeFragment.show(fragmentManager, DELETE_DIALOG_TAG);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnTodoSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTodoSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}
