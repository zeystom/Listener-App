package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;


public class CompanionChat extends AppCompatActivity {

    private TextView companionName;
    private ImageView companionImage;
    private EditText inputField;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companion_chat);

        // Initialize UI components
        companionName = findViewById(R.id.companionName);
        companionImage = findViewById(R.id.companionImage);
        inputField = findViewById(R.id.inputField);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Set companion name and image (you can load images dynamically using libraries like Glide or Picasso)
        companionName.setText("Companion Name");
        companionImage.setImageResource(R.drawable.logout); // replace with your image resource

        // Setup chatRecyclerView adapter and layout manager here
        // Setup sendButton onClickListener to handle sending messages
    }
}