package com.example.remaindergame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int count = 0;
    private int card1 = -1;
    private int card2 = -1;
    private int counterPlayer1 = 0;
    private int counterPlayer2 = 0;
    private boolean player1Turn = true;
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

        // Set onClick listeners for each image view
        for (ImageView imageView : imageViewsArray) {
            imageView.setOnClickListener(this::onBtnClicked);
        }
    }

    public void onBtnClicked(View view) {
        ImageView clickedImageView = (ImageView) view;

        int clickedIndex = -1;
        for (int i = 0; i < imageViewsArray.length; i++) {
            if (imageViewsArray[i] == clickedImageView) {
                clickedIndex = i;
                break;
            }
        }

        // If card is already opened, ignore the click
        if (clickedImageView.getTag() != null && (boolean) clickedImageView.getTag()) {
            return;
        }

        clickedImageView.setImageResource(drawablesArray[clickedIndex]);
        clickedImageView.setTag(true);
Log.e("xxxx" , "line 59");
        if (count == 0) {
            Log.e("xxxx" , "line 61");
            card1 = clickedIndex;
            count++;
        } else if (count == 1) {
            Log.e("xxxx" , "line 65");
            card2 = clickedIndex;
            count++;
            handler.postDelayed(this::turnEnd, 1000);
        }
    }

    private void fillImageViewsArray() {
        for (int i = 0; i < imageViewsArray.length; i++) {
            int imageViewId = getResources().getIdentifier("car" + (i + 1), "id", getPackageName());
            imageViewsArray[i] = findViewById(imageViewId);
        }
    }

    private void fillDrawablesArray() {
        for (int i = 0; i < drawablesArray.length; i++) {
            int drawableId = getResources().getIdentifier("img_" + ((i % 8) + 1), "drawable", getPackageName());
            drawablesArray[i] = drawableId;
        }
    }

    private void shuffleDrawablesArray() {
        List<Integer> drawablesList = Arrays.asList(drawablesArray);
        Collections.shuffle(drawablesList);
        drawablesList.toArray(drawablesArray);
    }

    private void turnEnd() {
        if (drawablesArray[card1].equals(drawablesArray[card2])) {
            if (player1Turn) {
                counterPlayer1++;
            } else {
                counterPlayer2++;
            }

            if (counterPlayer1 + counterPlayer2 == 8) {
                Toast.makeText(this, "Game Over! Player 1: " + counterPlayer1 + " - Player 2: " + counterPlayer2, Toast.LENGTH_LONG).show();
            }
        } else {
            imageViewsArray[card1].setImageResource(R.drawable.back32);
            imageViewsArray[card2].setImageResource(R.drawable.back32);
            imageViewsArray[card1].setTag(false);
            imageViewsArray[card2].setTag(false);
        }

        count = 0;
        card1 = -1;
        card2 = -1;
        player1Turn = !player1Turn;
    }
}
