package com.example.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.databinding.FragmentHomeBinding;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SoundManager soundManager;
    private GameViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        soundManager = SoundManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);

        // Render mode tabs (Classic | Drawing)
        renderModeTabs();

        // Render topics grid
        renderTopicGrid();

        // Bind top-right setting gear icon opens dialogue
        binding.btnSettingsGear.setOnClickListener(v -> {
            soundManager.playClick();
            showSettingsDialog();
        });

        binding.btnStartGame.setOnClickListener(v -> {
            soundManager.playClick();
            Navigation.findNavController(v).navigate(R.id.action_home_to_setup);
        });

        binding.btnViewStats.setOnClickListener(v -> {
            soundManager.playClick();
            Navigation.findNavController(v).navigate(R.id.action_home_to_stats);
        });
    }

    private void showSettingsDialog() {
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        container.setPadding(48, 36, 48, 12);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Sound switch row
        LinearLayout soundRow = new LinearLayout(requireContext());
        soundRow.setOrientation(LinearLayout.HORIZONTAL);
        soundRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        TextView soundLabel = new TextView(requireContext());
        soundLabel.setText("Muted Sound Effects");
        soundLabel.setTextColor(android.graphics.Color.WHITE);
        soundLabel.setTextSize(14);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        soundLabel.setLayoutParams(labelParams);
        soundRow.addView(soundLabel);

        com.google.android.material.materialswitch.MaterialSwitch soundSwitch = new com.google.android.material.materialswitch.MaterialSwitch(requireContext());
        soundSwitch.setChecked(soundManager.isSoundMuted());
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.setSoundMuted(isChecked);
            if (!isChecked) {
                soundManager.playClick();
            }
        });
        soundRow.addView(soundSwitch);
        layout.addView(soundRow);

        // Spacer
        View divider = new View(requireContext());
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        divParams.setMargins(0, 24, 0, 24);
        divider.setLayoutParams(divParams);
        divider.setBackgroundColor(0x33FFFFFF);
        layout.addView(divider);

        // Vibration row
        LinearLayout vibrateRow = new LinearLayout(requireContext());
        vibrateRow.setOrientation(LinearLayout.HORIZONTAL);
        vibrateRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView vibrateLabel = new TextView(requireContext());
        vibrateLabel.setText("Haptic & Vibration");
        vibrateLabel.setTextColor(android.graphics.Color.WHITE);
        vibrateLabel.setTextSize(14);
        vibrateLabel.setLayoutParams(labelParams);
        vibrateRow.addView(vibrateLabel);

        com.google.android.material.materialswitch.MaterialSwitch vibrateSwitch = new com.google.android.material.materialswitch.MaterialSwitch(requireContext());
        vibrateSwitch.setChecked(soundManager.isVibrateMuted());
        vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.setVibrateMuted(isChecked);
            if (!isChecked) {
                soundManager.vibrate(100);
            }
        });
        vibrateRow.addView(vibrateSwitch);
        layout.addView(vibrateRow);

        // Spacer before How to Play button
        View divider2 = new View(requireContext());
        LinearLayout.LayoutParams divParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        divParams2.setMargins(0, 24, 0, 24);
        divider2.setLayoutParams(divParams2);
        divider2.setBackgroundColor(0x33FFFFFF);
        layout.addView(divider2);

        // How to Play button
        com.google.android.material.button.MaterialButton btnHowToPlay = new com.google.android.material.button.MaterialButton(requireContext(), null, com.google.android.material.R.style.Widget_Material3_Button_OutlinedButton);
        btnHowToPlay.setText("HOW TO PLAY MANUAL");
        btnHowToPlay.setTextColor(0xFF00ADB5); // Elegant teal accent
        btnHowToPlay.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFF00ADB5));
        btnHowToPlay.setStrokeWidth(3);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnHowToPlay.setLayoutParams(btnParams);
        btnHowToPlay.setOnClickListener(v -> {
            soundManager.playClick();
            showHowToPlayDialog();
        });
        layout.addView(btnHowToPlay);

        container.addView(layout);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sound & Haptics Settings")
                .setView(container)
                .setPositiveButton("Done", (dialog, which) -> soundManager.playClick())
                .show();
    }

    private void showHowToPlayDialog() {
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        container.setPadding(48, 36, 48, 12);

        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        String[] steps = {
            "1. Pass the device around so everyone secretly views their assigned keyword from localized categories (like Places, Foods & Drinks, Animals!).",
            "2. One or more players are designated IMPOSTERS. They get a slightly different keyword or blank instructions and do not know the real secret word.",
            "3. Debate or Draw on the canvas to prove your innocence to others. But don't give the secret word away, or the imposter will blend in!",
            "★ SHARED CANVAS MODE: All players draw stroke-by-stroke on exactly ONE cumulative canvas. Imposters must analyze others' strokes, guess the topic, and mimic it to blend in!",
            "4. Cast votes together. Boot out the imposters to win, or fail and let the imposters hijack the game!"
        };

        for (String step : steps) {
            TextView tv = new TextView(requireContext());
            tv.setText(step);
            tv.setTextColor(0xDDFFFFFF);
            tv.setTextSize(14);
            tv.setPadding(0, 0, 0, 24);
            layout.addView(tv);
        }

        scrollView.addView(layout);
        container.addView(scrollView);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("How to Play")
                .setView(container)
                .setPositiveButton("I Understood", (dialog, which) -> soundManager.playClick())
                .show();
    }

    private void renderTopicGrid() {
        binding.topicsGrid.removeAllViews();
        List<String> categories = com.example.model.CategoriesData.getCategoriesList();
        String selectedCategory = viewModel.getCurrentCategory().getValue();
        if (selectedCategory == null) {
            selectedCategory = "Places";
            viewModel.getCurrentCategory().setValue(selectedCategory);
        }

        for (String cat : categories) {
            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(requireContext());
            
            // Grid layout parameters
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = 0;
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            params.rowSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED);
            params.setMargins(12, 12, 12, 12);
            card.setLayoutParams(params);
            card.setRadius(6); // Sleek slight rounding on simple square

            boolean isSelected = cat.equalsIgnoreCase(selectedCategory);
            if (isSelected) {
                card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF222831));
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFF05454)); // brand Red accent
                card.setStrokeWidth(3);
            } else {
                card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0x0EFFFFFF));
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(0x1FFFFFFF));
                card.setStrokeWidth(2);
            }

            TextView tv = new TextView(requireContext());
            tv.setText(cat.toUpperCase());
            tv.setTextColor(isSelected ? 0xFFFFFFFF : 0xCCFFFFFF);
            tv.setTextSize(14);
            tv.setLetterSpacing(-0.01f);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(16, 44, 16, 44); // Beautiful square padding proportions

            card.addView(tv);

            card.setOnClickListener(v -> {
                soundManager.playClick();
                viewModel.getCurrentCategory().setValue(cat);
                renderTopicGrid(); // re-render state highlights
            });

            binding.topicsGrid.addView(card);
        }
    }

    private void renderModeTabs() {
        String mode = viewModel.getCurGameMode().getValue();
        if (mode == null) {
            mode = "Classic Imposter";
            viewModel.getCurGameMode().setValue(mode);
        }

        boolean isDrawing = "Drawing Imposter".equals(mode);

        if (isDrawing) {
            binding.btnTabClassic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            binding.btnTabClassic.setTextColor(0xFFB0B5BC); // text_grey_sub

            binding.btnTabDrawing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF05454)); // brand secondary crimson
            binding.btnTabDrawing.setTextColor(android.graphics.Color.WHITE);
        } else {
            binding.btnTabClassic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF05454)); // brand secondary crimson
            binding.btnTabClassic.setTextColor(android.graphics.Color.WHITE);

            binding.btnTabDrawing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            binding.btnTabDrawing.setTextColor(0xFFB0B5BC); // text_grey_sub
        }

        binding.btnTabClassic.setOnClickListener(v -> {
            soundManager.playClick();
            viewModel.getCurGameMode().setValue("Classic Imposter");
            renderModeTabs();
        });

        binding.btnTabDrawing.setOnClickListener(v -> {
            soundManager.playClick();
            viewModel.getCurGameMode().setValue("Drawing Imposter");
            renderModeTabs();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
