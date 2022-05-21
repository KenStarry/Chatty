package com.example.chatappfirebase.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappfirebase.databinding.ItemContainerReceivedMessageBinding;
import com.example.chatappfirebase.databinding.ItemContainerSentMessageBinding;
import com.example.chatappfirebase.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessageList;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    //  Adapter constructor
    public ChatAdapter(List<ChatMessage> chatMessageList, Bitmap receiverProfileImage, String senderId) {
        this.chatMessageList = chatMessageList;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //  We need to get the bounded view
        if (viewType == VIEW_TYPE_SENT) {
            //  Return the message that has been sent
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            //  Return the message that has been received
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //  Setting the data to our model
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    //  Get the returned view type from the list of chats
    @Override
    public int getItemViewType(int position) {

        if (chatMessageList.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    //  Sent Message Class
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        //  Binding for out itemsent container
        private final ItemContainerSentMessageBinding binding;

        //  Constructor
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        //  Using our ChatMessage Model to set our data
        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    //  Sent Message Class
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        //  Binding for the itemreceived chats
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        //  Set Data for the received message using our model and our Bitmap
        void setData(ChatMessage chatMessage, Bitmap receivedProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

            if (receivedProfileImage != null) {
                binding.imageProfile.setImageBitmap(receivedProfileImage);
            }
        }
    }



}
