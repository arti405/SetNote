package com.arti405.setnote.data.export;

import com.arti405.setnote.data.CollectionEntity;
import com.arti405.setnote.data.CollectionSessionEntity;
import com.arti405.setnote.data.ExerciseEntity;
import com.arti405.setnote.data.FolderEntity;
import com.arti405.setnote.data.SessionEntity;
import com.arti405.setnote.data.SetEntity;
import com.arti405.setnote.data.TemplateEntity;
import com.arti405.setnote.data.TemplateExerciseEntity;
import com.arti405.setnote.data.TemplateSetEntity;

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
