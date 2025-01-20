package com.example.remaindergame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// Main Activity class
public class MainActivity extends AppCompatActivity {
    private int count = 0; // Counts the open cards
    private int card1 = -1; // First card index
    private int card2 = -1; // Second card index
    private int counterPlayer1 = 0; // Player 1 score
    private int counterPlayer2 = 0; // Player 2 score
    private boolean player1Turn = true; // Player 1's turn
    private final ImageView[] imageViewsArray = new ImageView[16]; // Array for card ImageViews
    private final Integer[] drawablesArray = new Integer[16]; // Array for card resources
    private final Handler handler = new Handler(); // For delays
    private TextView scoreTextView; // Score display

    private DatabaseReference databaseRef; // Firebase database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("game");

        // Add a sample user to Firebase
        addUserToDatabase("user1", new User("John", "john@example.com"));
        addUserToDatabase("user2", new User("Doe", "doe@example.com"));

        // Initialize score TextView
        scoreTextView = findViewById(R.id.scoreTextView);

        fillImageViewsArray(); // Fill card ImageViews array
        fillDrawablesArray(); // Fill card drawables array
        shuffleDrawablesArray(); // Shuffle card images

        // Set click listeners for all cards
        for (ImageView imageView : imageViewsArray) {
            imageView.setOnClickListener(this::onBtnClicked);
        }

