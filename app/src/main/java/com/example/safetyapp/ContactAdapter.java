package com.example.safetyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter to show only trusted phone numbers
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<ContactModel> contactList;
    private Context context;

    // Constructor
    public ContactAdapter(Context context, List<ContactModel> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactNumber;

        public ViewHolder(View itemView) {
            super(itemView);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
        }
    }

    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the new item layout for better contrast and rounded background
        View view = LayoutInflater.from(context).inflate(R.layout.item_trusted_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        ContactModel contact = contactList.get(position);
        // Show only the phone number
        holder.tvContactNumber.setText(contact.getPhone());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}