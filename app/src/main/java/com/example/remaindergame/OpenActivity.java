package com.example.remaindergame;

// יבוא מחלקות נדרשות
import android.content.Intent; // למחלקת Intent לצורך מעבר בין פעילויות
import android.os.Bundle; // למחלקת Bundle לצורך העברת נתונים בזמן יצירת Activity
import android.view.View; // למחלקת View לצורך הגדרת מאזינים לאירועים
import android.widget.Button; // למחלקת Button לצורך עבודה עם כפתורים

import androidx.activity.EdgeToEdge; // למחלקה EdgeToEdge לצורך פריסה מלאה של ממשק המשתמש
import androidx.appcompat.app.AppCompatActivity; // למחלקה AppCompatActivity המהווה בסיס ל-Activity
import androidx.core.graphics.Insets; // למחלקת Insets לעבודה עם קצוות מערכת
import androidx.core.view.ViewCompat; // למחלקה ViewCompat להתאמה לאחור של תצוגות
import androidx.core.view.WindowInsetsCompat; // למחלקה WindowInsetsCompat לניהול קצוות מערכת

// מחלקת OpenActivity המייצגת את מסך הפתיחה של האפליקציה
public class OpenActivity extends AppCompatActivity {

    // פונקציית onCreate שמופעלת עם יצירת ה-Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // קריאה לפונקציה של המחלקה העליונה
        EdgeToEdge.enable(this); // הפעלת מצב "קצה לקצה" לצורך פריסה מלאה של המסך
        setContentView(R.layout.activity_open); // הגדרת תצוגת המסך למסך הפתיחה

        // קביעת רווחים על פי קצוות המערכת
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // קבלת קצוות מערכת
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // הגדרת רווחים
            return insets; // החזרת הקצוות
        });

        // אתחול כפתורים
        Button startGameButton = findViewById(R.id.startGameButton); // מצביע לכפתור "Start Game"
        Button settingsButton = findViewById(R.id.settingsButton); // מצביע לכפתור "Settings"

        // הגדרת מאזין ללחיצה על כפתור "Start Game"
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenActivity.this, MainActivity.class); // יצירת Intent לעבור ל-MainActivity
                startActivity(intent); // מעבר לפעילות הראשית (MainActivity)
            }
        });

        // הגדרת מאזין ללחיצה על כפתור "Settings"
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenActivity.this, SettingsActivity.class); // יצירת Intent לעבור ל-SettingsActivity
                startActivity(intent); // מעבר למסך ההגדרות (SettingsActivity)
            }
        });
    }
}