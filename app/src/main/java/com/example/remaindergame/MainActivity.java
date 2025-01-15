package com.example.remaindergame;

// מחלקה ראשית של האפליקציה

import androidx.appcompat.app.AppCompatActivity; // מחלקה לניהול פעילות (Activity)
import android.content.Intent; // משמש לעבור בין מסכים
import android.os.Bundle; // משמש להעברת נתונים בין פעילויות
import android.os.Handler; // משמש להפעלת עיכוב בקוד
import android.util.Log; // משמש לרישום הודעות לוג
import android.view.View; // מייצג רכיב תצוגה באפליקציה
import android.widget.ImageView; // רכיב תצוגה להצגת תמונות
import android.widget.TextView; // רכיב תצוגה להצגת טקסט
import android.widget.Toast; // משמש להצגת הודעות קצרות על המסך

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays; // מספק כלים לעבודה עם מערכים
import java.util.Collections; // מספק כלים למיון וערבוב רשימות
import java.util.List; // מייצג רשימה

// מחלקת הפעילות הראשית
public class MainActivity extends AppCompatActivity {
    private int count = 0; // סופר את מספר הקלפים הפתוחים
    private int card1 = -1; // מזהה של הקלף הראשון שנבחר
    private int card2 = -1; // מזהה של הקלף השני שנבחר
    private int counterPlayer1 = 0; // נקודות של שחקן 1
    private int counterPlayer2 = 0; // נקודות של שחקן 2
    private boolean player1Turn = true; // מציין אם תורו של שחקן 1
    private final ImageView[] imageViewsArray = new ImageView[16]; // מערך לאחסון תמונות הקלפים
    private final Integer[] drawablesArray = new Integer[16]; // מערך לאחסון מזהי המשאבים (תמונות)
    private final Handler handler = new Handler(); // משמש לעיכוב
    private TextView scoreTextView; // רכיב להצגת תוצאות המשחק


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // קובע את הפריסה של המסך
        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

// Create a User object
        User user = new User("John", "john@example.com");

// Write data
        databaseReference.child("user1").setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Data written successfully.");
                    } else {
                        Log.d("Firebase", "Failed to write data: " + task.getException());
                    }
                });
        databaseReference.child("user1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("Firebase", "User Name: " + user.getName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });


        // אתחול תצוגת התוצאות
        scoreTextView = findViewById(R.id.scoreTextView);

        fillImageViewsArray(); // מילוי המערך של רכיבי התמונות
        fillDrawablesArray(); // מילוי המערך של מזהי התמונות
        shuffleDrawablesArray(); // ערבוב המערך של התמונות

        // קביעת מאזין לחיצה לכל תמונה
        for (ImageView imageView : imageViewsArray) {
            imageView.setOnClickListener(this::onBtnClicked);
        }

        // אתחול תצוגת התוצאות
        updateScoreDisplay();
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
        Log.e("xxxx", "line 59"); // רושם הודעת לוג
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

            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show(); // מציג הודעה שהשחקן צדק

            if (counterPlayer1 + counterPlayer2 == 8) { // אם כל הקלפים נחשפו
                Intent intent = new Intent(MainActivity.this, EndScreen.class); // מעבר למסך הסיום
                startActivity(intent);
                return;
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
        count = 0; // מאפס את ספירת הקלפים
        card1 = -1;
        card2 = -1;
    }

    // מעדכן את תצוגת התוצאות
    private void updateScoreDisplay() {
        String scoreText = "Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2;
        scoreTextView.setText(scoreText);
    }
}


