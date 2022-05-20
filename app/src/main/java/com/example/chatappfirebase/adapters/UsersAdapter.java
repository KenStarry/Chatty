package com.example.chatappfirebase.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappfirebase.databinding.ItemContainerUserBinding;
import com.example.chatappfirebase.listeners.UserListener;
import com.example.chatappfirebase.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private final List<User> usersList;
    //  Listener for users during chatting
    private final UserListener userListener;

    public UsersAdapter(List<User> usersList, UserListener userListener) {

        this.usersList = usersList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We want to inflate our binded item layout file
        ItemContainerUserBinding item_row = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(item_row);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserData(usersList.get(position));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        //  Binding for our item container class
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        //  Setting the user data
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            //  Passing the user to the interface
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    //  get user images using Bitmap
    public Bitmap getUserImage(String encodedImage) {
        //  An array of Bytes containing the image String resource
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        //  decode the bytes array from the start to finish
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
