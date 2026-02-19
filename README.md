# TabNotes

Minimalistische Android App zum strukturierten Loggen von Trainingssessions.

TabNotes kombiniert die Einfachheit einer Notiz-App mit einer klaren Tabellenstruktur für Übungen und Sätze.
Kein Account. Keine Cloud. Keine Werbung. 100% offline.

---

## Features

* Sessions erstellen und umbenennen
* Mehrere Übungen pro Session
* Mehrere Sätze pro Übung (Gewicht, Wiederholungen, Notiz)
* Persistente Speicherung mit Room (SQLite)
* Automatische Aktualisierung der Session-Liste (LiveData)
* PDF Export mit Share-Funktion
* Unterstützt Android Back-Gesture Handling

---

## Architektur

### UI Layer

* `SessionListActivity`
* `EditorActivity`
* RecyclerView + Nested RecyclerView (Exercises → Sets)

### Data Layer (Room)

* `SessionEntity`
* `ExerciseEntity`
* `SetEntity`
* `GymDao`
* `AppDatabase`

### Infrastructure

* `DbExecutors` (IO Thread Handling)
* `PdfExporter`
* `FileProvider` (PDF Sharing)

### Datenfluss

SessionList beobachtet `Room` via `LiveData`.
Editor lädt und speichert über `sessionId`.
PDF Export rendert das aktuelle UI-Modell in ein `PdfDocument`.

---

## Technische Details

* Sprache: Java
* Minimum SDK: 24
* Architektur: Layered (UI / Data / Infra)
* Persistenz: Room ORM
* PDF Rendering: `android.graphics.pdf.PdfDocument`
* Threading: Single-threaded Executor für DB I/O

---

## Projektstatus

Phase 1 abgeschlossen:

* Persistente Sessions
* Nested Exercise-Struktur
* Rename persistent
* PDF Export
* Saubere Trennung von UI und Datenmodell

---

## Geplante Erweiterungen

* Trainingsstatistik (Volumen, 1RM-Schätzung)
* Verlaufsanalyse pro Übung
* Reorder von Übungen/Sätzen
* UX-Verbesserungen
* Optional: MVVM Refactor

---

## Motivation

Die App entstand aus dem Bedarf nach einer schnellen, tabellarischen Trainingsnotiz ohne unnötige Features.
Ziel ist ein simples, leistungsfähiges Tool für strukturiertes Krafttraining.

---

## Lizenz

TBD

