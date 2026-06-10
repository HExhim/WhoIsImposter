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
import com.example.R;
import com.example.databinding.FragmentSetupBinding;
import com.example.model.CategoriesData;
import com.example.model.Player;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;
import com.google.android.material.chip.Chip;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class SetupFragment extends Fragment {

    private FragmentSetupBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private PlayersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSetupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());

        // Setup Recycler
        adapter = new PlayersAdapter(index -> {
            soundManager.playClick();
            viewModel.removeSetupPlayer(index);
        });
        binding.rvSetupPlayers.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        binding.rvSetupPlayers.setAdapter(adapter);

        // Observe players
        viewModel.getPlayers().observe(getViewLifecycleOwner(), players -> {
            adapter.setPlayers(players);
            // Cap max imposters relative to player size
            int maxPossibleImposters = Math.max(1, (players.size() - 2));
            if (viewModel.getSelectedImposterCount().getValue() != null &&
                    viewModel.getSelectedImposterCount().getValue() > maxPossibleImposters) {
                viewModel.getSelectedImposterCount().setValue(maxPossibleImposters);
            }
        });

        // Add custom player
        binding.btnAddPlayer.setOnClickListener(v -> {
            String name = binding.inputPlayerName.getText().toString().trim();
            String pin = binding.inputPlayerPin.getText().toString();
            if (TextUtils.isEmpty(name)) {
                soundManager.playFail();
                Toast.makeText(requireContext(), "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (hasDuplicatePlayer(name)) {
                soundManager.playFail();
                Toast.makeText(requireContext(), "Player with this name already exists!", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(pin.trim()) || pin.trim().length() < 3) {
                soundManager.playFail();
                Toast.makeText(requireContext(), "Enter at least a 3-digit PIN!", Toast.LENGTH_SHORT).show();
            } else {
                soundManager.playClick();
                viewModel.addSetupPlayer(name, pin.trim());
                binding.inputPlayerName.setText("");
                binding.inputPlayerPin.setText("");
            }
        });

        // Setup horizontal presets dynamically if they exist from previous sessions
        viewModel.getSavedPresets().observe(getViewLifecycleOwner(), savedPresets -> {
            setupPresetsRow(savedPresets);
        });

        // Set up attribute counters live updates
        viewModel.getSelectedImposterCount().observe(getViewLifecycleOwner(), count ->
                binding.tvImposterCount.setText(String.valueOf(count)));

        viewModel.getSelectedTimerDuration().observe(getViewLifecycleOwner(), duration ->
                binding.tvTimerDuration.setText(duration + "s"));

        binding.btnSubImposter.setOnClickListener(v -> {
            soundManager.playClick();
            int cur = viewModel.getSelectedImposterCount().getValue() != null ? viewModel.getSelectedImposterCount().getValue() : 1;
            if (cur > 1) {
                viewModel.getSelectedImposterCount().setValue(cur - 1);
            }
        });

        binding.btnAddImposter.setOnClickListener(v -> {
            soundManager.playClick();
            int cur = viewModel.getSelectedImposterCount().getValue() != null ? viewModel.getSelectedImposterCount().getValue() : 1;
            List<Player> curPlayers = viewModel.getPlayers().getValue();
            int limit = (curPlayers != null ? curPlayers.size() : 4) - 2;
            if (cur < limit && cur < 5) {
                viewModel.getSelectedImposterCount().setValue(cur + 1);
            } else {
                soundManager.playFail();
                Toast.makeText(requireContext(), "Keep at least 2 safe players!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSubTimer.setOnClickListener(v -> {
            soundManager.playClick();
            int cur = viewModel.getSelectedTimerDuration().getValue() != null ? viewModel.getSelectedTimerDuration().getValue() : 60;
            if (cur > 15) {
                viewModel.getSelectedTimerDuration().setValue(cur - 15);
            }
        });

        binding.btnAddTimer.setOnClickListener(v -> {
            soundManager.playClick();
            int cur = viewModel.getSelectedTimerDuration().getValue() != null ? viewModel.getSelectedTimerDuration().getValue() : 60;
            if (cur < 300) {
                viewModel.getSelectedTimerDuration().setValue(cur + 15);
            }
        });

        // LAUNCH GAME ACTION
        binding.btnLaunchGame.setOnClickListener(v -> {
            List<Player> curPlayers = viewModel.getPlayers().getValue();
            if (curPlayers == null || curPlayers.size() < 3) {
                soundManager.playFail();
                Toast.makeText(requireContext(), "Need at least 3 players to play!", Toast.LENGTH_LONG).show();
                return;
            }

            // Successfully initiate
            soundManager.playReveal();
            viewModel.startGame();
            Navigation.findNavController(v).navigate(R.id.action_setup_to_passDevice);
        });
    }

    private void setupPresetsRow(List<String> presetsList) {
        if (presetsList == null || presetsList.isEmpty()) {
            binding.tvPresetsTitle.setVisibility(View.GONE);
            binding.presetsScroll.setVisibility(View.GONE);
            binding.presetsContainer.removeAllViews();
            return;
        }

        binding.tvPresetsTitle.setVisibility(View.VISIBLE);
        binding.presetsScroll.setVisibility(View.VISIBLE);
        binding.presetsContainer.removeAllViews();
        for (String p : presetsList) {
            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 16, 0); // clear spacing
            card.setLayoutParams(lp);
            card.setRadius(4); // Sleek simple square corners as requested!
            card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF222831)); // theme canvas contrast
            card.setStrokeColor(android.content.res.ColorStateList.valueOf(0x33FFFFFF));
            card.setStrokeWidth(2);

            TextView tv = new TextView(requireContext());
            tv.setText("+ " + p.toUpperCase());
            tv.setTextColor(android.graphics.Color.WHITE);
            tv.setTextSize(13);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(32, 24, 32, 24); // Square-ish styling
            tv.setGravity(android.view.Gravity.CENTER);

            card.addView(tv);

            card.setOnClickListener(v -> {
                soundManager.playClick();
                binding.inputPlayerName.setText(p);
                binding.inputPlayerPin.setText(getSavedPlayerPin(p));
            });

            // Long click to delete preset
            card.setOnLongClickListener(v -> {
                soundManager.vibrate(80);
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Preset")
                        .setMessage("Do you want to delete '" + p + "' from quick additions?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            soundManager.playFail();
                            viewModel.deleteSavedPreset(p);
                            Toast.makeText(requireContext(), "'" + p + "' deleted from presets", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });

            binding.presetsContainer.addView(card);
        }
    }



    private boolean hasDuplicatePlayer(String name) {
        List<Player> playersList = viewModel.getPlayers().getValue();
        if (playersList == null) return false;
        for (Player p : playersList) {
            if (p.getName().trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private String getSavedPlayerPin(String name) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("Mehfil_Prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("player_pin_" + name.toLowerCase(), "1111");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
