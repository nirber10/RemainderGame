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

import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// מחלקת הפעילות הראשית של המשחק
public class MainActivity extends AppCompatActivity {
    private boolean isGameOn = FALSE;
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

    public void resetGameData() {

        // מחיקת כל הנתונים של המשחק
        databaseRef.setValue(null) // פעולה זו מוחקת את כל הנתונים שנשמרו בנתיב הזה ב-Firebase Realtime Database.
                .addOnCompleteListener(task -> { // הקוד מוודא אם הפעולה הסתיימה בהצלחה או לא
                    if (task.isSuccessful()) { //  בדיקה האם הפעולה הצליחה – אם המחיקה בוצעה ללא בעיות, התנאי יתבצע.
                        System.out.println("הנתונים בפיירבייס אופסו בהצלחה!"); // הדפסה
                    } else {
                        System.err.println("שגיאה באיפוס הנתונים: " + task.getException()); // אם נכשל תדפיס שגיאה
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // קביעת ממשק המשתמש עבור הפעילות

        // אתחול מסד הנתונים של Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // יצירת מופע של מסד הנתונים של Firebase
        databaseRef = database.getReference("game"); // הגדרת נתיב הבסיס עבור המשחק במסד הנתונים

        DatabaseReference gameStateRef = databaseRef.child("isGameOn"); // יצירת הפניה למפתח "isGameOn" בתוך "game"

        if (gameStateRef == null) {
            isGameOn = FALSE; // אם הנתיב לא קיים, נגדיר את isGameOn כ-FALSE
        }

        gameStateRef.addListenerForSingleValueEvent(new ValueEventListener() { // מאזין לקריאה חד-פעמית מנתוני המשחק
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // מופעל כאשר הנתונים משתנים או נקראים לראשונה
                if (snapshot.exists()) { // בדיקה האם הנתון קיים ב-Firebase
                    isGameOn = snapshot.getValue(Boolean.class); // שליפת הערך של "isGameOn" מהמסד והמרתו ל-Bool
                    if (isGameOn == FALSE) { // אם המשחק כבוי (FALSE)
                        resetGameData(); // קורא למתודה שמאפסת את נתוני המשחק
                        Log.d("Firebase", "isGameOn: " + isGameOn); // הדפסת המצב הנוכחי בלוג
                        // עשה משהו עם gameState (ניתן להוסיף כאן לוגיקה נוספת)
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { // מטפל במקרה של כישלון בקריאת הנתונים
                Log.e("Firebase", "Failed to read game state", error.toException()); // הצגת שגיאה בלוג במקרה של כשל
            }
        });

        // שם את הניקוד מהXML למסך קורא לו.
        scoreTextView = findViewById(R.id.scoreTextView);

        fillImageViewsArray(); // מילוי המערך של הקלפים
        fillDrawablesArray(); // מילוי המערך של תמונות הקלפים

        databaseRef.child("cardOrder").addListenerForSingleValueEvent(new ValueEventListener() {  // מאזין לשינוי בסדר הקלפים ב-Firebase
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {  // כאשר נתוני ה-Firebase משתנים (או מתחילים לקרוא אותם)

                if (!isGameOn) {  // אם המשחק לא התחיל (isGameOn == false)
                    //set isGameOn to TRUE also in Database
                    databaseRef.child("isGameOn").setValue(true);  // עדכון ב-Firebase שהמשחק התחיל
                    isGameOn = TRUE;  // עדכון משתנה מקומי שהמשחק התחיל

                    // השחקן הראשון - שומר את סדר הקלפים ב-Firebase
                    List<Integer> drawablesList = Arrays.asList(drawablesArray);  // המרת המערך של סדר הקלפים לרשימה
                    Collections.shuffle(drawablesList);  // ערבוב סדר הקלפים
                    drawablesList.toArray(drawablesArray);  // החזרת סדר הקלפים המעורבב למערך

                    databaseRef.child("cardOrder").setValue(drawablesList)  // שמירת סדר הקלפים המעורבב ב-Firebase
                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "First player - card order saved."))  // אם השמירה הצליחה
                            .addOnFailureListener(e -> Log.e("Firebase", "Failed to save card order: " + e.getMessage()));  // אם הייתה שגיאה בשמירה

                    whoAmI = 1;  // השחקן הראשון מקבל את הערך 1
                    Log.d("whoAmI", "first player");  // רישום ב-log ששחקן זה הוא הראשון

                } else {  // אם המשחק כבר התחיל (isGameOn == true)
                    // השחקן השני - מוריד את הסדר של הקלפים
                    int i = 0;  // אתחול משתנה אינדקס
                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {  // עבור כל קלף בסדר הקלפים שנשמר
                        drawablesArray[i] = cardSnapshot.getValue(Integer.class);  // עדכון המערך המקומי בסדר הקלפים
                        i++;  // הגדלת אינדקס
                    }
                    Log.d("Firebase", "Second player - card order loaded.");  // רישום ב-log ששחקן זה הוא השני
                    whoAmI = 2;  // השחקן השני מקבל את הערך 2
                    Log.d("whoAmI", "Second player");  // רישום ב-log ששחקן זה הוא השני
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // אם קריאת הנתונים ב-Firebase נכשלת
                Log.e("Firebase", "Failed to load card order: " + error.getMessage());  // רישום שגיאה ב-log
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
                Boolean gameState = snapshot.getValue(Boolean.class);  // קריאה למצב של isGameOn מתוך Firebase (אם המשחק התחיל או לא)
                boolean isGameOn = (gameState != null) ? gameState : false;  // אם gameState הוא null, נגדיר את המצב כ-false (המשחק לא התחיל)

                if (isGameOn) {  // אם המשחק כבר התחיל
                    // המשחק כבר התחיל, שחקן מצטרף למשחק קיים
                    Log.d(TAG, "Game is already running, joining...");  // רישום ב-log שהמשחק כבר התחיל והשחקן מצטרף
                } else {  // אם המשחק לא התחיל
                    // המשחק עדיין לא התחיל, המשתמש הנוכחי מתחיל אותו
                    databaseRef.child("isGameOn").setValue(true);  // עדכון ב-Firebase שהמשחק התחיל
                    Log.d(TAG, "Starting a new game...");  // רישום ב-log שהמשחק התחיל
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // טיפול בשגיאה אם הקריאה ל-Firebase נכשלה
                Log.e(TAG, "Failed to read isGameOn: " + error.getMessage());  // רישום שגיאה ב-log
            }
        });



        databaseRef.child("boardState").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { // אם קיים
                    // השחקן הראשון - שמור את הלוח שלו
                    updateBoardState();  // כאן אתה שומר את מצב הלוח של השחקן הראשון אם הלוח לא קיים ב-Firebase
                    Log.d("Firebase", "First player - board state saved."); // log
                } else {
                    Log.d("Firebase", "Board state already exists."); // log
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to check board state: " + error.getMessage()); // אם נכשל
            }
        });
    }

    // פונקציה להאזנה בזמן אמת למסד הנתונים
    private void addRealtimeListeners() {
        // מאזין למצב הלוח
        Log.d("XXX", "addRealtimeListeners");  // רישום ב-log שהפונקציה הוזנה והחלה לפעול
        databaseRef.child("boardState").addValueEventListener(new ValueEventListener() {  // מאזין לשינויים במצב הלוח ב-Firebase
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("XXXX", "onDataChange");  // רישום ב-log כאשר משתנה המצב ב-Firebase
                // עדכון הלוח במכשיר הנוכחי לפי מצב Firebase
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {  // עבור כל קלף במצב הלוח
                    int position = cardSnapshot.child("position").getValue(Integer.class);  // קריאה למיקום הקלף (position)
                    boolean isFlipped = cardSnapshot.child("isFlipped").getValue(Boolean.class);  // קריאה אם הקלף הפוך (isFlipped)

                    Log.e("XXXX", "position = " + position);  // רישום המיקום של הקלף ב-log
                    Log.e("XXXX", "isFlipped = " + isFlipped);  // רישום אם הקלף הפוך או לא ב-log

                    if (isFlipped) {  // אם הקלף הפוך
                        imageViewsArray[position].setImageResource(drawablesArray[position]);  // הצגת התמונה המתאימה ב-ImageView
                        imageViewsArray[position].setTag(true);  // הגדרת ה-tag של ה-ImageView כ-true (הקלף הפוך)
                    } else {  // אם הקלף לא הפוך
                        imageViewsArray[position].setImageResource(R.drawable.back32);  // הצגת התמונה של הגב
                        imageViewsArray[position].setTag(false);  // הגדרת ה-tag של ה-ImageView כ-false (הקלף לא הפוך)
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read board state: " + error.getMessage());  // טיפול בשגיאה אם הקריאה ל-Firebase נכשלת
            }
        });


        // מאזין למצב המשחק
        databaseRef.child("gameState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;  // אם לא קיימים נתונים, יוצאים מהפונקציה
                }

                // עדכון ניקוד השחקנים
                counterPlayer1 = snapshot.child("player1Score").getValue(Integer.class);  // קריאה לניקוד של השחקן הראשון
                counterPlayer2 = snapshot.child("player2Score").getValue(Integer.class);  // קריאה לניקוד של השחקן השני

                // עדכון תור השחקן
                player1Turn = "Player 1".equals(snapshot.child("playerTurn").getValue(String.class));  // אם השחקן בתור הוא "Player 1", עדכון המשתנה player1Turn

                updateScoreDisplay();  // עדכון תצוגת הניקוד על המסך
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read game state: " + error.getMessage());  // טיפול בשגיאה אם הקריאה ל-Firebase נכשלה
            }
        });


        // מאזין למצב הלוח
        databaseRef.child("boardState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {  // בודק אם קיימים נתונים ב-Firebase
                    // אם קיימים נתונים במצב הלוח, מעדכן את הממשק
                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {  // עובר על כל הקלפים במצב הלוח
                        int position = cardSnapshot.child("position").getValue(Integer.class);  // מקבל את המיקום של הקלף
                        boolean isFlipped = cardSnapshot.child("isFlipped").getValue(Boolean.class);  // מקבל אם הקלף הפוך או לא

                        // אם הקלף הפוך, מציג את התמונה המתאימה
                        if (isFlipped) {
                            imageViewsArray[position].setImageResource(drawablesArray[position]);  // מציג את הקלף הפוך
                            imageViewsArray[position].setTag(true);  // עדכון תוית הקלף לפוך
                        } else {
                            imageViewsArray[position].setImageResource(R.drawable.back32);  // מציג את צד הקטגוריה של הקלף
                            imageViewsArray[position].setTag(false);  // עדכון תוית הקלף לא הפוך
                        }
                    }
                    Log.d("Firebase", "Board state loaded for new player.");  // הודעת לוג שמצב הלוח נטען בהצלחה
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load board state: " + error.getMessage());  // טיפול בשגיאה אם לא מצליח לקרוא את מצב הלוח
            }
        });
    }

    // פונקציה להוספת משתמש למסד הנתונים
    private void addUserToDatabase(String userId, User user) {
        // הגדרה של נתיב מסד הנתונים עבור המשתמש החדש
        databaseRef.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {  // מאזין להשלמת המשימה
                    if (task.isSuccessful()) {  // אם המשימה הושלמה בהצלחה
                        // הודעת לוג שמציינת שהמשתמש נוסף בהצלחה
                        Log.d("Firebase", "User " + userId + " added successfully.");
                    } else {  // אם המשימה לא הצליחה
                        // הודעת שגיאה עם פרטי השגיאה
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
    //הפונקציה isMyTurn בודקת אם זה תורו של השחקן הנוכחי לפעול על פי הערכים של whoAmI
    //אם השחקן הנוכחי יכול לפעול בתורו, הפונקציה מחזירה true, אחרת false.


    // פונקציה שמטפלת בלחיצה על קלף
    public void onBtnClicked(View view) {

        // אם זה לא תור שלי, לא עושה כלום
        if(!isMyTurn())
        {
            Toast.makeText(this, "Not your turn...", Toast.LENGTH_SHORT).show();  // מציגה הודעה אם זה לא תור השחקן
            return;  // יוצא מהפונקציה אם זה לא תור השחקן
        }

        ImageView clickedImageView = (ImageView) view;  // מקבל את ה-ImageView של הקלף שנלחץ

        // מציאת אינדקס הקלף שנלחץ
        int clickedIndex = -1;  // מאתחל את האינדקס לערך לא חוקי (-1)
        for (int i = 0; i < imageViewsArray.length; i++) {  // מעביר על כל המערך של הקלפים
            if (imageViewsArray[i] == clickedImageView) {  // אם מצאנו את הקלף שנלחץ
                clickedIndex = i;  // שומר את האינדקס של הקלף
                break;  // יוצא מהלולאה כי מצאנו את הקלף
            }
        }

        // בדיקה אם הקלף כבר נחשף
        if (clickedImageView.getTag() != null && (boolean) clickedImageView.getTag()) {  // אם ה-Tag של ה-ImageView הוא true (הקלף נחשף)
            return;  // אם הקלף כבר נחשף, לא עושים כלום ומפסיקים את הפונקציה
        }

        // חשיפת הקלף
        clickedImageView.setImageResource(drawablesArray[clickedIndex]);  // מציג את התמונה של הקלף שנבחר
        clickedImageView.setTag(true);  // משנה את ה-Tag של ה-ImageView ל-true, כלומר הקלף נחשף
        logCardFlip(clickedIndex);  // שולח עדכון למסד הנתונים על חשיפת קלף
        updateBoardState();  // מעדכן את מצב הלוח במסד הנתונים

        // ניהול קלף ראשון ושני שנבחרו
        if (count == 0) {  // אם זו הבחירה הראשונה
            card1 = clickedIndex;  // שומר את האינדקס של הקלף הראשון
            count++;  // מגדיל את ה-counter ל-1
        } else if (count == 1) {  // אם זו הבחירה השנייה
            card2 = clickedIndex;  // שומר את האינדקס של הקלף השני
            count++;  // מגדיל את ה-counter ל-2
            handler.postDelayed(this::turnEnd, 1000);  // ממתין 1 שנייה לפני קריאה לפונקציה turnEnd (סיום התור)
        }
    }
    //הסבר
    //הפונקציה בודקת אם זה תור השחקן. אם לא, היא מחזירה הודעה שהשחקן לא בתור ולא עושה כלום.
     //אם זה כן תור השחקן, היא מחשבת את אינדקס הקלף שנלחץ.
     //אם הקלף כבר נחשף, הפונקציה מחזירה ולא עושה כלום.
     //אם הקלף לא נחשף, היא חושפת אותו (מציגה את התמונה שלו) ומעדכנת את ה-Tag של ה-ImageView לסמן שהוא נחשף.
     //הפונקציה שומרת את מצב הקלפים שנבחרו וממתינה לפני שהיא משווה בין השניים או מבצעת סיום תור.



    // מילוי מערך תצוגת הקלפים (ImageViews)
    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {  // עבור כל מקום במערך של ImageViews
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName());  // בונה את שם ה-ID של ה-ImageView (למשל car1, car2, car3, ...)
            imageViewsArray[i] = findViewById(imageViewId);  // מוצא את ה-ImageView על פי ה-ID ושם אותו במערך

            // לוג כדי להראות מידע על ה-ImageView
            Log.e("ImageViewArray", "Index: " + i + ", View ID: car" + (i + 1) + ", Object: " + imageViewsArray[i]);  // מציג מידע על כל ImageView ב-Logcat
        }
    }
    //הפונקציה הזו עוברת על כל המיקומים במערך imageViewsArray.
     //היא בונה שם (ID) לכל ImageView (למשל "car1", "car2" וכו').
     // הפונקציה תמצא כל אחד מהם לפי ה-ID ותשייך אותו למקום הנכון במערך כך שיהיה לך גישה אליו בקלות בקוד.







    // מילוי מערך המשאבים של הקלפים (drawables)
    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {  // עבור כל מקום במערך drawablesArray
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName());  // בונה את שם ה-ID של ה-Drawable (למשל img_1, img_2, ...)
            drawablesArray[i] = drawableId;  // שם את ה-ID של ה-Drawable במערך

            // לוג כדי להראות את ה-Drawable שנבחר
            Log.e("DrawablesArray", "Index: " + i + ", Drawable ID: img_" + ((i % 8) + 1) + ", Resource ID: " + drawableId);  // מציג מידע על כל Drawable ב-Logcat
        }
    }


    // הבדל בין השניים
    //אחד ממלא את המערך בתמונות עצמן (המשאבים) fillDrawablesArray
     //ה-ImageView הוא האלמנט הגרפי שמציג את התמונה על המסך, כלומר כל ImageView יהיה אחראי להציג את התמונה המתאימה בתור קלף במשחק.

    // ערבוב הקלפים עם לוגים
    private void shuffleDrawablesArray() {
        // המרה של המערך drawablesArray לרשימה כדי לבצע עליו ערבוב
        List<Integer> drawablesList = Arrays.asList(drawablesArray);

        // ערבוב הרשימה (random shuffle) כך שהסדר של הקלפים משתנה
        Collections.shuffle(drawablesList);

        // המרת הרשימה חזרה למערך
        drawablesList.toArray(drawablesArray);

        // לוג ERROR כדי לראות את הסדר של הקלפים לאחר ערבוב
        Log.e("Shuffle", "Shuffled DrawablesArray: " + Arrays.toString(drawablesArray));
    }


