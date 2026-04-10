package com.example.setnote.data;
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

    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC LIMIT :limit")
    LiveData<List<SessionEntity>> observeRecentSessions(int limit);

    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC")
    LiveData<List<SessionEntity>> observeAllSessions();
    // Rename and Delete
    @Query("UPDATE sessions SET title=:newTitle WHERE id=:sessionId")
    void renameSession(long sessionId, String newTitle);

    @Query("DELETE FROM sessions WHERE id=:sessionId")
    void deleteSession(long sessionId);

    // Templates list
    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    LiveData<List<TemplateEntity>> observeTemplates();

    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    List<TemplateEntity> getTemplates();

    // Insert template
    @Insert
    long insertTemplate(TemplateEntity t);

    @Insert
    long insertTemplateExercise(TemplateExerciseEntity e);

    @Insert
    void insertTemplateSets(List<TemplateSetEntity> sets);

    // Read template content
    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY position ASC")
    List<TemplateExerciseEntity> getTemplateExercises(long templateId);

    @Query("SELECT * FROM template_sets WHERE templateExerciseId = :exId ORDER BY position ASC")
    List<TemplateSetEntity> getTemplateSets(long exId);

    // Delete template (cascade wipes children)
    @Query("UPDATE templates SET name=:newName WHERE id=:templateId")
    void renameTemplate(long templateId, String newName);
    @Query("DELETE FROM templates WHERE id = :templateId")
    void deleteTemplate(long templateId);
    @Query("SELECT * FROM folders WHERE type='YEAR' AND year=:year LIMIT 1")
    FolderEntity getYearFolder(int year);

    @Query("SELECT * FROM folders WHERE type='MONTH' AND year=:year AND month=:month LIMIT 1")
    FolderEntity getMonthFolder(int year, int month);

    @Query("SELECT dateEpochMillis FROM sessions WHERE dateEpochMillis BETWEEN :startMillis AND :endMillis")
    List<Long> getSessionDatesInRange(long startMillis, long endMillis);

    @Query("SELECT * FROM sessions WHERE dateEpochMillis BETWEEN :startMillis AND :endMillis ORDER BY dateEpochMillis DESC")
    LiveData<List<SessionEntity>> observeSessionsForDay(long startMillis, long endMillis);
    @Insert
    long insertFolder(FolderEntity f);

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY year DESC")
    LiveData<List<FolderEntity>> observeYearFolders();

    @Query("SELECT * FROM folders WHERE parentId=:yearFolderId ORDER BY month DESC")
    LiveData<List<FolderEntity>> observeMonthFolders(long yearFolderId);

    @Query("SELECT * FROM sessions WHERE folderId=:monthFolderId ORDER BY dateEpochMillis DESC")
    LiveData<List<SessionEntity>> observeSessionsForMonth(long monthFolderId);

    // Collections Create DAO
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    LiveData<List<CollectionEntity>> observeCollections();

    @Insert long insertCollection(CollectionEntity c);

    @Query("UPDATE collections SET name=:name WHERE id=:id")
    void renameCollection(long id, String name);

    @Query("DELETE FROM collections WHERE id=:id")
    void deleteCollection(long id);

    // Add/Remove session in collection
    @Insert void insertCollectionSession(CollectionSessionEntity link);

    @Query("DELETE FROM collection_sessions WHERE collectionId=:collectionId AND sessionId=:sessionId")
    void removeSessionFromCollection(long collectionId, long sessionId);

    // List sessions inside a collection (ordered)
    @Query(
            "SELECT s.* FROM sessions s " +
                    "JOIN collection_sessions cs ON cs.sessionId = s.id " +
                    "WHERE cs.collectionId = :collectionId " +
                    "ORDER BY cs.position ASC"
    )
    LiveData<List<SessionEntity>> observeSessionsInCollection(long collectionId);

    @Query("DELETE FROM sessions WHERE id IN (:sessionIds)")
    void deleteSessionsByIds(List<Long> sessionIds);

    // Next position helper
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM collection_sessions WHERE collectionId=:collectionId")
    int nextCollectionPosition(long collectionId);

    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC")
    List<SessionEntity> getAllSessions();

    @Query("DELETE FROM collection_sessions")
    void deleteAllCollectionSessions();

    @Query("DELETE FROM collections")
    void deleteAllCollections();

    @Query("DELETE FROM template_sets")
    void deleteAllTemplateSets();

    @Query("DELETE FROM template_exercises")
    void deleteAllTemplateExercises();

    @Query("DELETE FROM templates")
    void deleteAllTemplates();

    @Query("DELETE FROM sets")
    void deleteAllSets();

    @Query("DELETE FROM exercises")
    void deleteAllExercises();

    @Query("DELETE FROM sessions")
    void deleteAllSessions();

    @Query("DELETE FROM folders")
    void deleteAllFolders();

    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC")
    java.util.List<com.example.setnote.data.SessionEntity> getAllSessionsNow();

    @Query("SELECT * FROM exercises ORDER BY position ASC")
    java.util.List<com.example.setnote.data.ExerciseEntity> getAllExercisesNow();

    @Query("SELECT * FROM sets ORDER BY position ASC")
    java.util.List<com.example.setnote.data.SetEntity> getAllSetsNow();

    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    java.util.List<com.example.setnote.data.TemplateEntity> getAllTemplatesNow();

    @Query("SELECT * FROM template_exercises ORDER BY position ASC")
    java.util.List<com.example.setnote.data.TemplateExerciseEntity> getAllTemplateExercisesNow();

    @Query("SELECT * FROM template_sets ORDER BY position ASC")
    java.util.List<com.example.setnote.data.TemplateSetEntity> getAllTemplateSetsNow();

    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    java.util.List<com.example.setnote.data.FolderEntity> getAllFoldersNow();

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    java.util.List<com.example.setnote.data.CollectionEntity> getAllCollectionsNow();

    @Query("SELECT * FROM collection_sessions ORDER BY position ASC")
    java.util.List<com.example.setnote.data.CollectionSessionEntity> getAllCollectionSessionsNow();


}

