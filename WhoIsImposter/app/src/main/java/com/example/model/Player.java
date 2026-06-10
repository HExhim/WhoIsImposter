package com.example.model;

import android.graphics.Bitmap;

public class Player {
    private final int id;
    private String name;
    private String role; // "Innocent" or "Imposter"
    private String secretWord;
    private boolean isVotedOut;
    private Bitmap drawing;
    private int voteCount;
    private String pin;
    private String votedForName;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.role = "Innocent";
        this.secretWord = "";
        this.isVotedOut = false;
        this.drawing = null;
        this.voteCount = 0;
        this.pin = "";
        this.votedForName = null;
    }

    public String getVotedForName() {
        return votedForName;
    }

    public void setVotedForName(String votedForName) {
        this.votedForName = votedForName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public boolean isVotedOut() {
        return isVotedOut;
    }

    public void setVotedOut(boolean votedOut) {
        this.isVotedOut = votedOut;
    }

    public Bitmap getDrawing() {
        return drawing;
    }

    public void setDrawing(Bitmap drawing) {
        this.drawing = drawing;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void incrementVote() {
        this.voteCount++;
    }

    public void resetVotes() {
        this.voteCount = 0;
    }
}
