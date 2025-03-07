package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FindUserAdapter extends RecyclerView.Adapter<FindUserAdapter.FindUserHolder> {

    private final List<FindUser> userList;
    private final OnUserClickListener listener;

    public FindUserAdapter(List<FindUser> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FindUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_find_user, parent, false);
        return new FindUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindUserHolder holder, int position) {
        FindUser findUser = userList.get(position);
        Log.d("FindUserAdapter", "User Name: " + findUser.getUsername());
        holder.textViewName.setText(findUser.getUsername());

        // Обрабатываем клик по пользователю
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(findUser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class FindUserHolder extends RecyclerView.ViewHolder {
        TextView textViewName;

        public FindUserHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewNameFinder);
        }
    }

    // Интерфейс для обработки кликов
    public interface OnUserClickListener {
        void onUserClick(FindUser user);
    }
}
