package com.example.remaindergame;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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

// מחלקת הפעילות הראשית של המשחק
public class MainActivity extends AppCompatActivity {
    private Boolean isGameOn = FALSE;
    private int whoAmI = 1;
    private int count = 0; // מונה כמות הקלפים הפתוחים כרגע
    private int card1 = -1; // אינדקס הקלף הראשון שנבחר
    private int card2 = -1; // אינדקס הקלף השני שנבחר
    private int counterPlayer1 = 0; // ניקוד של שחקן 1
    private int counterPlayer2 = 0; // ניקוד של שחקן 2
    private boolean player1Turn = true; // תור שחקן 1 (אם שקר, זה תור שחקן 2)
    private final ImageView[] imageViewsArray = new ImageView[16]; // מערך לכל הקלפים (תצוגה)
    private final Integer[] drawablesArray = new Integer[16]; // מערך לכל הקלפים (משאבים)
    private final Handler handler = new Handler(); // מנהל עבור השהיות
    private TextView scoreTextView; // תצוגת הטקסט להצגת הניקוד
    private static final String TAG = "MainActivity";

    private DatabaseReference databaseRef; // הפניה למסד הנתונים של Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // קביעת ממשק המשתמש עבור הפעילות

        // אתחול מסד הנתונים של Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("game"); // הגדרה של הנתיב במשחק

        // הוספת משתמשים לדוגמה למסד הנתונים
        addUserToDatabase("user1", new User("John", "john@example.com"));
        addUserToDatabase("user2", new User("Doe", "doe@example.com"));

        // אתחול תצוגת הניקוד
        scoreTextView = findViewById(R.id.scoreTextView);

        fillImageViewsArray(); // מילוי המערך של הקלפים
        fillDrawablesArray(); // מילוי המערך של תמונות הקלפים


