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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.myapplication.databinding.ActivityGeneralBinding;

import java.util.ArrayList;
import java.util.List;

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

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_profile) {
                    // Handle "Profile" click
                } else if (id == R.id.nav_settings) {
                    // Handle "Settings" click
                } else if (id == R.id.nav_sign_out) {
                    mAuth.signOut();
                    startActivity(new Intent(GeneralActivity.this, MainActivity.class));
                    finish();
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Set up RecyclerView for chats
        recyclerViewChats = binding.recyclerViewChats;
        recyclerViewFindUsers = binding.listViewGen;

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatList.add(new Chat("Bob", "Hi, its bob", "bob", "bob"));
        chatAdapter = new ChatAdapter(chatList);
        recyclerViewChats.setAdapter(chatAdapter);

        // Set up RecyclerView for FindUsers
        findUserList = new ArrayList<>();
        findUserAdapter = new FindUserAdapter(findUserList, new FindUserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(FindUser user) {
                Intent intent = new Intent(GeneralActivity.this, CompanionChat.class);
                intent.putExtra("USER_ID", user.getUid());
                startActivity(intent);
            }
        });
        recyclerViewFindUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFindUsers.setAdapter(findUserAdapter);
        recyclerViewFindUsers.setVisibility(View.GONE); // Hide initially
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerViewFindUsers.setVisibility(View.GONE); // Hide results when closed
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    recyclerViewFindUsers.setVisibility(View.GONE); // Hide results if focus lost
                }
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
                .orderBy("username") // Order results by username to allow efficient querying
                .startAt(query)
                .endAt(query + "\uf8ff") // To perform case-insensitive search
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                FindUser user = document.toObject(FindUser.class);
                                if (user != null) {
                                    Log.d("TAG", "searchUsers: " + user.username + " / " + user.getUsername() + " / " + document.getId());
                                    user.setUid(document.getId());
                                    findUserList.add(user); // Add user to the list
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
