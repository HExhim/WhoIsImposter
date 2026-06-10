package com.example.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemVotingSuspectBinding;
import com.example.model.Player;
import com.example.util.SoundManager;
import java.util.ArrayList;
import java.util.List;

public class VotingAdapter extends RecyclerView.Adapter<VotingAdapter.ViewHolder> {

    public interface OnSuspectSelectedListener {
        void onSuspectSelected(Player suspect);
    }

    private List<Player> suspects = new ArrayList<>();
    private final OnSuspectSelectedListener listener;
    private int selectedPosition = -1;

    public VotingAdapter(OnSuspectSelectedListener listener) {
        this.listener = listener;
    }

    public void setSuspects(List<Player> newList) {
        this.suspects = newList != null ? newList : new ArrayList<Player>();
        this.selectedPosition = -1; // Reset selection on new suspect list load
        notifyDataSetChanged();
    }

    public Player getSelectedSuspect() {
        if (selectedPosition >= 0 && selectedPosition < suspects.size()) {
            return suspects.get(selectedPosition);
        }
        return null;
    }

    public void clearSelection() {
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVotingSuspectBinding binding = ItemVotingSuspectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player suspect = suspects.get(position);
        holder.binding.tvSuspectName.setText(suspect.getName());
        
        // Secretive voting: Hide actual votes during voting turn-by-turn process
        holder.binding.tvSuspectVotes.setVisibility(View.GONE);

        boolean isSelected = (position == selectedPosition);

        // Styling based on selection state
        if (isSelected) {
            holder.binding.cardSuspect.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFF05454)); // brand secondary crimson
            holder.binding.cardSuspect.setStrokeWidth(3); // thick visual highlight
            holder.binding.cardSuspect.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0x22F05454)); // overlay fill
            holder.binding.btnCastVote.setText("SELECTED");
            holder.binding.btnCastVote.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            holder.binding.btnCastVote.setTextColor(0xFF0F0F0F);
        } else {
            holder.binding.cardSuspect.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFF343D4B)); // divider_line
            holder.binding.cardSuspect.setStrokeWidth(1);
            holder.binding.cardSuspect.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF222831)); // dark_surface_card
            holder.binding.btnCastVote.setText("SELECT");
            holder.binding.btnCastVote.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF05454));
            holder.binding.btnCastVote.setTextColor(0xFFFFFFFF);
        }

        // Entire card is clickable to select suspect easily (48dp target-friendly)
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                SoundManager.getInstance(v.getContext()).playClick();
                
                if (selectedPosition == currentPos) {
                    // Tap again to deselect
                    selectedPosition = -1;
                } else {
                    selectedPosition = currentPos;
                }
                
                notifyDataSetChanged();
                
                if (listener != null) {
                    listener.onSuspectSelected(selectedPosition != -1 ? suspect : null);
                }
            }
        };

        holder.binding.cardSuspect.setOnClickListener(clickListener);
        holder.binding.btnCastVote.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return suspects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemVotingSuspectBinding binding;

        public ViewHolder(ItemVotingSuspectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