        databaseRef.child("cardOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isGameOn) {

                    //task 1: clear database


                    //set isGameOn to TRUE
                    isGameOn = TRUE;


                    //task 2. set isGameOn to TRUE also in Database

                    // השחקן הראשון - שומר את סדר הקלפים ב-Firebase
                    List<Integer> drawablesList = Arrays.asList(drawablesArray);
                    Collections.shuffle(drawablesList);
                    drawablesList.toArray(drawablesArray);

                    databaseRef.child("cardOrder").setValue(drawablesList)
                            .addOnSuccessListener(aVoid -> Log.d(" Firebase", "First player - card order saved."))
                            .addOnFailureListener(e -> Log.e("Firebase", "Failed to save card order: " + e.getMessage()));
                    whoAmI = 1;
                    Log.d("whoAmI", "first player");

                } else {
                    // השחקן השני - מוריד את הסדר של הקלפים
                    int i = 0;
                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                        drawablesArray[i] = cardSnapshot.getValue(Integer.class);
                        i++;
                    }
                    Log.d("Firebase", "Second player - card order loaded.");
                    whoAmI = 2;
                    Log.d("whoAmI", "Second player");


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load card order: " + error.getMessage());
            }
        });



        shuffleDrawablesArray(); // ערבוב תמונות הקלפים

        // קביעת מאזין לחיצה לכל קלף
        for (ImageView imageView : imageViewsArray) {
            imageView.setOnClickListener(this::onBtnClicked);
        }

        updateScoreDisplay(); // עדכון תצוגת הניקוד בתחילת המשחק
        updateBoardState(); // עדכון מצב הלוח בתחילת המשחק
        updateDatabaseGameState(); // עדכון מצב המשחק במסד הנתונים
        addRealtimeListeners();

        // Read from the database
        databaseRef.child("isGameOn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isGameOn = snapshot.getValue(Boolean.class);
                if (isGameOn != null && isGameOn) {
                    // המשחק כבר התחיל, שחקן מצטרף למשחק קיים
                    Log.d(TAG, "Game is already running, joining...");
                } else {
                    // המשחק עדיין לא התחיל, המשתמש הנוכחי מתחיל אותו
                    databaseRef.child("isGameOn").setValue(true);
                    Log.d(TAG, "Starting a new game...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read isGameOn: " + error.getMessage());
            }
        });

        databaseRef.child("boardState").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // השחקן הראשון - שמור את הלוח שלו
                    updateBoardState();
                    Log.d("Firebase", "First player - board state saved.");
                } else {
                    Log.d("Firebase", "Board state already exists.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to check board state: " + error.getMessage());
            }
        });


    }


    // פונקציה להאזנה בזמן אמת למסד הנתונים
    private void addRealtimeListeners() {
        // מאזין למצב הלוח
        Log.d("XXX","addRealtimeListeners");
        databaseRef.child("boardState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("XXXX","onDataChange");
                // עדכון הלוח במכשיר הנוכחי לפי מצב Firebase
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    int position = cardSnapshot.child("position").getValue(Integer.class);
                    boolean isFlipped = cardSnapshot.child("isFlipped").getValue(Boolean.class);
                    Log.e("XXXX", "position = " + position );
                    Log.e("XXXX", "isFlipped = " + isFlipped );

                    if (isFlipped) {
                        imageViewsArray[position].setImageResource(drawablesArray[position]);
                        imageViewsArray[position].setTag(true);
                    } else {
                        imageViewsArray[position].setImageResource(R.drawable.back32);
                        imageViewsArray[position].setTag(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read board state: " + error.getMessage());
            }
        });

        // מאזין למצב המשחק
        databaseRef.child("gameState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                counterPlayer1 = snapshot.child("player1Score").getValue(Integer.class);
                counterPlayer2 = snapshot.child("player2Score").getValue(Integer.class);
                player1Turn = "Player 1".equals(snapshot.child("playerTurn").getValue(String.class));

                updateScoreDisplay(); // עדכון תצוגת הניקוד
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read game state: " + error.getMessage());
            }
        });

        databaseRef.child("boardState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                        int position = cardSnapshot.child("position").getValue(Integer.class);
                        boolean isFlipped = cardSnapshot.child("isFlipped").getValue(Boolean.class);

                        if (isFlipped) {
                            imageViewsArray[position].setImageResource(drawablesArray[position]);
                            imageViewsArray[position].setTag(true);
                        } else {
                            imageViewsArray[position].setImageResource(R.drawable.back32);
                            imageViewsArray[position].setTag(false);
                        }
                    }
                    Log.d("Firebase", "Board state loaded for new player.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load board state: " + error.getMessage());
            }
        });


    }


    // פונקציה להוספת משתמש למסד הנתונים
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

    private boolean isMyTurn()
    {
        Log.e("isMyTurn", "whoAmI = " + whoAmI);
        Log.e("isMyTurn", "player1Turn = " + player1Turn);
        return (whoAmI == 1 && player1Turn == TRUE) || (whoAmI == 2 && player1Turn == FALSE);
    }
    // פונקציה שמטפלת בלחיצה על קלף
    public void onBtnClicked(View view) {

        //if not my turn do nothing
        if(!isMyTurn())
        {
            Toast.makeText(this, "Not your turn...", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView clickedImageView = (ImageView) view;

        // מציאת אינדקס הקלף שנלחץ
        int clickedIndex = -1;
        for (int i = 0; i < imageViewsArray.length; i++) {
            if (imageViewsArray[i] == clickedImageView) {
                clickedIndex = i;
                break;
            }
        }

        // בדיקה אם הקלף כבר נחשף
        if (clickedImageView.getTag() != null && (boolean) clickedImageView.getTag()) {
            return;
        }

        // חשיפת הקלף
        clickedImageView.setImageResource(drawablesArray[clickedIndex]);
        clickedImageView.setTag(true); // סימון הקלף כ"נחשף"
        logCardFlip(clickedIndex); // עדכון במסד הנתונים על חשיפת קלף
        updateBoardState(); // עדכון מצב הלוח

        // ניהול קלף ראשון ושני שנבחרו
        if (count == 0) {
            card1 = clickedIndex;
            count++;
        } else if (count == 1) {
            card2 = clickedIndex;
            count++;
            handler.postDelayed(this::turnEnd, 1000); // השהיה לפני סיום התור
        }
    }

    // מילוי מערך תצוגת הקלפים (ImageViews)
    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName());
            imageViewsArray[i] = findViewById(imageViewId);

            // לוג ERROR כדי להדגיש את המידע ב-Logcat
            Log.e("ImageViewArray", "Index: " + i + ", View ID: car" + (i + 1) + ", Object: " + imageViewsArray[i]);
        }
    }

    // מילוי מערך המשאבים של הקלפים (drawables)
    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName());
            drawablesArray[i] = drawableId;

            // לוג ERROR כדי להדגיש את ה-Drawable שנבחר
            Log.e("DrawablesArray", "Index: " + i + ", Drawable ID: img_" + ((i % 8) + 1) + ", Resource ID: " + drawableId);
        }
    }

    // ערבוב הקלפים עם לוגים
    private void shuffleDrawablesArray() {
        List<Integer> drawablesList = Arrays.asList(drawablesArray);
        Collections.shuffle(drawablesList);
        drawablesList.toArray(drawablesArray);

        // לוג ERROR כדי לראות את הסדר של הקלפים לאחר ערבוב
        Log.e("Shuffle", "Shuffled DrawablesArray: " + Arrays.toString(drawablesArray));
    }


    // סיום תור
    private void turnEnd() {
        Log.e("XXXXX", "player1Turn = " + player1Turn);
        Toast.makeText(this, "Turn end", Toast.LENGTH_SHORT).show();

        // if there is a match
        if (drawablesArray[card1].equals(drawablesArray[card2])) {
            //get who's turn from DB
            if (player1Turn) {
                counterPlayer1++; // עדכון ניקוד שחקן 1
            } else {
                counterPlayer2++; // עדכון ניקוד שחקן 2
            }

            logMatch(); // רישום הצלחה במסד הנתונים
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();

            // בדיקת סיום המשחק
            if (counterPlayer1 + counterPlayer2 == 8) {

                isGameOn = FALSE;
                //task 3. set in Database isGameOn to FALSE

                Intent intent = new Intent(MainActivity.this, EndScreen.class);
                intent.putExtra("player1Score", counterPlayer1);
                intent.putExtra("player2Score", counterPlayer2);
                String winner = counterPlayer1 > counterPlayer2 ? "Player 1" : "Player 2";
                intent.putExtra("winner", winner);
                startActivity(intent);
                return;
            }
        } else {
            // החזרת הקלפים למצב מוסתר אם לא נמצא זוג
            imageViewsArray[card1].setImageResource(R.drawable.back32);
            imageViewsArray[card2].setImageResource(R.drawable.back32);
            imageViewsArray[card1].setTag(false);
            imageViewsArray[card2].setTag(false);

            //write to DB
            player1Turn = !player1Turn; // העברת התור לשחקן השני
        }

        updateScoreDisplay(); // עדכון תצוגת הניקוד
        updateBoardState(); // עדכון מצב הלוח
        updateDatabaseGameState(); // עדכון מצב המשחק במסד הנתונים

        count = 0;
        card1 = -1;
        card2 = -1;
    }

    // פונקציה לעדכון תצוגת הניקוד
    private void updateScoreDisplay() {
        Log.e("XXXX","+updateScoreDisplay");
        String scoreText = "Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2;
        scoreTextView.setText(scoreText);
        Log.e("XXXX","-updateScoreDisplay");

    }

    // פונקציה לעדכון מצב הלוח במסד הנתונים
    private void updateBoardState() {
        HashMap<String, Object> boardState = new HashMap<>();

        for (int i = 0; i < imageViewsArray.length; i++) {
            HashMap<String, Object> cardState = new HashMap<>();
            cardState.put("imageId", drawablesArray[i]); // מזהה התמונה של הקלף
            cardState.put("isFlipped", imageViewsArray[i].getTag() != null && (boolean) imageViewsArray[i].getTag());
            cardState.put("position", i); // המיקום של הקלף בלוח

            boardState.put("card" + i, cardState);
        }

        databaseRef.child("boardState").setValue(boardState)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Board state updated.");
                    } else {
                        Log.e("Firebase", "Failed to update board state: " + task.getException());
                    }
                });
    }

    // פונקציה לעדכון מצב המשחק במסד הנתונים
    private void updateDatabaseGameState() {
        HashMap<String, Object> gameState = new HashMap<>();
        gameState.put("player1Score", counterPlayer1);
        gameState.put("player2Score", counterPlayer2);
        gameState.put("playerTurn", player1Turn ? "Player 1" : "Player 2");
        gameState.put("cardsFlipped", count);

        databaseRef.child("gameState").setValue(gameState)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Game state updated.");
                    } else {
                        Log.e("Firebase", "Failed to update game state: " + task.getException());
                    }
                });
    }

    // רישום חשיפת קלף במסד הנתונים
    private void logCardFlip(int cardIndex) {
        String text = "Card flipped: " + cardIndex + " by " +
                (player1Turn ? "Player 1" : "Player 2");
       // Log.e("XXXXXX", text);

        databaseRef.child("gameLog").push().setValue(text);
    }

    // רישום התאמה שנמצאה במסד הנתונים
    private void logMatch() {
        databaseRef.child("gameLog").push().setValue("Match found by " +
                (player1Turn ? "Player 1" : "Player 2"));
    }
}