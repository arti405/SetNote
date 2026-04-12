package com.arti405.setnote.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "collections")
public class CollectionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public long createdAt;
}
