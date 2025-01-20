package com.example.remaindergame;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// מחלקת מסך הסיום
public class EndScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen); // הגדרת תצוגת המסך למסך הסיום

        // קבלת תוצאות המשחק מתוך ה-Intent שהועבר ממסך המשחק
        int player1Score = getIntent().getIntExtra("player1Score", 0); // ניקוד של שחקן 1
        int player2Score = getIntent().getIntExtra("player2Score", 0); // ניקוד של שחקן 2
        String winner = getIntent().getStringExtra("winner"); // שם המנצח

        // מציג הודעת טוסט (Toast) למסך שמסך הסיום נטען
        Toast.makeText(this, "End Screen", Toast.LENGTH_SHORT).show();

        // איתור רכיבי TextView מתוך קובץ ה-XML של מסך הסיום
        TextView scoreTextView = findViewById(R.id.scoreTextView); // רכיב להצגת ניקוד השחקנים
        TextView winnerTextView = findViewById(R.id.winnerTextView); // רכיב להצגת המנצח

        // בדיקה האם רכיבי ה-TextView נמצאים בתצוגה
        if (scoreTextView != null && winnerTextView != null) {
            scoreTextView.setText("Player 1: " + player1Score + " - Player 2: " + player2Score); // הצגת התוצאה
            winnerTextView.setText("congratulations to the Winner: " + winner); // הצגת הודעת המנצח
        } else {
            // במקרה שרכיבי ה-TextView לא נמצאו, תוצג הודעת שגיאה
            Toast.makeText(this, "TextViews not found in the layout!", Toast.LENGTH_SHORT).show();
        }
    }
}
