package com.example.android.todo.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TodoDao {

    @Query("SELECT * FROM todo ORDER BY date ASC, importance DESC")
    LiveData<List<TodoEntry>> getAllItems();

    @Query("SELECT * FROM todo WHERE id = :todoId")
    LiveData<TodoEntry> getItem(long todoId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertItem(TodoEntry todoEntry);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateItem(TodoEntry todoEntry);

    @Delete
    void deleteItem(TodoEntry todoEntry);

    @Query("DELETE FROM todo")
    void deleteAllItems();
}
