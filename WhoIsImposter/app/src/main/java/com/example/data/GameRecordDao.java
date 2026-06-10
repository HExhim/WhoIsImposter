package com.example.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameRecordDao {
    @Insert
    void insertRecord(GameRecord record);

    @Query("SELECT * FROM game_records ORDER BY date DESC")
    LiveData<List<GameRecord>> getAllRecords();

    @Query("DELETE FROM game_records")
    void clearAll();
}
