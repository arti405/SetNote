package com.example.tabnotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SessionEntity.class, ExerciseEntity.class, SetEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GymDao gymDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "tabnotes_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
