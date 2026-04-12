package com.arti405.setnote.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DbExecutors {
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();
}

