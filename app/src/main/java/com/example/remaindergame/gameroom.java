package com.example.remaindergame;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class gameroom extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private String roomId;
    private String playerId;
    private boolean isPlayer1;
    private int counterPlayer1 = 0, counterPlayer2 = 0;
    private boolean player1Turn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("gameRooms");

        findOrCreateGameRoom();
    }

    private void findOrCreateGameRoom() {
        databaseRef.orderByChild("player2").equalTo(null).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Join existing room
                            for (DataSnapshot room : snapshot.getChildren()) {
                                roomId = room.getKey();
                                playerId = "player2";
                                isPlayer1 = false;
                                databaseRef.child(roomId).child("player2").setValue("connected");
                                startListeningToGameState(); // Call it here
                                return;
                            }
                        } else {
                            // Create new room
                            roomId = databaseRef.push().getKey();
                            playerId = "player1";
                            isPlayer1 = true;

                            HashMap<String, Object> newRoom = new HashMap<>();
                            newRoom.put("player1", "connected");
                            newRoom.put("player2", null);
                            newRoom.put("playerTurn", "player1");
                            newRoom.put("player1Score", 0);
                            newRoom.put("player2Score", 0);
                            newRoom.put("boardState", new HashMap<String, Object>());

                            databaseRef.child(roomId).setValue(newRoom);
                            startListeningToGameState(); // Call it here
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error finding/creating room: " + error.getMessage());
                    }
                });
    }

    private void startListeningToGameState() {
        databaseRef.child(roomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // Update Player Scores
                counterPlayer1 = snapshot.child("player1Score").getValue(Integer.class);
                counterPlayer2 = snapshot.child("player2Score").getValue(Integer.class);
                player1Turn = snapshot.child("playerTurn").getValue(String.class).equals("player1");

                // Update UI
                updateScoreDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to sync game state: " + error.getMessage());
            }
        });
    }

    private void updateScoreDisplay() {
        //  Update UI elements to reflect scores
        Log.d("Game", "Player 1 Score: " + counterPlayer1 + ", Player 2 Score: " + counterPlayer2);
    }
}
