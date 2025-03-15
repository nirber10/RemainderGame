package com.example.remaindergame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // הגדרת מערכת ליצירת Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // הצגת Toast לברוך הבא
        Toast.makeText(this, "ברוך הבא להגדרות!", Toast.LENGTH_SHORT).show();

        // הצגת חוקי המשחק
        String gameRules = "חוקי המשחק:\n\n"
                + "1. המשחק כולל לוח עם קלפים הפוכים.\n"
                + "2. המטרה היא למצוא זוגות של קלפים תואמים.\n"
                + "3. כל שחקן בתורו יכול להחשוף שני קלפים.\n"
                + "4. אם הקלפים תואמים, הם נשארים הפוכים והשחקן נשאר בתורו.\n"
                + "5. אם הקלפים לא תואמים, הם יחזרו להיות הפוכים והתור עובר לשחקן הבא.\n"
                + "6. כל שחקן צובר ניקוד עבור כל זוג שנמצא.\n"
                + "7. השחקן עם הניקוד הגבוה ביותר מנצח!\n\n"
                + "בהצלחה!";

        TextView rulesTextView = findViewById(R.id.rulesTextView);
        rulesTextView.setText(gameRules);

        // כפתור חזרה למסך הראשי
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, OpenActivity.class);
            startActivity(intent);
            finish(); // סיום פעילות ההגדרות
        });

        // כפתור שינוי הרקע לירוק
        Button greenButton = findViewById(R.id.greenButton);
        greenButton.setOnClickListener(v -> {
            findViewById(R.id.main).setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        });

        // כפתור שינוי הרקע לכחול
        Button blueButton = findViewById(R.id.blueButton);
        blueButton.setOnClickListener(v -> {
            findViewById(R.id.main).setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        });
    }
}
