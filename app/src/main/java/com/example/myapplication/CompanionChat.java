package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityCompanionChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanionChat extends AppCompatActivity {

    private static final String TAG = "CompanionChat";
    private static final String FIELD_CHAT_IDS = "chatIds";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_FIRST_USER_ID = "firstUserId";
    private static final String FIELD_SECOND_USER_ID = "secondUserId";

    private ActivityCompanionChatBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String firstUserId;
    private String secondUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding = ActivityCompanionChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        secondUserId = getIntent().getStringExtra("USER_ID");
        firstUserId = mAuth.getUid();

        if (firstUserId == null || secondUserId == null) {
            Log.e(TAG, "User IDs are not available.");
            finish();
            return;
        }

        loadCompanionName();

        List<String> participants = Arrays.asList(firstUserId, secondUserId);
        Collections.sort(participants);
        chatId = participants.get(0) + "_" + participants.get(1);

        ensureChatDocument();
        setupChatRecyclerView();

        binding.sendButton.setOnClickListener(v -> {
            String messageText = binding.inputField.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                binding.inputField.setText("");
            }
        });
    }

    private void loadCompanionName() {
        db.collection("users").document(secondUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString(FIELD_USERNAME);
                        binding.companionName.setText(name != null ? name : "Unknown");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching companion name", e));
    }

    private void ensureChatDocument() {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put(FIELD_FIRST_USER_ID, firstUserId);
                        chatData.put(FIELD_SECOND_USER_ID, secondUserId);
                        chatRef.set(chatData)
                                .addOnSuccessListener(aVoid -> {
                                    addChatIdToUser(firstUserId);
                                    addChatIdToUser(secondUserId);
                                })
                                .addOnFailureListener(e -> Log.e("TAG", "Error creating chat document", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking chat document", e));
    }

    private void addChatIdToUser(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update(FIELD_CHAT_IDS, FieldValue.arrayUnion(chatId))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating chatIds for user: " + userId, e));
    }

    private void setupChatRecyclerView() {
        RecyclerView chatRecyclerView = binding.chatRecyclerView;
        List<Message> messageList = new ArrayList<>();
        MessageAdapter chatAdapter = new MessageAdapter(firstUserId, messageList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Message> messages = new ArrayList<>();
                        querySnapshot.getDocuments().forEach(doc -> {
                            Message msg = doc.toObject(Message.class);
                            if (msg != null) {
                                messages.add(msg);
                            }
                        });
                        chatAdapter.setMessages(messages);
                        // Smooth scroll to the latest message
                        if (!messages.isEmpty()) {
                            chatRecyclerView.post(() -> chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
                        }
                    }
                });
    }

    private void sendMessage(String text) {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        // Directly add the message to the messages subcollection
        addMessageToSubcollection(chatRef, firstUserId, text);
    }

    private void addMessageToSubcollection(DocumentReference chatRef, String senderId, String text) {
        CollectionReference messagesRef = chatRef.collection("messages");

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);
        messageData.put("senderId", senderId);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        messagesRef.add(messageData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Message sent successfully"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }
}
