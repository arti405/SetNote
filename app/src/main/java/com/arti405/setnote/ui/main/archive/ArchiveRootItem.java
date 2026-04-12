package com.arti405.setnote.ui.main.archive;

public class ArchiveRootItem {
    public final String title;
    public final String subtitle;
    public final int type;

    public static final int TYPE_TEMPLATES = 1;
    public static final int TYPE_ALL_SESSIONS = 2;
    public static final int TYPE_YEARS = 3;

    public static final int TYPE_CALENDAR = 4;

    public ArchiveRootItem(String title, String subtitle, int type) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
    }
}
