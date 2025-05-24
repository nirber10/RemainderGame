package com.example.remaindergame;

import android.content.Intent; // מיובא לצורך שימוש בכוונות (Intent) למעבר בין מסכים
import android.os.Bundle; // מיובא לצורך שמירה וטעינה של נתונים בעת מעבר בין פעילויות
import android.view.View; // מיובא לצורך טיפול בלחיצות כפתור
import android.widget.TextView; // מיובא לצורך הצגת טקסטים על המסך
import android.widget.Toast; // מיובא לצורך הצגת הודעות טוסט

import androidx.activity.result.ActivityResult; // מיובא לצורך קבלת תוצאות מחזרת פעולה
import androidx.activity.result.ActivityResultCallback; // מיובא לצורך קבלת callback לתוצאה של Activity
import androidx.activity.result.ActivityResultLauncher; // מיובא לצורך הרצה של פעילות עם תוצאה
import androidx.activity.result.contract.ActivityResultContracts; // מיובא לצורך שימוש בהסכמים (contracts) של פעילויות
import androidx.annotation.NonNull; // מיובא לצורך טיפול בקוד לא נלאה
import androidx.appcompat.app.AppCompatActivity; // מיובא לצורך יצירת פעילויות עם ממשק עיצוב
import com.bumptech.glide.Glide; // מיובא לצורך הצגת תמונות בפרופיל בעזרת Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn; // מיובא לצורך אינטגרציה עם כניסת גוגל
import com.google.android.gms.auth.api.signin.GoogleSignInAccount; // מיובא לצורך קבלת פרטי חשבון גוגל
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // מיובא לצורך יצירת לקוח כניסה ל-Google
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // מיובא לצורך הגדרת אפשרויות כניסת גוגל
import com.google.android.gms.common.SignInButton; // מיובא לצורך הצגת כפתור כניסה לגוגל
import com.google.android.gms.common.api.ApiException; // מיובא לצורך טיפול בשגיאות של גוגל
import com.google.android.gms.tasks.OnCompleteListener; // מיובא לצורך טיפול בהשלמת משימות (Tasks)
import com.google.android.gms.tasks.OnSuccessListener; // מיובא לצורך טיפול בהצלחת משימות
import com.google.android.gms.tasks.Task; // מיובא לצורך עבודה עם משימות
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp; // מיובא לצורך אתחול Firebase
import com.google.firebase.auth.AuthCredential; // מיובא לצורך טיפול בהסמכות גישה
import com.google.firebase.auth.AuthResult; // מיובא לצורך טיפול בתוצאות כניסה
import com.google.firebase.auth.FirebaseAuth; // מיובא לצורך עבודה עם Firebase Authentication
import com.google.firebase.auth.GoogleAuthProvider; // מיובא לצורך עבודה עם הספק Google לאימות
import com.example.remaindergame.R; // מיובא לצורך עבודה עם קובצי משאבים (resources)

import java.util.Objects;

// מחלקת הפעילות לכניסה (LoginActivity)
public class LoginActivity extends AppCompatActivity {

    // משתנים
    FirebaseAuth auth; // ניהול אימות משתמשים
    GoogleSignInClient googleSignInClient; // לקוח כניסה ל-Google
    ShapeableImageView imageView; // להצגת תמונת פרופיל המשתמש
    TextView name, mail; // להצגת שם ודוא"ל המשתמש

