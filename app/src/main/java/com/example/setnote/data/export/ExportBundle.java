package com.example.setnote.data.export;

import com.example.setnote.data.CollectionEntity;
import com.example.setnote.data.CollectionSessionEntity;
import com.example.setnote.data.ExerciseEntity;
import com.example.setnote.data.FolderEntity;
import com.example.setnote.data.SessionEntity;
import com.example.setnote.data.SetEntity;
import com.example.setnote.data.TemplateEntity;
import com.example.setnote.data.TemplateExerciseEntity;
import com.example.setnote.data.TemplateSetEntity;

import java.util.List;

public class ExportBundle {
    public long exportedAt;

    public List<SessionEntity> sessions;
    public List<ExerciseEntity> exercises;
    public List<SetEntity> sets;

    public List<TemplateEntity> templates;
    public List<TemplateExerciseEntity> templateExercises;
    public List<TemplateSetEntity> templateSets;

    public List<FolderEntity> folders;

    public List<CollectionEntity> collections;
    public List<CollectionSessionEntity> collectionSessions;
}
