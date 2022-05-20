package com.example.chatappfirebase.listeners;

import com.example.chatappfirebase.models.User;

public interface ConversationListener {
    void onConversationClicked(User user);
}
