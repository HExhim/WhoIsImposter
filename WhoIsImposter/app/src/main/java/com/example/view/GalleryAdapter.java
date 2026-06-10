package com.example.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemGalleryDrawingBinding;
import com.example.model.Player;
import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<Player> players = new ArrayList<>();

    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : new ArrayList<Player>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryDrawingBinding binding = ItemGalleryDrawingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.binding.tvGalleryPlayerName.setText(player.getName());

        if (player.getDrawing() != null) {
            holder.binding.ivGalleryBitmap.setImageBitmap(player.getDrawing());
        } else {
            holder.binding.ivGalleryBitmap.setImageBitmap(null);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemGalleryDrawingBinding binding;

        public ViewHolder(ItemGalleryDrawingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
