package com.example.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemResultsRoleBinding;
import com.example.model.Player;
import java.util.ArrayList;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private List<Player> players = new ArrayList<>();

    public void setPlayers(List<Player> newList) {
        this.players = newList != null ? newList : new ArrayList<Player>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemResultsRoleBinding binding = ItemResultsRoleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.binding.tvResultsPlayerName.setText(player.getName());
        holder.binding.tvResultsSecretWord.setText("Secret Word: " + player.getSecretWord());

        String role = player.getRole();
        holder.binding.tvResultsRoleBadge.setText(role.toUpperCase());

        if ("Imposter".equals(role)) {
            holder.binding.tvResultsRoleBadge.setTextColor(Color.WHITE);
            holder.binding.tvResultsRoleBadge.setBackgroundColor(0xFFEC407A); // Crimson Hotpink
        } else {
            holder.binding.tvResultsRoleBadge.setTextColor(Color.WHITE);
            holder.binding.tvResultsRoleBadge.setBackgroundColor(0xFF66BB6A); // Lime green
        }

        if (player.isVotedOut()) {
            holder.binding.tvVotedOutIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvVotedOutIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemResultsRoleBinding binding;

        public ViewHolder(ItemResultsRoleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
