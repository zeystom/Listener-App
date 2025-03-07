package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityCompanionChatBinding; // Import the generated binding class
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanionChat extends AppCompatActivity {

    // Declare a binding object
    private ActivityCompanionChatBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String firstUserId;
    String secondUserId;
    String chatId;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Инициализация ViewBinding
        binding = ActivityCompanionChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        binding.companionImage.setImageResource(R.drawable.logout);
        String data = intent.getStringExtra("USER_ID");

        firstUserId = mAuth.getUid();
        secondUserId = data;

        // Получаем имя собеседника
        db.collection("users").document(secondUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        binding.companionName.setText(name);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("FirestoreError", "Error getting username", e);
                });

        // Формирование chatId на основе сортировки id пользователей
        List<String> participants = Arrays.asList(firstUserId, secondUserId);
        Collections.sort(participants);
        chatId = participants.get(0) + "_" + participants.get(1);
        DocumentReference chatRef = db.collection("chats").document(chatId);

        // Проверка существования чата (опционально)
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // Можно создать чат, если он не существует
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("firstuserid", firstUserId);
                chatData.put("seconduserid", secondUserId);
                chatRef.set(chatData);
            }
        });

        // Инициализация RecyclerView и адаптера
        RecyclerView chatRecyclerView = binding.chatRecyclerView;
        List<Message> messageList = new ArrayList<>();
        MessageAdapter chatAdapter = new MessageAdapter(firstUserId, messageList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Слушатель изменений для обновления списка сообщений
        chatRef.collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Message msg = doc.toObject(Message.class);
                            messages.add(msg);
                        }
                        chatAdapter.setMessages(messages);
                        if (!messages.isEmpty() && chatAdapter.getItemCount() > 0) {
                            chatRecyclerView.post(() -> {
                                int targetPos = chatAdapter.getItemCount() - 1;
                                if (targetPos >= 0) {
                                    chatRecyclerView.smoothScrollToPosition(targetPos);
                                }
                            });
                        }                }
                });

        // Обработчик нажатия кнопки отправки сообщения
        binding.sendButton.setOnTouchListener((v, event) -> {
            String messageText = binding.inputField.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(chatId, firstUserId, messageText);
                binding.inputField.setText(""); // Очистка поля ввода
            }
            return false;
        });
    }
    private void sendMessage(String chatId, String senderId, String text) {
        // 1) Build your chatRef
        DocumentReference chatRef = FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId);

        // 2) Check if it exists
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // 3) Create the chat doc if it doesn’t exist
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("firstuserid", firstUserId);
                chatData.put("seconduserid", secondUserId);

                chatRef.set(chatData).addOnSuccessListener(unused -> {
                    // 4) Now that the chat doc exists, add the message
                    addMessageToSubcollection(chatRef, senderId, text);
                });
            } else {
                // 5) If the doc already exists, just add the new message
                addMessageToSubcollection(chatRef, senderId, text);
            }
        });
    } private void addMessageToSubcollection(DocumentReference chatRef, String senderId, String text) {
        CollectionReference messagesRef = chatRef.collection("messages");

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);
        messageData.put("senderId", senderId);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        messagesRef.add(messageData)
                .addOnSuccessListener(docRef -> {
                    // Message added successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}
