package com.example.setnote.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "template_sets",
        foreignKeys = @ForeignKey(
                entity = TemplateExerciseEntity.class,
                parentColumns = "id",
                childColumns = "templateExerciseId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("templateExerciseId")
)
public class TemplateSetEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long templateExerciseId;
    public String weight;
    public String reps;
    public String note;
    public int position;
}
