package com.example.tabnotes.data;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface GymDao {

    // Sessions
    @Insert long insertSession(SessionEntity s);

    @Update void updateSession(SessionEntity s);

    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC")
    LiveData<List<SessionEntity>> observeSessions();

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    SessionEntity getSessionById(long id);

    // Exercises
    @Query("SELECT * FROM exercises WHERE sessionId = :sessionId ORDER BY position ASC")
    List<ExerciseEntity> getExercisesForSession(long sessionId);

    @Insert long insertExercise(ExerciseEntity e);

    @Query("DELETE FROM exercises WHERE sessionId = :sessionId")
    void deleteExercisesForSession(long sessionId);

    // Sets
    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY position ASC")
    List<SetEntity> getSetsForExercise(long exerciseId);

    @Insert void insertSets(List<SetEntity> sets);

    @Query("DELETE FROM sets WHERE exerciseId IN (SELECT id FROM exercises WHERE sessionId = :sessionId)")
    void deleteSetsForSession(long sessionId);
}

