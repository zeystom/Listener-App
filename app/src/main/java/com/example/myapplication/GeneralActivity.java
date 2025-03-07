package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityGeneralBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralActivity extends AppCompatActivity {

    private ActivityGeneralBinding binding;
    private RecyclerView recyclerViewChats;
    private RecyclerView recyclerViewFindUsers;
    private ChatAdapter chatAdapter;
    private FindUserAdapter findUserAdapter;
    private List<Chat> chatList;
    private List<FindUser> findUserList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        binding = ActivityGeneralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (false) {
                // Handle "Profile" click
            } else if (false) {
                // Handle "Settings" click
            } else if (id == R.id.nav_sign_out) {
                mAuth.signOut();
                startActivity(new Intent(GeneralActivity.this, MainActivity.class));
                finish();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Setup chats RecyclerView
        recyclerViewChats = binding.recyclerViewChats;
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(GeneralActivity.this, CompanionChat.class);
            intent.putExtra("USER_ID", chat.getUid());
            startActivity(intent);
        });
        recyclerViewChats.setAdapter(chatAdapter);


        recyclerViewFindUsers = binding.listViewGen;
        findUserList = new ArrayList<>();
        findUserAdapter = new FindUserAdapter(findUserList, user -> {
            Intent intent = new Intent(GeneralActivity.this, CompanionChat.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        });
        recyclerViewFindUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFindUsers.setAdapter(findUserAdapter);
        recyclerViewFindUsers.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchChatsFromFirestore();
    }

    private void fetchChatsFromFirestore() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        firestore.collection("chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatList.clear();
                    Set<String> addedUserIds = new HashSet<>();

                    for (DocumentSnapshot chatDoc : queryDocumentSnapshots) {
                        String firstUserId = chatDoc.getString("firstUserId");
                        String secondUserId = chatDoc.getString("secondUserId");

                        if (!currentUserId.equals(firstUserId) && !currentUserId.equals(secondUserId)) {
                            continue;
                        }

                        String otherUserId = currentUserId.equals(firstUserId) ? secondUserId : firstUserId;

                        if (addedUserIds.contains(otherUserId)) {
                            continue;
                        }
                        addedUserIds.add(otherUserId);

                        firestore.collection("users")
                                .document(otherUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String name = userDoc.getString("username");
                                        String pfp = userDoc.getString("pfp");

                                        chatDoc.getReference()
                                                .collection("messages")
                                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(messagesSnapshot -> {
                                                    String lastMessage = "";
                                                    if (!messagesSnapshot.isEmpty()) {
                                                        DocumentSnapshot lastMessageDoc = messagesSnapshot.getDocuments().get(0);
                                                        lastMessage = lastMessageDoc.getString("text");
                                                    }

                                                    Chat chat = new Chat(otherUserId, pfp, name, lastMessage);
                                                    chatList.add(chat);
                                                    chatAdapter.notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e ->
                                                        Log.e("GeneralActivity", "Error getting last message: ", e)
                                                );
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.e("GeneralActivity", "Error getting user info: ", e)
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("GeneralActivity", "Error getting chats: ", e)
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");

        searchView.setOnCloseListener(() -> {
            recyclerViewFindUsers.setVisibility(View.GONE);
            return false;
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                recyclerViewFindUsers.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 3) {
                    findUserList.clear();
                    searchUsers(newText);
                    recyclerViewFindUsers.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewFindUsers.setVisibility(View.GONE);
                }
                return false;
            }
        });

        return true;
    }

    private void searchUsers(String query) {
        firestore.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                FindUser user = document.toObject(FindUser.class);
                                if (user != null) {
                                    user.setUid(document.getId());
                                    findUserList.add(user);
                                }
                            }
                            findUserAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("GeneralActivity", "No users found.");
                        }
                    } else {
                        Log.d("GeneralActivity", "Error getting documents: ", task.getException());
                    }
                });
    }
}
