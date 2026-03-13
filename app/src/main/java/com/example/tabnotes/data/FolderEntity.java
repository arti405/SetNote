package com.example.tabnotes.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "folders",
        indices = {
                @Index(value = {"type", "year"}, unique = false),
                @Index(value = {"type", "year", "month"}, unique = false),
                @Index(value = {"parentId"})
        }
)
public class FolderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;      // "2026", "March", "Templates"
    public String type;      // "YEAR", "MONTH", "TEMPLATES"
    public Long parentId;    // null for YEAR
    public Integer year;     // YEAR/MONTH
    public Integer month;    // MONTH 1..12
    public long createdAt;
}
