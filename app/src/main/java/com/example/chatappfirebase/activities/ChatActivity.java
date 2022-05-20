package com.example.chatappfirebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.adapters.ChatAdapter;
import com.example.chatappfirebase.databinding.ActivityChatBinding;
import com.example.chatappfirebase.models.ChatMessage;
import com.example.chatappfirebase.models.User;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    //  The user user receiving the message
    private User receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    //  Id that will be used to get the current conversation
    private String conversationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    //  Initializing the adapters, arraylist etc
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    //  Method to send a message
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        //  Add this Hashmap to the database collection
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        //  Update the conversation so as to be shown in recents
        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {

            HashMap<String, Object> conversation = new HashMap<>();

            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());

            addConversationToDatabase(conversation);
        }

        //  Set the input text back to null
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    //  WHERE ALL THE MAGIC HAPPENS
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if (error != null) {
            return;
        }

        if (value != null) {
            int messageCount = chatMessageList.size();

            for (DocumentChange documentChange : value.getDocumentChanges()) {

                //  If we have added items to the database,
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessageList.add(chatMessage);
                }
            }

            //  Sorting the chats according to time received
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if (messageCount == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                //  Scroll to the position of the most recent chat
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        //  Check for the conversations
        if (conversationId == null) {
            checkForConversation();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //  Method to lad details of the user receiving the message
    private void loadReceiverDetails() {
        //  Get the intent information sent over when a user is clicked from the list of users
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        //  set the name of the user using activity binding
        binding.textName.setText(receiverUser.name);
    }

    //  Set Listeners for the various bounded views
    private void setListeners() {

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    //  Method to convert date into a simple format
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    //  Add the conversation to the Firestore Database collection
    private void addConversationToDatabase(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                //  On successful addition, we assign the conversation id
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    //  Update the conversation as users send messages
    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);

        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    //  Check for the conversation
    private void checkForConversation() {
        //  Have a recent conversation list only if there exists at least one message in the inbox
        if (chatMessageList.size() != 0) {
            //  Check for the conversation remotely
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            //  Do the same in reverse since maybe the sender can become the receiver
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    //  Variable to get the conversation id once a message is successfully sent
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {

        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            //  Get the id of the conversation
            conversationId = documentSnapshot.getId();
        }
    };
}