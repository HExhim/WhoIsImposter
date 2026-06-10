package com.example.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_records")
public class GameRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String mode;
    private String winner;
    private int playerCount;
    private long date;
    private String players;
    private String category;
    private String imposterNames;
    private String votedOutName;

    public GameRecord(String mode, String winner, int playerCount, long date, String players, String category, String imposterNames, String votedOutName) {
        this.mode = mode;
        this.winner = winner;
        this.playerCount = playerCount;
        this.date = date;
        this.players = players;
        this.category = category;
        this.imposterNames = imposterNames;
        this.votedOutName = votedOutName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImposterNames() {
        return imposterNames;
    }

    public void setImposterNames(String imposterNames) {
        this.imposterNames = imposterNames;
    }

    public String getVotedOutName() {
        return votedOutName;
    }

    public void setVotedOutName(String votedOutName) {
        this.votedOutName = votedOutName;
    }
}
