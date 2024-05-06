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

public class MainActivity extends AppCompatActivity
{
    int count = 0;
    int card1;
    int counterPlayer1 = 0;
    int counterPlayer2 = 0;
    String turn = "counterPlayer1";
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
        turnEnd1(imageView);
    }
}

