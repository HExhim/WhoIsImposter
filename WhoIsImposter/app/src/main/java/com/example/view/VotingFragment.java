package com.example.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.R;
import com.example.databinding.FragmentVotingBinding;
import com.example.model.Player;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;

import java.util.ArrayList;
import java.util.List;

public class VotingFragment extends Fragment {

    private FragmentVotingBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private VotingAdapter votingAdapter;

    private List<Player> votersList = new ArrayList<>();
    private int currentVoterIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVotingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());

        // Handle Back Press Navigation Confirmatory
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

        // Reset all player vote registers for this fresh voting session
        List<Player> allPlayers = viewModel.getPlayers().getValue();
        if (allPlayers != null) {
            for (Player p : allPlayers) {
                p.resetVotes();
            }
        }

        // Determine active voters (players not voted out yet)
        votersList.clear();
        if (allPlayers != null) {
            for (Player p : allPlayers) {
                if (!p.isVotedOut()) {
                    votersList.add(p);
                }
            }
        }

        currentVoterIndex = 0;

        // Initialize voting list adapter
        votingAdapter = new VotingAdapter(selectedSuspect -> {
            // Enable confidential submit button ONLY when a player selection is active/non-null
            binding.btnCastSecretVote.setEnabled(selectedSuspect != null);
        });

        binding.rvVotingSuspects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVotingSuspects.setAdapter(votingAdapter);

        // Wire Unlock Ballot button
        binding.btnUnlockBallot.setOnClickListener(v -> {
            soundManager.playClick();
            verifyPinAndUnlock();
        });

        // Wire Cast Secret Vote button
        binding.btnCastSecretVote.setOnClickListener(v -> {
            castSecretVoteAndProceed();
        });

        // Wire Reveal Match Results button
        binding.btnTallyVotes.setOnClickListener(v -> {
            soundManager.playSuccess();
            viewModel.tallyVotesAndReveal();
            Navigation.findNavController(v).navigate(R.id.action_voting_to_results);
        });

        // Start the turn-by-turn voting cycle
        if (votersList.isEmpty()) {
            // Fallback if somehow no voters exist
            binding.layoutVoterUnlock.setVisibility(View.GONE);
            binding.layoutBallot.setVisibility(View.GONE);
            binding.layoutVotingComplete.setVisibility(View.VISIBLE);
        } else {
            showVoterTurn();
        }
    }

    private void showVoterTurn() {
        if (currentVoterIndex >= votersList.size()) {
            showVotingComplete();
            return;
        }

        Player currentVoter = votersList.get(currentVoterIndex);

        // Update passage card texts
        binding.tvCurrentVoterName.setText(currentVoter.getName().toUpperCase());
        binding.inputVoterPin.setText("");

        // Reset and clear input focus
        binding.inputVoterPin.clearFocus();

        // Switch to passage mode
        binding.layoutVoterUnlock.setVisibility(View.VISIBLE);
        binding.layoutBallot.setVisibility(View.GONE);
        binding.layoutVotingComplete.setVisibility(View.GONE);
    }

    private void verifyPinAndUnlock() {
        if (currentVoterIndex >= votersList.size()) return;

        Player currentVoter = votersList.get(currentVoterIndex);
        String enteredPin = binding.inputVoterPin.getText().toString().trim();
        String expectedPin = currentVoter.getPin();

        if (TextUtils.isEmpty(expectedPin)) {
            expectedPin = "1111"; // universal fallback
        }

        if (enteredPin.equals(expectedPin)) {
            // Authorized! Unlock ballot and reveal suspects
            soundManager.playClick();
            unlockBallotScreen(currentVoter);
        } else {
            // Denied!
            soundManager.playFail();
            Toast.makeText(requireContext(), "Incorrect security PIN! Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void unlockBallotScreen(Player currentVoter) {
        binding.tvBallotCaption.setText("VOTER: " + currentVoter.getName().toUpperCase() + " (Confidential ballot)");
        
        // Exclude the current voter from the list of candidates to prevent self-voting
        List<Player> suspects = new ArrayList<>();
        for (Player p : votersList) {
            if (p.getId() != currentVoter.getId()) {
                suspects.add(p);
            }
        }

        votingAdapter.setSuspects(suspects);

        // Switch container view state
        binding.layoutVoterUnlock.setVisibility(View.GONE);
        binding.layoutBallot.setVisibility(View.VISIBLE);
        binding.layoutVotingComplete.setVisibility(View.GONE);
    }

    private void castSecretVoteAndProceed() {
        Player selected = votingAdapter.getSelectedSuspect();
        if (selected == null) {
            soundManager.playFail();
            Toast.makeText(requireContext(), "Please select one player to vote!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Record who voted for who confidential targets
        if (currentVoterIndex < votersList.size()) {
            Player currentVoter = votersList.get(currentVoterIndex);
            currentVoter.setVotedForName(selected.getName());
        }

        // Register the safe vote
        viewModel.registerVote(selected);
        soundManager.playClick();

        // Clear selection and advance to next voter in sequence
        votingAdapter.clearSelection();
        currentVoterIndex++;

        if (currentVoterIndex < votersList.size()) {
            showVoterTurn();
        } else {
            showVotingComplete();
        }
    }

    private void showVotingComplete() {
        binding.layoutVoterUnlock.setVisibility(View.GONE);
        binding.layoutBallot.setVisibility(View.GONE);
        binding.layoutVotingComplete.setVisibility(View.VISIBLE);
    }

    private void showExitConfirmationDialog() {
        soundManager.playFail();
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Exit Game Match?")
                .setMessage("Are you sure you want to end this match? Current game progress will be lost.")
                .setPositiveButton("Exit Match", (dialog, which) -> {
                    soundManager.playClick();
                    Navigation.findNavController(binding.getRoot()).popBackStack(R.id.homeFragment, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
