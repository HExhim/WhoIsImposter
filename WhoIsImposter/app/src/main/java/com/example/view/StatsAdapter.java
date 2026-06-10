package com.example.view;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemStatsRecordBinding;
import com.example.data.GameRecord;
import java.util.ArrayList;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {

    private List<GameRecord> records = new ArrayList<>();

    public void setRecords(List<GameRecord> newList) {
        this.records = newList != null ? newList : new ArrayList<GameRecord>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStatsRecordBinding binding = ItemStatsRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRecord record = records.get(position);

        // Treat dates beautifully
        CharSequence dateStr = DateFormat.format("MMM dd, yyyy • hh:mm a", record.getDate());
        holder.binding.tvRecordDate.setText(dateStr);

        holder.binding.tvRecordMeta.setText("Mode: " + record.getMode() + " • Topic: " + record.getCategory());
        holder.binding.tvRecordDetail.setText("Voted Out: " + record.getVotedOutName() + " • Spies: " + record.getImposterNames());

        String winner = record.getWinner();
        holder.binding.tvRecordWinnerBadge.setText(winner.toUpperCase() + " WON");

        if ("Innocents".equals(winner)) {
            holder.binding.tvRecordWinnerBadge.setBackgroundColor(0xFFF05454); // Brand Red
            holder.binding.tvRecordWinnerBadge.setTextColor(Color.WHITE);
        } else {
            holder.binding.tvRecordWinnerBadge.setBackgroundColor(0xFFFFFFFF); // White contrast
            holder.binding.tvRecordWinnerBadge.setTextColor(0xFF0F0F0F);
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStatsRecordBinding binding;

        public ViewHolder(ItemStatsRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
