package com.example.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemSetupPlayerBinding;
import com.example.model.Player;
import java.util.ArrayList;
import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.ViewHolder> {

    public interface OnPlayerDeleteListener {
        void onDeleteClick(int index);
    }

    private List<Player> playersList = new ArrayList<>();
    private final OnPlayerDeleteListener listener;

    public PlayersAdapter(OnPlayerDeleteListener listener) {
        this.listener = listener;
    }

    public void setPlayers(List<Player> newList) {
        this.playersList = newList != null ? newList : new ArrayList<Player>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSetupPlayerBinding binding = ItemSetupPlayerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = playersList.get(position);
        holder.binding.tvSetupPlayerNum.setText(String.valueOf(position + 1));
        holder.binding.tvSetupPlayerName.setText(player.getName());

        if (player.getName() != null && !player.getName().isEmpty()) {
            String initial = player.getName().substring(0, 1).toUpperCase();
            holder.binding.tvSetupPlayerAvatar.setText(initial);
        } else {
            holder.binding.tvSetupPlayerAvatar.setText("P");
        }

        holder.binding.btnDeletePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSetupPlayerBinding binding;

        public ViewHolder(ItemSetupPlayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
