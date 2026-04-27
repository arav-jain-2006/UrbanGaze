package com.urbangaze.app.ui.buddies;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.urbangaze.app.R;
import com.urbangaze.app.model.MatchUser;

import java.util.ArrayList;
import java.util.List;

public class BuddiesAdapter extends RecyclerView.Adapter<BuddiesAdapter.ViewHolder> {
    private List<MatchUser> list = new ArrayList<>();

    public void submitList(List<MatchUser> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_buddy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchUser user = list.get(position);

        holder.name.setText(user.name);
        holder.place.setText(user.placeName);
        holder.date.setText(BuddiesFragment.formatDateRange(user.startDate, user.endDate));

        holder.connectBtn.setOnClickListener(v -> {
            String email = user.email;

            String subject = "UrbanGaze: Trip to " + user.placeName;

            String body = "Hello " + user.name + ",\n\n"
                    + "I came across your travel plan for " + user.placeName + " ("
                    + BuddiesFragment.formatDateRange(user.startDate, user.endDate) + "). "
                    + "I'm planning something similar and thought we could connect!\n\n"
                    + "Thank you.";

            Intent intent = new Intent(Intent.ACTION_SENDTO);
//            intent.setData(Uri.parse("mailto:" + email));
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);

            try {
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, place, date;
        MaterialButton connectBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameText);
            place = itemView.findViewById(R.id.placeText);
            date = itemView.findViewById(R.id.dateText);
            connectBtn = itemView.findViewById(R.id.connectBtn);
        }
    }
}