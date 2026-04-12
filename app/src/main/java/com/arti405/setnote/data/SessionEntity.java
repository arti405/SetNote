package com.arti405.setnote.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sessions",
        foreignKeys = @ForeignKey(
                entity = FolderEntity.class,
                parentColumns = "id",
                childColumns = "folderId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("folderId")
)
public class SessionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public long dateEpochMillis;
    // add:
    public Long folderId;


}
