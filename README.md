# TabNotes

TabNotes is a lightweight Android app for tracking gym workouts as simple structured notes.
The goal is to replace messy text notes with a fast, minimal system for logging exercises, sets, and sessions.

The app focuses on **clarity and speed**, avoiding the complexity of traditional fitness apps.

---

# Core Idea

Many workout apps force users into rigid structures, statistics dashboards, or social features.
TabNotes takes the opposite approach.

It acts like a **structured notebook for workouts**, allowing users to freely record exercises and sets while keeping everything organized and searchable.

---

# Features

## Session Editor

Create and edit workout sessions quickly.

Each session can contain:

* multiple exercises
* multiple sets per exercise
* weight and repetition entries
* optional notes

Additional functionality:

* duplicate sets
* reorder exercises
* rename sessions
* delete sessions
* automatic saving using Room database

---

## Templates

Users can save workout sessions as reusable templates.

Templates allow:

* quickly starting a workout from a predefined routine
* reusing common exercise structures
* building personal training plans

Templates are managed inside the Archive system.

---

## Home

The Home tab shows **all sessions**, ordered by date.

Features:

* quick overview of all workout notes
* direct access to any session
* context menu (rename / delete)
* quick creation via "+" button

---

## Archive

The Archive tab organizes sessions in multiple ways.

### Templates

Saved workout templates.

### All Sessions

Full chronological list of all sessions.

### Year / Month Archive

Automatic folder structure:

```
Year
 └ Month
    └ Sessions
```

Sessions are automatically placed in the correct month and year.

### Calendar Archive

A monthly calendar view showing workout activity.

Features:

* sessions marked on calendar days
* navigation between months
* tap on a day to view that day's sessions
* rename and delete sessions directly from the day view

---

## Collections

Collections are user-created folders for grouping sessions manually.

Users can:

* create collections
* rename collections
* delete collections
* add sessions to collections
* remove sessions from collections

---

# Architecture

TabNotes uses a **Single Activity + Fragment architecture**.

```
MainActivity
 ├ HomeFragment
 ├ ArchiveRootFragment
 ├ CollectionsFragment
 └ SettingsFragment (planned)
```

The workout editor runs in a separate activity:

```
EditorActivity
```

---

# Database

TabNotes uses **Room** for local data persistence.

Entities:

* SessionEntity
* ExerciseEntity
* SetEntity
* FolderEntity
* CollectionEntity
* CollectionSessionCrossRef

Relationships:

```
Session
 └ Exercises
     └ Sets
```

Collections use a many-to-many relationship with sessions.

---

# Technology

* Java
* Android Studio
* Room Database
* RecyclerView
* Material Components

---

# Project Status

## Phase 1 – Core Editor

Implemented the workout editor, session management, templates, and PDF export.

## Phase 2 – App Architecture

Implemented full navigation structure:

* Home
* Archive
* Calendar history
* Templates
* Collections

---

# Roadmap

Next planned steps:

* UI overhaul
* Settings screen
* backup / export features
* theme switching

---

# License

This project is open source and free to use.
