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
import com.example.R;
import com.example.databinding.FragmentPassDeviceBinding;
import com.example.model.Player;
import com.example.util.SoundManager;
import com.example.viewmodel.GameViewModel;

import java.util.List;

public class PassDeviceFragment extends Fragment {

    private FragmentPassDeviceBinding binding;
    private GameViewModel viewModel;
    private SoundManager soundManager;
    private boolean isSecretRevealed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPassDeviceBinding.inflate(inflater, container, false);
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

        // Observe game state to auto-navigate when roll-reveal settles
        viewModel.getCurrentGameState().observe(getViewLifecycleOwner(), state -> {
            if (state == GameViewModel.GameState.DISCUSSION || state == GameViewModel.GameState.DRAWING_PHASE) {
                Navigation.findNavController(requireView()).navigate(R.id.action_passDevice_to_gameplay);
            }
        });

        // Observe active player index to dynamically refresh the screen handoff values
        viewModel.getActivePlayerIndex().observe(getViewLifecycleOwner(), index -> {
            // Conceal again for safety when index shifts!
            concealSecret();
            refreshPlayerDetails();
        });
    }

    private void refreshPlayerDetails() {
        Player player = viewModel.getCurrentPlayer();
        List<Player> allPlayers = viewModel.getPlayers().getValue();
        Integer idx = viewModel.getActivePlayerIndex().getValue();

        if (player != null && allPlayers != null && idx != null) {
            binding.tvRevealProgress.setText("PLAYER " + (idx + 1) + " OF " + allPlayers.size());
            binding.tvTargetPlayerName.setText(player.getName());
            binding.tvPassPrompt.setText("Pass physical device to:");
            binding.tvPassSubtitle.setText("No spying allowed! Only " + player.getName() + " should look!");

            // Secret texts
            binding.tvRevealedWordText.setText(player.getSecretWord());
            binding.tvRoleDescriptor.setText("Role: " + player.getRole());
            if ("Imposter".equals(player.getRole())) {
                binding.tvRoleDescriptor.setTextColor(0xFFEC407A); // Crimson indicator
            } else {
                binding.tvRoleDescriptor.setTextColor(0xFF66BB6A); // Lime indicator
            }

            // Click listener for secret card
            binding.cardSecretUnveil.setOnClickListener(v -> {
                if (isSecretRevealed) {
                    concealSecret();
                } else {
                    promptForPinAndReveal();
                }
            });

            // Action button logic
            binding.btnActionConfirm.setOnClickListener(v -> {
                soundManager.playClick();
                viewModel.advancePassReveal();
            });
        }
    }

    private void promptForPinAndReveal() {
        Player player = viewModel.getCurrentPlayer();
        if (player == null) return;

        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        container.setPadding(48, 36, 48, 12);

        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(requireContext(),
                null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        inputLayout.setHint("Enter 3-4 digit PIN");
        inputLayout.setBoxStrokeColor(0xFFF05454); // Match brand red accent
        inputLayout.setHintTextColor(android.content.res.ColorStateList.valueOf(0xFFFFF054));

        com.google.android.material.textfield.TextInputEditText editText = new com.google.android.material.textfield.TextInputEditText(inputLayout.getContext());
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setMaxLines(1);
        editText.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});
        editText.setTextColor(android.graphics.Color.WHITE);
        editText.setTextSize(16);

        inputLayout.addView(editText);
        container.addView(inputLayout);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Security Verification")
                .setMessage("Hi " + player.getName() + ",\nEnter your PIN to reveal your secret word:")
                .setView(container)
                .setCancelable(false)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String entered = editText.getText().toString().trim();
                    if (entered.equals(player.getPin())) {
                        revealSecret();
                    } else {
                        soundManager.playFail();
                        android.widget.Toast.makeText(requireContext(), "Wrong PIN! Access Denied.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void revealSecret() {
        isSecretRevealed = true;
        soundManager.playReveal();

        binding.layoutHiddenState.setVisibility(View.GONE);
        binding.layoutRevealedState.setVisibility(View.VISIBLE);
        binding.btnActionConfirm.setVisibility(View.VISIBLE);
    }

    private void concealSecret() {
        isSecretRevealed = false;
        binding.layoutHiddenState.setVisibility(View.VISIBLE);
        binding.layoutRevealedState.setVisibility(View.GONE);
        binding.btnActionConfirm.setVisibility(View.INVISIBLE);
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
