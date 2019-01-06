package com.example.android.todo.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "todo")
public class TodoEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date date;
    private String description;
    private int importance;

    @Ignore
    public TodoEntry(Date date, String description, int importance) {
        this.date = date;
        this.description = description;
        this.importance = importance;
    }

    public TodoEntry(int id, Date date, String description, int importance) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.importance = importance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }
}
