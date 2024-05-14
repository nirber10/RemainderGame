package com.example.remaindergame;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    int count = 0;
    int card1 = -1; // Initialize to -1 to indicate no card selected
    int card2 = -1;
    int counterPlayer1 = 0;
    int counterPlayer2 = 0;
    boolean player1Turn = true; // Player 1 starts the game
    ImageView[] imageViewsArray = new ImageView[16];
    Integer[] drawablesArray = new Integer[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillImageViewsArray();
        fillDrawablesArray();
        shuffleDrawablesArray();
    }

    // Fill imageViewsArray with ImageViews
    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName());
            imageViewsArray[i] = findViewById(imageViewId);
        }
    }

    // Fill drawablesArray with drawables identifiers
    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName());
            drawablesArray[i] = drawableId;
        }
    }

    // Shuffle drawablesArray
    private void shuffleDrawablesArray() {
        List<Integer> drawablesList = Arrays.asList(drawablesArray);
        Collections.shuffle(drawablesList);
        drawablesList.toArray(drawablesArray);
    }

    public void openCard(View view) {
        ImageView imageView = (ImageView) view;
        int id_of_imageview_in_array = 0;
        for (int i = 0; i < imageViewsArray.length; i++) {
            if (imageViewsArray[i] == imageView) {
                id_of_imageview_in_array = i;
                break;
            }
        }

        // If the card is already flipped, return
        if (imageView.getDrawable() != null) {
            return;
        }

        imageView.setImageResource(drawablesArray[id_of_imageview_in_array]);
        count++;

        // Store the position of the card clicked
        if (count == 1) {
            card1 = id_of_imageview_in_array;
        } else if (count == 2) {
            card2 = id_of_imageview_in_array;
            turnEnd();
        }
    }

    private void turnEnd() {
        if (drawablesArray[card1].equals(drawablesArray[card2])) {
            // Match found
            if (player1Turn) {
                counterPlayer1++;
            } else {
                counterPlayer2++;
            }
            if (counterPlayer1 + counterPlayer2 == 8) {
                // Game Over, you can put your game over logic here
                // For now, let's just display a toast
                Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No match found
            // Delay hiding cards for better user experience
            imageViewsArray[card1].postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageViewsArray[card1].setImageResource(0); // Hide first card
                    imageViewsArray[card2].setImageResource(0); // Hide second card
                }
            }, 1000);
        }

        // Reset count and prepare for the next turn
        count = 0;
        card1 = -1;
        card2 = -1;

        // Switch player turns
        player1Turn = !player1Turn;
    }
}

