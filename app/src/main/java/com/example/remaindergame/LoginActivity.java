package com.example.remaindergame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.remaindergame.R;
import java.util.Objects;

// מחלקת הפעילות לכניסה (LoginActivity)
public class LoginActivity extends AppCompatActivity {

    // משתנים גלובליים
    FirebaseAuth auth; // ניהול אימות משתמשים
    GoogleSignInClient googleSignInClient; // לקוח כניסה ל-Google
    ShapeableImageView imageView; // להצגת תמונת פרופיל המשתמש
    TextView name, mail; // להצגת שם ודוא"ל המשתמש

    // משתנה המפעיל פעילות ומחזיר תוצאה
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) { // בדיקת הצלחת הפעולה
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class); // קבלת פרטי חשבון
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) { // אם הכניסה הצליחה
                                        auth = FirebaseAuth.getInstance(); // קבלת המשתמש המחובר
                                        // טוען תמונת פרופיל באמצעות Glide
                                        Glide.with(LoginActivity.this)
                                                .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                                                .into(imageView);
                                        name.setText(auth.getCurrentUser().getDisplayName()); // שם המשתמש
                                        mail.setText(auth.getCurrentUser().getEmail()); // דוא"ל המשתמש
                                        Toast.makeText(LoginActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                                        // מעבר לפעילות הבאה
                                        Intent intent = new Intent(LoginActivity.this, OpenActivity.class);
                                        intent.putExtra("USERNAME", auth.getCurrentUser().getDisplayName()); // מעביר שם המשתמש
                                        startActivity(intent);
                                    } else { // אם הכניסה נכשלה
                                        Toast.makeText(LoginActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace(); // טיפול בשגיאות
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
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        // הגדרת אפשרויות כניסה ל-Google
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // מזהה לקוח (Client ID)
                .requestEmail() // בקשת דוא"ל המשתמש
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options); // יצירת לקוח Google Sign-In
        auth = FirebaseAuth.getInstance(); // אתחול Firebase Authentication

        // מאזין ללחיצה על כפתור כניסה
        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent(); // פתיחת פעילות כניסה ל-Google
                activityResultLauncher.launch(intent); // מפעיל את הפעילות
            }
        });

        // מאזין ללחיצה על כפתור יציאה
        MaterialButton signOut = findViewById(R.id.signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // מאזין לשינויים במצב האימות
                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if (firebaseAuth.getCurrentUser() == null) { // אם המשתמש יצא
                            googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(LoginActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, LoginActivity.class)); // מעבר חזרה למסך כניסה
                                }
                            });
                        }
                    }
                });
                FirebaseAuth.getInstance().signOut(); // מבצע יציאה
            }
        });

        // אם המשתמש כבר מחובר
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, OpenActivity.class)); // מעבר למסך הבא
            finish(); // מסיים את הפעילות הנוכחית
        }
    }
}