package com.example.tabnotes.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "templates")
public class TemplateEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public long createdAt;
}
