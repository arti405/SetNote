package com.example.tabnotes.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class SessionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public long dateEpochMillis;
}