    // משתנה המפעיל פעילות ומחזיר תוצאה
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), // התחברות עם הסכם (contract) להתחלת פעילות שמחזירה תוצאה
            new ActivityResultCallback<ActivityResult>() { // callback לאחר סיום הפעילות
                @Override
                public void onActivityResult(ActivityResult result) { // האם מילאת נכון את הטופס
                    if (result.getResultCode() == RESULT_OK) { // בדיקת הצלחת הפעולה
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData()); // קבלת נתונים מהפעילות
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class); // קבלת פרטי חשבון גוגל
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null); // יצירת הסמכה (credential) עם טוקן הגישה
                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() { // ביצוע כניסה עם הסמכה
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) { // אם הכניסה הצליחה
                                        auth = FirebaseAuth.getInstance(); // קבלת המשתמש המחובר
                                        Glide.with(LoginActivity.this) // טוען תמונת פרופיל בעזרת Glide
                                                .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()) // טוען את תמונת הפרופיל של המשתמש
                                                .into(imageView); // מציב את התמונה ב-ImageView
                                        name.setText(auth.getCurrentUser().getDisplayName()); // מציב את שם המשתמש ב-TextView
                                        mail.setText(auth.getCurrentUser().getEmail()); // מציב את הדוא"ל ב-TextView
                                        Toast.makeText(LoginActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show(); // מציג הודעת הצלחה
                                        Intent intent = new Intent(LoginActivity.this, OpenActivity.class); // מעבר למסך הבא
                                        intent.putExtra("USERNAME", auth.getCurrentUser().getDisplayName()); // מעביר את שם המשתמש כפרמטר
                                        startActivity(intent); // מתחיל את הפעילות הבאה
                                    } else { // אם הכניסה נכשלה
                                        Toast.makeText(LoginActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show(); // מציג הודעת שגיאה
                                    }
                                }
                            });
                        } catch (ApiException e) { // טיפול בשגיאות בעת ניסיון להתחבר עם גוגל
                            e.printStackTrace(); // הדפסת שגיאה
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // קביעת הפריסה של המסך
        FirebaseApp.initializeApp(this); // אתחול Firebase

        // אתחול רכיבים גרפיים
        imageView = findViewById(R.id.profileImage); // אתחול ImageView להצגת תמונת פרופיל
        name = findViewById(R.id.nameTV); // אתחול TextView להצגת שם המשתמש
        mail = findViewById(R.id.mailTV); // אתחול TextView להצגת דוא"ל המשתמש

        // הגדרת אפשרויות כניסה ל-Google - מילוי טופס גוגל - פרטים לאפייון תהליך הכניסה
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // יצירת אובייקט של אפשרויות כניסה
                .requestIdToken(getString(R.string.client_id)) // בקשת מזהה לקוח
                .requestEmail() // בקשת דוא"ל המשתמש
                .build(); // בונה את האובייקט עם ההגדרות

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options); // יצירת לקוח Google Sign-In - מביא לגוגל את הטופס
        auth = FirebaseAuth.getInstance(); // אתחול Firebase Authentication

        // מאזין ללחיצה על כפתור כניסה
        SignInButton signInButton = findViewById(R.id.signIn); // אתחול כפתור כניסה
        signInButton.setOnClickListener(new View.OnClickListener() { //  מאזין ללחיצה על הכפתור - שילחצו על הכפתור תלך ל106
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent(); // יצירת כוונה (Intent) לפתיחת פעילות כניסה ל-Google
                activityResultLauncher.launch(intent); // מפעיל את הפעילות ומחכה לתוצאה - שולח את הטופס
            }
        });

        // מאזין ללחיצה על כפתור יציאה
        MaterialButton signOut = findViewById(R.id.signout); // אתחול כפתור יציאה
        signOut.setOnClickListener(new View.OnClickListener() { // מאזין ללחיצה על כפתור היציאה
            @Override
            public void onClick(View view) {
                // מאזין לשינויים במצב האימות
                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() { // מאזין לשינויים במצב האימות
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if (firebaseAuth.getCurrentUser() == null) { // אם המשתמש יצא
                            googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() { // מבצע יציאה מגוגל
                                @Override
                                public void onSuccess(Void unused) { // בהצלחה
                                    Toast.makeText(LoginActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show(); // הודעת הצלחה
                                    startActivity(new Intent(LoginActivity.this, LoginActivity.class)); // חוזר למסך הכניסה
                                }
                            });
                        }
                    }
                });
                FirebaseAuth.getInstance().signOut(); // מבצע יציאה מ-Firebase
            }
        });

        // אם המשתמש כבר מחובר
        if (auth.getCurrentUser() != null) { // אם יש משתמש מחובר
            startActivity(new Intent(LoginActivity.this, OpenActivity.class)); // מעבר לפעילות הבאה
            finish(); // סיום הפעילות הנוכחית
        }
    }
}
