package com.example.tabnotes.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "exercises",
        foreignKeys = @ForeignKey(
                entity = SessionEntity.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("sessionId")}
)
public class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long sessionId;
    public String name;
    public int position; // Reihenfolge im Editor
}

