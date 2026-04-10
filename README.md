# SetNote

SetNote is a minimalist Android application for tracking workout sessions.
It focuses on clarity, speed, and structured data entry without unnecessary complexity.

---

## Concept

Many workout tracking apps are overloaded with features and difficult to use.
SetNote intentionally reduces functionality to the essentials:

* Quickly create and edit workout sessions
* Structure exercises and sets clearly
* Easily revisit past training data

The app uses a central hub screen instead of traditional tab-based navigation.

---

## Features

### Sessions

* Create new workout sessions
* Edit title and date
* Add, modify, and delete exercises and sets
* Automatic saving

### Editor

* Dynamic management of exercises and sets
* Drag-and-drop reordering
* Multiple sets per exercise
* Structured input interface

### Templates

* Save sessions as reusable templates
* Quickly create new sessions based on existing structures

### Archive

* Overview of all sessions
* Organized by year, month, and day
* Calendar view with highlighted training days

### Multi-Select

* Select multiple sessions
* Batch deletion

### Export

* Export sessions as PDF

---

## Architecture

The app follows a standard Android architecture:

* Single-activity structure
* Navigation using fragments
* RecyclerView for dynamic lists
* Room for local data persistence

### Key Components

* `EditorActivity` – handles session editing
* `SessionsFragment` – displays all sessions
* `ExerciseAdapter` – manages exercises
* `SetAdapter` – manages sets
* `AppDatabase` / DAO – data layer

---

## Data Model

The app uses a relational structure:

* Session → contains multiple exercises
* Exercise → contains multiple sets
* Template → similar to session, used as a reusable blueprint

All data is stored locally using Room (SQLite).

---

## Tech Stack

* Java
* Android SDK
* Room Database
* RecyclerView

---

## Current Status

* Core functionality implemented
* Stable data flow and database integration
* Hub-based navigation structure completed
* Multi-select and template system implemented

Pending improvements:

* UI refinement
* Extended settings
* Optional cloud synchronization

---

## Goal

SetNote aims to provide a fast, simple, and structured way to track workouts without unnecessary features or complexity.

---

# License

This project is open source and free to use.
