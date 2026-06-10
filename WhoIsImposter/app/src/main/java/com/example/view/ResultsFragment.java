package com.example.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.R;
import com.example.databinding.FragmentResultsBinding;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;

public class ResultsFragment extends Fragment {

    private FragmentResultsBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private ResultsAdapter resultsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());

        // Setup roles adapter
        resultsAdapter = new ResultsAdapter();
        binding.rvResultsRoles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResultsRoles.setAdapter(resultsAdapter);
        resultsAdapter.setPlayers(viewModel.getPlayers().getValue());

        // Dynamic Fun Awards and Achievements Computation
        displayMatchAwards();

        // Observe and update winner configurations
        viewModel.getWinnerMessage().observe(getViewLifecycleOwner(), message -> {
            binding.tvMatchWinnerSubtitle.setText(message);

            if (message != null && message.contains("Innocents Win")) {
                binding.winnerBannerCard.setCardBackgroundColor(0xFFF05454); // Brand Red Accent
                binding.tvMatchWinnerTitle.setText("INNOCENTS WIN!");
                binding.tvMatchWinnerTitle.setTextColor(0xFFFFFFFF);
                binding.tvMatchWinnerSubtitle.setTextColor(0xB3FFFFFF);
            } else {
                binding.winnerBannerCard.setCardBackgroundColor(0xFFFFFFFF); // Clean white contrast card
                binding.tvMatchWinnerTitle.setText("IMPOSTERS WIN!");
                binding.tvMatchWinnerTitle.setTextColor(0xFF0F0F0F);
                binding.tvMatchWinnerSubtitle.setTextColor(0xCC222831);
            }
        });

        // Navigation flows
        binding.btnReplaySame.setOnClickListener(v -> {
            soundManager.playClick();
            viewModel.resetToSetup();
            Navigation.findNavController(v).navigate(R.id.action_results_to_setup);
        });

        binding.btnBackHome.setOnClickListener(v -> {
            soundManager.playClick();
            viewModel.resetToSetup();
            Navigation.findNavController(v).navigate(R.id.action_results_to_home);
        });
    }

    private void displayMatchAwards() {
        if (binding == null) return;
        binding.containerAwards.removeAllViews();

        java.util.List<com.example.model.Player> playersList = viewModel.getPlayers().getValue();
        if (playersList == null || playersList.isEmpty()) return;

        java.util.List<Award> awardsList = new java.util.ArrayList<>();
        
        // Map for quick model lookups by name
        java.util.Map<String, com.example.model.Player> playerMap = new java.util.HashMap<>();
        for (com.example.model.Player p : playersList) {
            playerMap.put(p.getName(), p);
        }

        // 1. PRIME SUSPECT: highest vote count received
        com.example.model.Player primeSuspect = null;
        int maxVotes = 0;
        boolean multiSuspects = false;
        for (com.example.model.Player p : playersList) {
            if (p.getVoteCount() > maxVotes) {
                maxVotes = p.getVoteCount();
                primeSuspect = p;
                multiSuspects = false;
            } else if (p.getVoteCount() == maxVotes && maxVotes > 0) {
                multiSuspects = true;
            }
        }
        if (primeSuspect != null && maxVotes > 0) {
            String suffix = multiSuspects ? " (tied with others)" : "";
            awardsList.add(new Award("🚨", "PRIME SUSPECT", primeSuspect.getName() + suffix, 
                "Attracted the raw heat of suspicion, gathering " + maxVotes + " total ballots!"));
        }

        // 2. THE SHERLOCK (Detective): Innocent player(s) who voted for an Imposter
        java.util.List<String> detectiveNames = new java.util.ArrayList<>();
        for (com.example.model.Player p : playersList) {
            if ("Innocent".equals(p.getRole()) && p.getVotedForName() != null) {
                com.example.model.Player target = playerMap.get(p.getVotedForName());
                if (target != null && "Imposter".equals(target.getRole())) {
                    detectiveNames.add(p.getName());
                }
            }
        }
        if (!detectiveNames.isEmpty()) {
            String names = android.text.TextUtils.join(", ", detectiveNames);
            awardsList.add(new Award("🕵️‍♂️", "THE SHERLOCK", names, 
                "Spotted through the lies and cast their secret ballot correctly against the real Imposter!"));
        }

        // 3. BAMBOOZLED: Innocent player(s) who voted for other Innocent(s)
        java.util.List<String> bamboozledNames = new java.util.ArrayList<>();
        for (com.example.model.Player p : playersList) {
            if ("Innocent".equals(p.getRole()) && p.getVotedForName() != null) {
                com.example.model.Player target = playerMap.get(p.getVotedForName());
                if (target != null && "Innocent".equals(target.getRole())) {
                    bamboozledNames.add(p.getName());
                }
            }
        }
        if (!bamboozledNames.isEmpty()) {
            String names = android.text.TextUtils.join(", ", bamboozledNames);
            awardsList.add(new Award("🧸", "BAMBOOZLED", names, 
                "Took the bait! Misled by the whispers and voted for a completely safe, innocent peer."));
        }

        // 4. MASTER OF DISGUISE (or CAUGHT RED HANDED)
        java.util.List<String> sleekImposters = new java.util.ArrayList<>();
        for (com.example.model.Player p : playersList) {
            if ("Imposter".equals(p.getRole()) && !p.isVotedOut()) {
                sleekImposters.add(p.getName());
            }
        }
        if (!sleekImposters.isEmpty()) {
            String names = android.text.TextUtils.join(", ", sleekImposters);
            awardsList.add(new Award("🎭", "MASTER OF DISGUISE", names, 
                "Successfully deceived the crowd, survived without getting booted, and won the round!"));
        } else {
            java.util.List<String> bustedImposters = new java.util.ArrayList<>();
            for (com.example.model.Player p : playersList) {
                if ("Imposter".equals(p.getRole()) && p.isVotedOut()) {
                    bustedImposters.add(p.getName());
                }
            }
            if (!bustedImposters.isEmpty()) {
                String names = android.text.TextUtils.join(", ", bustedImposters);
                awardsList.add(new Award("🛑", "CAUGHT RED HANDED", names, 
                    "Could not pull off the deception. Outvoted, exposed, and captured by the locals!"));
            }
        }

        // 5. THE SILENT SPECTRE: Received absolute 0 votes
        java.util.List<String> zeroVoteNames = new java.util.ArrayList<>();
        for (com.example.model.Player p : playersList) {
            if (p.getVoteCount() == 0) {
                zeroVoteNames.add(p.getName());
            }
        }
        if (!zeroVoteNames.isEmpty()) {
            String names = android.text.TextUtils.join(", ", zeroVoteNames);
            awardsList.add(new Award("👻", "THE SILENT SPECTRE", names, 
                "Remained completely stealthy under the radar, drawing exactly zero votes. Flawless!"));
        }

        // 6. LONE WOLF: Voted for an unpopular candidate who got only 1 vote
        java.util.List<String> loneWolves = new java.util.ArrayList<>();
        for (com.example.model.Player p : playersList) {
            if (p.getVotedForName() != null) {
                com.example.model.Player target = playerMap.get(p.getVotedForName());
                if (target != null && target.getVoteCount() == 1) {
                    loneWolves.add(p.getName());
                }
            }
        }
        if (!loneWolves.isEmpty()) {
            String names = android.text.TextUtils.join(", ", loneWolves);
            awardsList.add(new Award("🐺", "LONE WOLF", names, 
                "Alone in their suspicion, they cast a vote for a candidate that nobody else suspected."));
        }

        // Inflate the layout and render the calculated list dynamically
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Award award : awardsList) {
            View itemView = inflater.inflate(R.layout.item_award, binding.containerAwards, false);
            
            android.widget.TextView tvEmoji = itemView.findViewById(R.id.tv_award_emoji);
            android.widget.TextView tvTitle = itemView.findViewById(R.id.tv_award_title);
            android.widget.TextView tvWinner = itemView.findViewById(R.id.tv_award_winner);
            android.widget.TextView tvDesc = itemView.findViewById(R.id.tv_award_desc);

            tvEmoji.setText(award.emoji);
            tvTitle.setText(award.title);
            tvWinner.setText(award.winner.toUpperCase());
            tvDesc.setText(award.description);

            binding.containerAwards.addView(itemView);
        }
    }

    private static class Award {
        String emoji;
        String title;
        String winner;
        String description;

        Award(String emoji, String title, String winner, String description) {
            this.emoji = emoji;
            this.title = title;
            this.winner = winner;
            this.description = description;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
