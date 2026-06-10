package com.example.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameRepository {
    private final GameRecordDao dao;
    private final LiveData<List<GameRecord>> allRecords;
    private final ExecutorService executor;

    public GameRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        dao = db.gameRecordDao();
        allRecords = dao.getAllRecords();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<GameRecord>> getAllRecords() {
        return allRecords;
    }

    public void insert(final GameRecord record) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                dao.insertRecord(record);
            }
        });
    }

    public void clear() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                dao.clearAll();
            }
        });
    }
}
