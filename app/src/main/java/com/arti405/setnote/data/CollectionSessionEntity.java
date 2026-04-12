package com.arti405.setnote.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "collection_sessions",
        primaryKeys = {"collectionId", "sessionId"},
        foreignKeys = {
                @ForeignKey(entity = CollectionEntity.class, parentColumns = "id", childColumns = "collectionId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SessionEntity.class, parentColumns = "id", childColumns = "sessionId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("collectionId"), @Index("sessionId")}
)
public class CollectionSessionEntity {
    public long collectionId;
    public long sessionId;
    public int position;
}
