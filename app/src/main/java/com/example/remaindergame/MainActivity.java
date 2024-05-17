package com.example.remaindergame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int count = 0;
    private int card1 = -1; // Initialize to -1 to indicate no card selected
    private int card2 = -1;
    private int counterPlayer1 = 0;
    private int counterPlayer2 = 0;
    private boolean player1Turn = true; // Player 1 starts the game
    private final ImageView[] imageViewsArray = new ImageView[16];
    private final Integer[] drawablesArray = new Integer[16];
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillImageViewsArray();
        fillDrawablesArray();
        shuffleDrawablesArray();
    }

    // First array - Image views
    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName());
            imageViewsArray[i] = findViewById(imageViewId);
        }
    }

    // Second array - Drawables (Images) identifiers
    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {
            // Fill in the drawable's identifier
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName());
            drawablesArray[i] = drawableId;
        }
    }

    // Second array - Shuffle the array
    private void shuffleDrawablesArray() {
        List<Integer> drawablesList = Arrays.asList(drawablesArray); // Convert array to list
        Collections.shuffle(drawablesList); // Shuffle list
        drawablesList.toArray(drawablesArray);

    }

    public void openCard(View view) throws InterruptedException {
        ImageView imageView = (ImageView) view;
        int id_of_imageview_in_array = 0;
        for (int i = 0; i < imageViewsArray.length; i++) {
            if (imageViewsArray[i] == imageView) {
                id_of_imageview_in_array = i;
                break;
            }
        }
        imageView.setImageResource(drawablesArray[id_of_imageview_in_array]);
        count++;
        turnEnd(imageView);
    }



    private void turnEnd(ImageView imageView) {
        if (drawablesArray[card1].equals(drawablesArray[card2])) {
            // Match found
            if (player1Turn) {
                counterPlayer1++;
            } else {
                counterPlayer2++;
            }
            if (counterPlayer1 + counterPlayer2 == 8) {
                // Game Over
                Toast.makeText(this, "Game Over! Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2, Toast.LENGTH_LONG).show();
            }
        } else {
            // No match found
            imageViewsArray[card1].setImageResource(R.drawable.back32); // Hide first card
            imageViewsArray[card2].setImageResource(R.drawable.back32);
            imageViewsArray[card1].setTag(false);
            imageViewsArray[card2].setTag(false);
        }

        // Reset count and prepare for the next turn
        count = 0;
        card1 = -1;
        card2 = -1;

        // Switch player turns
        player1Turn = !player1Turn;
    }

    public void onBtnClicked(View view) {

        Log.e("XXXXXXX", "+onBtnClick");
        }
}