//Intent הוא אובייקט שמייצג פעולה שאנחנו רוצים לבצע במערכת האנדרואיד. הוא יכול לשמש ל:
    // סיום תור
    private void turnEnd() {
        // הדפסת לוג עם מצב התור של שחקן 1 (player1Turn)
        Log.e("XXXXX", "player1Turn = " + player1Turn);

        // הצגת הודעת Toast שמסבירה שהתור הסתיים
        Toast.makeText(this, "Turn end", Toast.LENGTH_SHORT).show();

        // אם יש התאמה בין הקלפים
        if (drawablesArray[card1].equals(drawablesArray[card2])) {
            // אם מדובר בתור של שחקן 1
            if (player1Turn) {
                counterPlayer1++; // עדכון ניקוד שחקן 1
            } else {
                counterPlayer2++; // עדכון ניקוד שחקן 2
            }

            // רישום ההתאמה במסד הנתונים
            logMatch();

            // הצגת הודעה שהתשובה נכונה
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();

            // בדיקת אם המשחק הסתיים (8 זוגות נחשפו)
            if (counterPlayer1 + counterPlayer2 == 8) {
                // אם המשחק הסתיים, משתנים מצב המשחק ב-Firebase
                databaseRef.child("isGameOn").setValue(false);

                // יצירת Intent כדי לעבור למסך הסיום
                Intent intent = new Intent(MainActivity.this, EndScreen.class);
                intent.putExtra("player1Score", counterPlayer1);  // מוסיף את ניקוד שחקן 1 למסך הסיום
                intent.putExtra("player2Score", counterPlayer2);  // מוסיף את ניקוד שחקן 2 למסך הסיום
                String winner = counterPlayer1 > counterPlayer2 ? "Player 1" : "Player 2";  // קובע את המנצח
                intent.putExtra("winner", winner);  // מוסיף את שם המנצח למסך הסיום
                startActivity(intent); // מתחילים את מסך הסיום
                return; // יוצאים מהפונקציה
            }
        } else {
            // החזרת הקלפים למצב מוסתר אם לא הייתה התאמה
            imageViewsArray[card1].setImageResource(R.drawable.back32);
            imageViewsArray[card2].setImageResource(R.drawable.back32);
            imageViewsArray[card1].setTag(false);
            imageViewsArray[card2].setTag(false);

            // שינוי התור לשחקן השני
            player1Turn = !player1Turn; // העברת התור לשחקן השני
        }

        // עדכון תצוגת הניקוד
        updateScoreDisplay();

        // עדכון מצב הלוח במסד הנתונים
        updateBoardState();

        // עדכון מצב המשחק במסד הנתונים
        updateDatabaseGameState();

        // אפס את המשתנים הקשורים לקלפים שנבחרו
        count = 0;
        card1 = -1;
        card2 = -1;
    }


    // פונקציה לעדכון תצוגת הניקוד
    private void updateScoreDisplay() {
        // הדפסת לוג כדי לבדוק שהפונקציה נקראה
        Log.e("XXXX","+updateScoreDisplay");

        // יצירת טקסט שמציג את הניקוד הנוכחי של שחקן 1 ושחקן 2
        String scoreText = "Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2;

        // עדכון תצוגת הניקוד במסך עם הטקסט החדש
        scoreTextView.setText(scoreText);

        // הדפסת לוג כדי לבדוק שהפונקציה סיימה לפעול
        Log.e("XXXX","-updateScoreDisplay");
    }


    // פונקציה לעדכון מצב הלוח במסד הנתונים
    private void updateBoardState() {
        // יצירת אובייקט HashMap שמכיל את מצב הלוח
        HashMap<String, Object> boardState = new HashMap<>();

        // לולאת for לכל הקלפים בלוח
        for (int i = 0; i < imageViewsArray.length; i++) {
            // יצירת אובייקט HashMap שיכיל את מצב הקלף הנוכחי
            HashMap<String, Object> cardState = new HashMap<>();

            // שמירת מזהה התמונה של הקלף (הקלף שולי ה-ImageView) בתוך ה-HashMap
            cardState.put("imageId", drawablesArray[i]);

            // שמירת מצב חשיפת הקלף (אם הוא נחשף או לא) ב-HashMap
            cardState.put("isFlipped", imageViewsArray[i].getTag() != null && (boolean) imageViewsArray[i].getTag());

            // שמירת המיקום של הקלף בלוח
            cardState.put("position", i);

            // הוספת המידע על הקלף ל-HashMap הכללי של מצב הלוח
            boardState.put("card" + i, cardState);
        }

        // עדכון מצב הלוח במסד הנתונים
        databaseRef.child("boardState").setValue(boardState)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // אם העדכון הצליח, הדפסת לוג על ההצלחה
                        Log.d("Firebase", "Board state updated.");
                    } else {
                        // אם העדכון נכשל, הדפסת לוג על השגיאה
                        Log.e("Firebase", "Failed to update board state: " + task.getException());
                    }
                });
    }

    // פונקציה לעדכון מצב המשחק במסד הנתונים
    private void updateDatabaseGameState() {
        // יצירת אובייקט HashMap שיכיל את מצב המשחק
        HashMap<String, Object> gameState = new HashMap<>();

        // הוספת הניקוד של שחקן 1 ל-HashMap
        gameState.put("player1Score", counterPlayer1);

        // הוספת הניקוד של שחקן 2 ל-HashMap
        gameState.put("player2Score", counterPlayer2);

        // הוספת מידע על מי בתור להנחות המשחק (Player 1 או Player 2) ל-HashMap
        gameState.put("playerTurn", player1Turn ? "Player 1" : "Player 2");

        // הוספת מספר הקלפים שנחשפו ל-HashMap
        gameState.put("cardsFlipped", count);

        // עדכון מצב המשחק במסד הנתונים של Firebase
        databaseRef.child("gameState").setValue(gameState)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // אם העדכון הצליח, הדפסת לוג על ההצלחה
                        Log.d("Firebase", "Game state updated.");
                    } else {
                        // אם העדכון נכשל, הדפסת לוג על השגיאה
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