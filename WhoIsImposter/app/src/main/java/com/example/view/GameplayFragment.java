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
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.R;
import com.example.databinding.FragmentGameplayBinding;
import com.example.model.Player;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;

public class GameplayFragment extends Fragment {

    private FragmentGameplayBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private GalleryAdapter galleryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameplayBinding.inflate(inflater, container, false);
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

        // Setup RecyclerView for Gallery
        galleryAdapter = new GalleryAdapter();
        binding.rvDrawingsGallery.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvDrawingsGallery.setAdapter(galleryAdapter);

        // Observe central Game Stage to activate correct nested layouts
        viewModel.getCurrentGameState().observe(getViewLifecycleOwner(), state -> {
            hideAllNestedLayouts();
            if (state == GameViewModel.GameState.DISCUSSION) {
                binding.layoutDiscussionPhase.setVisibility(View.VISIBLE);
                setupDiscussionPhase();
            } else if (state == GameViewModel.GameState.DRAWING_PHASE) {
                binding.layoutPaintPhase.setVisibility(View.VISIBLE);
                setupPaintingPhase();
            } else if (state == GameViewModel.GameState.GALLERY) {
                binding.layoutGalleryPhase.setVisibility(View.VISIBLE);
                setupGalleryPhase();
            } else if (state == GameViewModel.GameState.VOTING) {
                // Navigate forward to voting
                Navigation.findNavController(requireView()).navigate(R.id.action_gameplay_to_voting);
            }
        });

        // Ticking Countdown observation
        viewModel.getTimerSecondsRemaining().observe(getViewLifecycleOwner(), seconds -> {
            int mins = seconds / 60;
            int secs = seconds % 60;
            binding.tvCountdownText.setText(String.format("%02d:%02d", mins, secs));

            // Warning visual tick feedback on low seconds!
            if (seconds <= 5 && seconds > 0) {
                soundManager.playClick();
            }
        });
    }

    private void hideAllNestedLayouts() {
        binding.layoutDiscussionPhase.setVisibility(View.GONE);
        binding.layoutPaintPhase.setVisibility(View.GONE);
        binding.layoutGalleryPhase.setVisibility(View.GONE);
    }

    private void setupDiscussionPhase() {
        binding.tvDiscCategory.setText("Topic: " + viewModel.getCurrentCategory().getValue() + " • Mode: " + viewModel.getCurGameMode().getValue());

        binding.btnStopTimerVote.setOnClickListener(v -> {
            soundManager.playClick();
            viewModel.skipToVotePhase();
        });
    }

    private void setupPaintingPhase() {
        Player player = viewModel.getCurrentPlayer();
        if (player != null) {
            binding.tvPainterName.setText(player.getName());
            binding.canvasPainter.setStrokes(viewModel.getSharedCanvasStrokes());

            // Setup prompt depending on whether they are imposters!
            if ("Imposter".equals(player.getRole())) {
                binding.tvDrawingWordReminder.setText("Impostor! Guess & Mimic!");
                binding.tvDrawingWordReminder.setTextColor(0xFFEC407A); // Crimson warning
            } else {
                binding.tvDrawingWordReminder.setText("Draw: " + player.getSecretWord());
                binding.tvDrawingWordReminder.setTextColor(0xFF66BB6A); // Lime guideline
            }

            // Painter controls palette wiring
            binding.colorChipBlack.setOnClickListener(v -> {
                soundManager.playClick();
                binding.canvasPainter.strokeColor(ColorPalette.BLACK);
            });
            binding.colorChipIndigo.setOnClickListener(v -> {
                soundManager.playClick();
                binding.canvasPainter.strokeColor(ColorPalette.INDIGO);
            });
            binding.colorChipCrimson.setOnClickListener(v -> {
                soundManager.playClick();
                binding.canvasPainter.strokeColor(ColorPalette.CRIMSON);
            });
            binding.colorChipLime.setOnClickListener(v -> {
                soundManager.playClick();
                binding.canvasPainter.strokeColor(ColorPalette.LIME);
            });

            binding.btnPaintUndo.setOnClickListener(v -> {
                soundManager.playClick();
                binding.canvasPainter.undo();
            });

            binding.btnPaintClear.setOnClickListener(v -> {
                soundManager.playFail();
                binding.canvasPainter.setStrokes(viewModel.getSharedCanvasStrokes());
            });

            // Action submit
            binding.btnPaintSubmit.setOnClickListener(v -> {
                soundManager.playReveal();
                viewModel.setSharedCanvasStrokes(binding.canvasPainter.getStrokes());
                viewModel.submitPlayerDrawing(binding.canvasPainter.getDrawingBitmap());
            });
        }
    }

    private void setupGalleryPhase() {
        galleryAdapter.setPlayers(viewModel.getPlayers().getValue());

        binding.btnGalleryToVoting.setOnClickListener(v -> {
            soundManager.playClick();
            Navigation.findNavController(v).navigate(R.id.action_gameplay_to_voting);
        });
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

    // Dynamic hex colors palette helper class variables
    private static class ColorPalette {
        static final int BLACK = 0xFF000000;
        static final int INDIGO = 0xFF5C6BC0;
        static final int CRIMSON = 0xFFEC407A;
        static final int LIME = 0xFF26A69A;
    }
}
