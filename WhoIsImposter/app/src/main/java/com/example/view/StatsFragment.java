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
import com.example.data.GameRecord;
import com.example.databinding.FragmentStatsBinding;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;

import java.util.List;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private StatsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());

        // Connect recycler adapter
        adapter = new StatsAdapter();
        binding.rvStatsHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStatsHistory.setAdapter(adapter);

        // Live Room database observational monitoring
        viewModel.getAllGameRecords().observe(getViewLifecycleOwner(), records -> {
            if (records == null || records.isEmpty()) {
                binding.tvEmptyStatsPrompt.setVisibility(View.VISIBLE);
                binding.rvStatsHistory.setVisibility(View.GONE);

                // Zero out numerical stats cards
                binding.tvStatTotalGames.setText("0");
                binding.tvStatInnocentWins.setText("0");
                binding.tvStatImposterWins.setText("0");
            } else {
                binding.tvEmptyStatsPrompt.setVisibility(View.GONE);
                binding.rvStatsHistory.setVisibility(View.VISIBLE);
                adapter.setRecords(records);

                // Compute numerical splits
                int totalGames = records.size();
                int innocentWins = 0;
                int imposterWins = 0;

                for (GameRecord gr : records) {
                    if ("Innocents".equals(gr.getWinner())) {
                        innocentWins++;
                    } else if ("Imposters".equals(gr.getWinner())) {
                        imposterWins++;
                    }
                }

                // Hydrate stat text UI items
                binding.tvStatTotalGames.setText(String.valueOf(totalGames));
                binding.tvStatInnocentWins.setText(String.valueOf(innocentWins));
                binding.tvStatImposterWins.setText(String.valueOf(imposterWins));
            }
        });

        // Trigger dynamic resets
        binding.btnClearStats.setOnClickListener(v -> {
            soundManager.playFail(); 
            viewModel.clearStats();
        });

        binding.btnStatsBackHome.setOnClickListener(v -> {
            soundManager.playClick();
            Navigation.findNavController(v).navigate(R.id.action_stats_to_home);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
