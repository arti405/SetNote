package com.example.tabnotes.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sets",
        foreignKeys = @ForeignKey(
                entity = ExerciseEntity.class,
                parentColumns = "id",
                childColumns = "exerciseId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("exerciseId")}
)
public class SetEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long exerciseId;
    public String weight;
    public String reps;
    public String note;   // optional, falls du später willst
    public int position;  // Reihenfolge im Block
}

