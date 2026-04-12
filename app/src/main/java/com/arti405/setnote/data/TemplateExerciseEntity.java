package com.arti405.setnote.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Entity(
        tableName = "template_exercises",
        foreignKeys = @ForeignKey(
                entity = TemplateEntity.class,
                parentColumns = "id",
                childColumns = "templateId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("templateId")
)
public class TemplateExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long templateId;
    public String name;
    public int position;
}
