package com.example.tabnotes.ui.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.AppDatabase;
import com.example.tabnotes.data.DbExecutors;
import com.example.tabnotes.data.SessionEntity;

import java.util.ArrayList;

public class SessionListActivity extends AppCompatActivity {

    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SessionEntityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        RecyclerView rv = findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getInstance(this);

        adapter = new SessionEntityAdapter(sessions, position -> {
            Intent i = new Intent(this, EditorActivity.class);
            i.putExtra("sessionId", sessions.get(position).id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        db.gymDao().observeSessions().observe(this, list -> {
            sessions.clear();
            sessions.addAll(list);
            adapter.notifyDataSetChanged();
        });

        Button btnNew = findViewById(R.id.btnNewSession);
        btnNew.setOnClickListener(v -> {
            DbExecutors.IO.execute(() -> {
                SessionEntity s = new SessionEntity();
                s.title = "New Session";
                s.dateEpochMillis = System.currentTimeMillis();

                long id = db.gymDao().insertSession(s);

                runOnUiThread(() -> {
                    Intent i = new Intent(this, EditorActivity.class);
                    i.putExtra("sessionId", id);
                    startActivity(i);
                });
            });
        });
    }
}


