package com.example.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.GameRecord;
import com.example.data.GameRepository;
import com.example.model.CategoriesData;
import com.example.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameViewModel extends AndroidViewModel {

    // States of game flow
    public enum GameState {
        SETUP,
        ROLL_REVEAL,
        DISCUSSION,
        DRAWING_PHASE,
        GALLERY,
        VOTING,
        REVEAL_RESULT
    }

    private final GameRepository repository;
    private final MutableLiveData<GameState> currentGameState = new MutableLiveData<>(GameState.SETUP);
    private final MutableLiveData<List<Player>> players = new MutableLiveData<>(new ArrayList<Player>());
    private final MutableLiveData<Integer> activePlayerIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> timerSecondsRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> winnerMessage = new MutableLiveData<>("");
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("Places");
    private final MutableLiveData<String> curDifficulty = new MutableLiveData<>("Medium");
    private final MutableLiveData<String> curGameMode = new MutableLiveData<>("Classic Imposter");
    private final MutableLiveData<Integer> selectedImposterCount = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> selectedTimerDuration = new MutableLiveData<>(60);

    private final MutableLiveData<List<String>> savedPresets = new MutableLiveData<>(new ArrayList<>());

    // Temp setup names
    private final List<String> setupPlayerNames = new ArrayList<>();

    // CountDownTimer instance
    private CountDownTimer gameCountdownTimer;

    // Active words
    private String innocentWord = "";
    private String imposterWord = "";

    // Shared canvas strokes state
    private final ArrayList<com.example.util.DrawingView.Stroke> sharedCanvasStrokes = new ArrayList<>();

    public ArrayList<com.example.util.DrawingView.Stroke> getSharedCanvasStrokes() {
        return sharedCanvasStrokes;
    }

    public void setSharedCanvasStrokes(ArrayList<com.example.util.DrawingView.Stroke> strokes) {
        this.sharedCanvasStrokes.clear();
        if (strokes != null) {
            this.sharedCanvasStrokes.addAll(strokes);
        }
    }

    public GameViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);

        // Load previously saved player presets from SharedPreferences
        loadSavedPresets();

        // Start empty as requested: "no dummy/intitial names should be available"
        updatePlayersListFromNames();
    }

    private void loadSavedPresets() {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        java.util.Set<String> saved = prefs.getStringSet("saved_player_names", new java.util.HashSet<>());
        savedPresets.setValue(new java.util.ArrayList<>(saved));
    }

    public LiveData<List<String>> getSavedPresets() {
        return savedPresets;
    }

    public void savePlayerToPresets(String name) {
        if (name == null || name.trim().isEmpty()) return;
        String trimmed = name.trim();
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        java.util.Set<String> saved = prefs.getStringSet("saved_player_names", new java.util.HashSet<>());
        java.util.Set<String> updated = new java.util.HashSet<>(saved);
        if (updated.add(trimmed)) {
            prefs.edit().putStringSet("saved_player_names", updated).apply();
            savedPresets.setValue(new java.util.ArrayList<>(updated));
        }
    }

    public void deleteSavedPreset(String name) {
        if (name == null || name.trim().isEmpty()) return;
        String trimmed = name.trim();
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        java.util.Set<String> saved = prefs.getStringSet("saved_player_names", new java.util.HashSet<>());
        java.util.Set<String> updated = new java.util.HashSet<>(saved);
        if (updated.remove(trimmed)) {
            prefs.edit().putStringSet("saved_player_names", updated).apply();
            prefs.edit().remove("player_pin_" + trimmed.toLowerCase()).apply();
            savedPresets.setValue(new java.util.ArrayList<>(updated));
        }
    }

    public LiveData<GameState> getCurrentGameState() { return currentGameState; }
    public LiveData<List<Player>> getPlayers() { return players; }
    public LiveData<Integer> getActivePlayerIndex() { return activePlayerIndex; }
    public LiveData<Integer> getTimerSecondsRemaining() { return timerSecondsRemaining; }
    public LiveData<Boolean> getIsTimerRunning() { return isTimerRunning; }
    public LiveData<String> getWinnerMessage() { return winnerMessage; }
    public MutableLiveData<String> getCurrentCategory() { return currentCategory; }
    public MutableLiveData<String> getCurDifficulty() { return curDifficulty; }
    public MutableLiveData<String> getCurGameMode() { return curGameMode; }
    public MutableLiveData<Integer> getSelectedImposterCount() { return selectedImposterCount; }
    public MutableLiveData<Integer> getSelectedTimerDuration() { return selectedTimerDuration; }
    public LiveData<List<GameRecord>> getAllGameRecords() { return repository.getAllRecords(); }

    public List<String> getSetupPlayerNames() {
        return setupPlayerNames;
    }

    public void addSetupPlayer(String name) {
        addSetupPlayer(name, "1111");
    }

    public void addSetupPlayer(String name, String pin) {
        if (name != null && !name.trim().isEmpty() && setupPlayerNames.size() < 12) {
            String trimmed = name.trim();
            if (!setupPlayerNames.contains(trimmed)) {
                setupPlayerNames.add(trimmed);
                savePlayerPin(trimmed, pin);
                updatePlayersListFromNames();
                savePlayerToPresets(trimmed);
            }
        }
    }

    private void savePlayerPin(String name, String pin) {
        String finalPin = (pin == null || pin.trim().isEmpty()) ? "1111" : pin.trim();
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("player_pin_" + name.toLowerCase(), finalPin).apply();
    }

    private String getPlayerPin(String name) {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("player_pin_" + name.toLowerCase(), "1111");
    }

    public void removeSetupPlayer(int index) {
        if (index >= 0 && index < setupPlayerNames.size()) {
            setupPlayerNames.remove(index);
            updatePlayersListFromNames();
        }
    }

    private void updatePlayersListFromNames() {
        List<Player> list = new ArrayList<>();
        for (int i = 0; i < setupPlayerNames.size(); i++) {
            Player p = new Player(i, setupPlayerNames.get(i));
            p.setPin(getPlayerPin(setupPlayerNames.get(i)));
            list.add(p);
        }
        players.setValue(list);
    }

    public void clearStats() {
        repository.clear();
    }

    public void startGame() {
        List<Player> activePlayers = players.getValue();
        if (activePlayers == null || activePlayers.size() < 3) return;

        // Clear shared canvas strokes
        sharedCanvasStrokes.clear();

        // Reset players
        for (Player p : activePlayers) {
            p.setRole("Innocent");
            p.setSecretWord("");
            p.setVotedOut(false);
            p.setDrawing(null);
            p.resetVotes();
            p.setVotedForName(null);
        }

        // Assign Imposters
        int numImposters = selectedImposterCount.getValue() != null ? selectedImposterCount.getValue() : 1;
        if (numImposters >= activePlayers.size()) {
            numImposters = activePlayers.size() - 2; // Keep at least 2 innocents
        }
        if (numImposters < 1) numImposters = 1;

        Random rand = new Random();
        Set<Integer> imposterIndices = new HashSet<>();
        while (imposterIndices.size() < numImposters) {
            imposterIndices.add(rand.nextInt(activePlayers.size()));
        }

        // Speed round modifies innocent/imposter mode timer slightly or words if needed
        String mode = curGameMode.getValue() != null ? curGameMode.getValue() : "Classic Imposter";
        boolean forceDrawing = "Drawing Imposter".equals(mode);

        // Get word pair
        String cat = currentCategory.getValue() != null ? currentCategory.getValue() : "Places";
        String diff = curDifficulty.getValue() != null ? curDifficulty.getValue() : "Medium";
        CategoriesData.WordPair pair = CategoriesData.getRandomWordPair(cat, diff, forceDrawing);
        innocentWord = pair.innocentWord;
        imposterWord = pair.imposterWord;

        for (int i = 0; i < activePlayers.size(); i++) {
            Player p = activePlayers.get(i);
            if (imposterIndices.contains(i)) {
                p.setRole("Imposter");
                if ("Drawing Imposter".equals(mode)) {
                    p.setSecretWord("??? Draw Vaguely!");
                } else if ("One Line Lie".equals(mode)) {
                    p.setSecretWord("??? LIE about something else!");
                } else {
                    p.setSecretWord(imposterWord);
                }
            } else {
                p.setRole("Innocent");
                p.setSecretWord(innocentWord);
            }
        }

        activePlayerIndex.setValue(0);
        currentGameState.setValue(GameState.ROLL_REVEAL);
    }

    public Player getCurrentPlayer() {
        List<Player> list = players.getValue();
        Integer idx = activePlayerIndex.getValue();
        if (list != null && idx != null && idx >= 0 && idx < list.size()) {
            return list.get(idx);
        }
        return null;
    }

    public void advancePassReveal() {
        List<Player> list = players.getValue();
        Integer idx = activePlayerIndex.getValue();
        if (list != null && idx != null) {
            int nextIdx = idx + 1;
            if (nextIdx < list.size()) {
                activePlayerIndex.setValue(nextIdx);
            } else {
                // Done rolling secret words! Let's transition
                String mode = curGameMode.getValue();
                if ("Drawing Imposter".equals(mode)) {
                    // Go to drawing turns phase
                    activePlayerIndex.setValue(0);
                    currentGameState.setValue(GameState.DRAWING_PHASE);
                } else {
                    // Traditional Word game modes: go directly to DISCUSSION
                    startDiscussionPhase();
                }
            }
        }
    }

    public void submitPlayerDrawing(Bitmap bitmap) {
        List<Player> list = players.getValue();
        Integer idx = activePlayerIndex.getValue();
        if (list != null && idx != null && idx >= 0 && idx < list.size()) {
            list.get(idx).setDrawing(bitmap);
            int nextIdx = idx + 1;
            if (nextIdx < list.size()) {
                activePlayerIndex.setValue(nextIdx);
            } else {
                // All drawings finished, go to Grid Gallery
                currentGameState.setValue(GameState.GALLERY);
            }
        }
    }

    public void startDiscussionPhase() {
        currentGameState.setValue(GameState.DISCUSSION);
        int duration = selectedTimerDuration.getValue() != null ? selectedTimerDuration.getValue() : 60;
        // Speed Round constraints
        if ("Speed Round".equals(curGameMode.getValue())) {
            duration = Math.min(duration, 20); // Speed round limits discussion to max 20 seconds!
        }
        startTimer(duration);
    }

    public void skipToVotePhase() {
        stopTimer();
        currentGameState.setValue(GameState.VOTING);
    }

    public void registerVote(Player votedPlayer) {
        if (votedPlayer != null) {
            votedPlayer.incrementVote();
        }
    }

    public void tallyVotesAndReveal() {
        List<Player> activePlayers = players.getValue();
        if (activePlayers == null || activePlayers.isEmpty()) return;

        Player votedOut = null;
        int maxVotes = -1;
        boolean tie = false;

        for (Player p : activePlayers) {
            if (p.getVoteCount() > maxVotes) {
                maxVotes = p.getVoteCount();
                votedOut = p;
                tie = false;
            } else if (p.getVoteCount() == maxVotes) {
                tie = true;
            }
        }

        // Even in a tie, we pick the first or vote out whichever has max index
        if (votedOut != null) {
            votedOut.setVotedOut(true);
        }

        // Check winners
        boolean allImpostersVotedOut = true;
        StringBuilder impostersSb = new StringBuilder();
        StringBuilder playersSb = new StringBuilder();

        for (Player p : activePlayers) {
            if (playersSb.length() > 0) playersSb.append(", ");
            playersSb.append(p.getName());

            if ("Imposter".equals(p.getRole())) {
                if (impostersSb.length() > 0) impostersSb.append(", ");
                impostersSb.append(p.getName());

                if (!p.isVotedOut()) {
                    allImpostersVotedOut = false;
                }
            }
        }

        String winner;
        String message;
        if (votedOut != null && "Imposter".equals(votedOut.getRole()) && allImpostersVotedOut) {
            winner = "Innocents";
            message = "Innocents Win! You voted out the Imposter (" + votedOut.getName() + ")! Secret word was: " + innocentWord;
        } else {
            winner = "Imposters";
            if (votedOut != null) {
                message = "Imposters Win! You booted out safe player (" + votedOut.getName() + " - " + votedOut.getRole() + ")! Imposter word was: " + imposterWord + ", Innocent word was: " + innocentWord;
            } else {
                message = "Imposters Win! Word pairs: Innocents: " + innocentWord + " | Imposter: " + imposterWord;
            }
        }

        winnerMessage.setValue(message);

        // Save to Database Room
        String mode = curGameMode.getValue();
        String category = currentCategory.getValue();
        String votedOutName = votedOut != null ? votedOut.getName() : "None";

        GameRecord record = new GameRecord(
                mode,
                winner,
                activePlayers.size(),
                System.currentTimeMillis(),
                playersSb.toString(),
                category,
                impostersSb.toString(),
                votedOutName
        );
        repository.insert(record);

        currentGameState.setValue(GameState.REVEAL_RESULT);
    }

    public void resetToSetup() {
        stopTimer();
        currentGameState.setValue(GameState.SETUP);
    }

    private void startTimer(int seconds) {
        stopTimer();
        timerSecondsRemaining.setValue(seconds);
        isTimerRunning.setValue(true);

        gameCountdownTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerSecondsRemaining.setValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timerSecondsRemaining.setValue(0);
                isTimerRunning.setValue(false);
                // Switch auto-forward to voting
                currentGameState.setValue(GameState.VOTING);
            }
        }.start();
    }

    private void stopTimer() {
        if (gameCountdownTimer != null) {
            gameCountdownTimer.cancel();
            gameCountdownTimer = null;
        }
        isTimerRunning.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTimer();
    }
}
