package com.arti405.setnote.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        SessionEntity.class, ExerciseEntity.class, SetEntity.class,
        TemplateEntity.class, TemplateExerciseEntity.class, TemplateSetEntity.class, FolderEntity.class, CollectionEntity.class, CollectionSessionEntity.class
},
        version = 4,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GymDao gymDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "setnote_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