        updateScoreDisplay(); // Display initial score
        updateDatabaseGameState();
    }

    // Add a user to Firebase
    private void addUserToDatabase(String userId, User user) {
        databaseRef.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "User " + userId + " added successfully.");
                    } else {
                        Log.e("Firebase", "Failed to add user: " + task.getException());
                    }
                });
    }


    // מטפל בלחיצות על קלפים
    public void onBtnClicked(View view) {
        ImageView clickedImageView = (ImageView) view; // המרה של תצוגה ל-ImageView

        int clickedIndex = -1; // מזהה של הקלף שנלחץ
        for (int i = 0; i < imageViewsArray.length; i++) {
            if (imageViewsArray[i] == clickedImageView) {
                clickedIndex = i; // מוצא את המיקום במערך
                break;
            }
        }

        // אם הקלף כבר נבחר, מתעלם מהלחיצה
        if (clickedImageView.getTag() != null && (boolean) clickedImageView.getTag()) {
            return;
        }

        clickedImageView.setImageResource(drawablesArray[clickedIndex]); // מציג את התמונה המתאימה לקלף
        clickedImageView.setTag(true); // מסמן שהקלף נבחר
        logCardFlip(clickedIndex);

        if (count == 0) { // אם זה הקלף הראשון שנבחר
            card1 = clickedIndex; // שומר את מיקומו
            count++;
        } else if (count == 1) { // אם זה הקלף השני שנבחר
            card2 = clickedIndex; // שומר את מיקומו
            count++;
            handler.postDelayed(this::turnEnd, 1000); // מעכב את הבדיקה בשנייה אחת
        }
    }

    // ממלא את מערך התצוגות של התמונות
    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName()); // מוצא את מזהה התצוגה
            imageViewsArray[i] = findViewById(imageViewId);
        }
    }

    // ממלא את מערך מזהי התמונות
    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName()); // מוצא את מזהה התמונה
            drawablesArray[i] = drawableId;
        }
    }

    // מערבב את מערך התמונות
    private void shuffleDrawablesArray() {
        List<Integer> drawablesList = Arrays.asList(drawablesArray); // ממיר למבנה רשימה
        Collections.shuffle(drawablesList); // מערבב את הרשימה
        drawablesList.toArray(drawablesArray); // ממיר חזרה למערך
    }

    // מטפל בסיום התור
    private void turnEnd() {
        if (drawablesArray[card1].equals(drawablesArray[card2])) { // אם הקלפים זהים
            if (player1Turn) {
                counterPlayer1++; // מוסיף נקודה לשחקן 1
            } else {
                counterPlayer2++; // מוסיף נקודה לשחקן 2
            }

            logMatch();

            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show(); // מציג הודעה שהשחקן צדק

            if (counterPlayer1 + counterPlayer2 == 8) { // בדיקה האם כל הקלפים נחשפו (8 זוגות במשחק)
                Intent intent = new Intent(MainActivity.this, EndScreen.class); // יצירת Intent למעבר למסך הסיום
                intent.putExtra("player1Score", counterPlayer1); // העברת הניקוד של שחקן 1 ל-Intent
                intent.putExtra("player2Score", counterPlayer2); // העברת הניקוד של שחקן 2 ל-Intent
                String winner = counterPlayer1 > counterPlayer2 ? "Player 1" : "Player 2"; // קביעת המנצח (השחקן עם הניקוד הגבוה יותר)
                intent.putExtra("winner", winner); // העברת שם המנצח ל-Intent
                startActivity(intent); // התחלת הפעולה למעבר למסך הסיום
                return; // סיום הפונקציה (כדי שלא יבוצע קוד נוסף אחר כך)
            }

        } else { // אם הקלפים לא תואמים
            Toast.makeText(this, "Turn end", Toast.LENGTH_SHORT).show(); // מציג הודעה שתור הסתיים
            imageViewsArray[card1].setImageResource(R.drawable.back32); // מחזיר את התמונה לגב הקלף
            imageViewsArray[card2].setImageResource(R.drawable.back32);
            imageViewsArray[card1].setTag(false); // מסמן שהקלף לא נבחר
            imageViewsArray[card2].setTag(false);

            player1Turn = !player1Turn; // מעביר את התור לשחקן השני
        }

        updateScoreDisplay(); // מעדכן את תצוגת התוצאות
        updateDatabaseGameState();

        count = 0; // מאפס את ספירת הקלפים
        card1 = -1;
        card2 = -1;
    }

    // מעדכן את תצוגת התוצאות
    private void updateScoreDisplay() {
        String scoreText = "Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2;
        scoreTextView.setText(scoreText);
    }

    // מעדכן את מצב המשחק במסד הנתונים של Firebase
    private void updateDatabaseGameState() {
        // יוצר אובייקט HashMap שיכיל את מצב המשחק
        HashMap<String, Object> gameState = new HashMap<>();
        gameState.put("player1Score", counterPlayer1); // מוסיף את הניקוד של שחקן 1
        gameState.put("player2Score", counterPlayer2); // מוסיף את הניקוד של שחקן 2
        gameState.put("playerTurn", player1Turn ? "Player 1" : "Player 2"); // קובע למי התור כרגע
        gameState.put("cardsFlipped", count); // מוסיף את מספר הקלפים שנחשפו

        // מעדכן את מסד הנתונים עם מצב המשחק
        databaseRef.child("gameState").setValue(gameState)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Game state updated."); // רושם לוג במקרה של הצלחה
                    } else {
                        Log.e("Firebase", "Failed to update game state: " + task.getException()); // רושם לוג במקרה של כשלון
                    }
                });
    }

    // מתעד במסד הנתונים קלף שנחשף
    private void logCardFlip(int cardIndex) {
        // מוסיף רשומה חדשה ביומן המשחק עם מידע על הקלף שנחשף ומי חשף אותו
        databaseRef.child("gameLog").push().setValue("Card flipped: " + cardIndex + " by " +
                (player1Turn ? "Player 1" : "Player 2"));
    }

    // מתעד במסד הנתונים מתי נמצאה התאמה
    private void logMatch() {
        // מוסיף רשומה חדשה ביומן המשחק שמציינת מי מצא התאמה
        databaseRef.child("gameLog").push().setValue("Match found by " +
                (player1Turn ? "Player 1" : "Player 2"));
    }
    // מתעד את תוצאות המשחק במסד הנתונים
    private void logEndGame() {
        // יוצר אובייקט HashMap שיכיל את תוצאות המשחק
        HashMap<String, Object> gameResult = new HashMap<>();
        gameResult.put("winner", counterPlayer1 > counterPlayer2 ? "Player 1" : "Player 2"); // מוסיף את שם המנצח
        gameResult.put("player1Score", counterPlayer1); // מוסיף את הניקוד של שחקן 1
        gameResult.put("player2Score", counterPlayer2); // מוסיף את הניקוד של שחקן 2

        // מעדכן את מסד הנתונים עם תוצאות המשחק
        databaseRef.child("gameResult").setValue(gameResult)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Game result saved."); // רושם לוג במקרה של הצלחה
                    } else {
                        Log.e("Firebase", "Failed to save game result: " + task.getException()); // רושם לוג במקרה של כשלון
                    }
                });
    }
        }





