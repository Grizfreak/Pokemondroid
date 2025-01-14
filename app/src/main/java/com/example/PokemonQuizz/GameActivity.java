package com.example.PokemonQuizz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Activity launched at start and used to access differents Activities
 */
public class GameActivity extends AppCompatActivity {

    /**
     * Attributes declaration
     */
    public TypedArray imgs_black;
    public ImageView pokemon, scoresView;
    public ImageButton reload;
    public boolean displaycolor;
    public ImageButton display;
    public PokemonNames actualPokemonName;
    public List<PokemonNames> displayed_names = new ArrayList<>();
    public Button btn1, btn2, btn3, btn4;
    public TextView chrono, timelimit, scoresText;
    public ProgressBar answerBar;
    public int nbfaults = 0;
    public Score actualScore;
    public final int MAXFAULTS = 3;
    CountDownTimer actualTimer;
    public MusicManager musicM;

    /**
     * Default function used at creation of the Activity
     *
     * @param savedInstanceState bundle used for app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar && Remove notification bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Getting Parameters from Intent
        Intent intent = getIntent();
        actualScore = new Score(intent.getExtras().getString("name"));

        //Describing attributes
        pokemon = findViewById(R.id.pokemonquestion);
        display = findViewById(R.id.display);
        reload = findViewById(R.id.reload);
        btn1 = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);
        btn4 = findViewById(R.id.button4);
        chrono = findViewById(R.id.chrono);
        timelimit = findViewById(R.id.timelimit);
        answerBar = findViewById(R.id.answerBar);
        scoresView = findViewById(R.id.scoresView);
        scoresText = findViewById(R.id.scoresText);
        scoresText.setText(String.valueOf(actualScore.score));
        musicM = new MusicManager(this);
        btn1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
        btn2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.blue));
        btn3.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.yellow));
        btn4.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
        changeImage();
        imgs_black = getResources().obtainTypedArray(R.array.pokemon_black_images_array);

        //Creating List of names
        displayed_names = Arrays.asList(PokemonNames.values());

        //Generating
        getRndImg();
        getPokemonName();
        launchTimer();

        //Setting up buttons
        reload.setOnClickListener(view -> {
            //Generating
            getRndImg();
            getPokemonName();
            launchTimer();
        });
        display.setOnClickListener(view -> displayColor());
        btn1.setOnClickListener(view -> {
            musicM.playButtonSound();
            checkResult(btn1);
        });
        btn2.setOnClickListener(view -> {
            musicM.playButtonSound();
            checkResult(btn2);
        });
        btn3.setOnClickListener(view -> {
            musicM.playButtonSound();
            checkResult(btn3);
        });
        btn4.setOnClickListener(view -> {
            musicM.playButtonSound();
            checkResult(btn4);
        });
    }

    /**
     * Function used to block interaction with back button
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * Function used to launch the timer for the actual question
     */
    private void launchTimer() {
        int count = checkTimeLimitAvailable();
        actualTimer = new CountDownTimer(count, 1) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                timelimit.setText("Il vous reste : " + millisUntilFinished / 1000 + " s");
            }

            public void onFinish() {
                displayBadAnswer();
            }
        }.start();
    }

    /**
     * Function used to calculate the time limit for the timer
     * @return the time limit in millis
     */
    private int checkTimeLimitAvailable() {
        int i = 21000;
        if (actualScore.score >= 50) {
            i = 16000;
        }
        if (actualScore.score >= 100) {
            i = 11000;
        }
        if (actualScore.score >= 200) {
            i = 6000;
        }
        musicM.playLevel(actualScore.score);
        return i;
    }

    /**
     * Function used to retrieve the pokemon name from the image picked
     */
    private void getPokemonName() {
        //Getting all names
        List<PokemonNames> names_to_get = new ArrayList<>(displayed_names);
        List<PokemonNames> names = new ArrayList<>();
        int rndInt;
        final Random rand = new Random();
        //getting ressource name in order to get the good answer
        String resName = getResourceNameFromClassByID(R.drawable.class, (Integer) pokemon.getTag());
        resName = resName.replace("_", "");
        int num = Integer.parseInt(resName);
        //Adding game responses with good answer and 3 random answers
        names.add(names_to_get.remove(num - 1));
        actualPokemonName = names.get(0);
        rndInt = rand.nextInt(names_to_get.size());
        names.add(names_to_get.remove(rndInt));
        rndInt = rand.nextInt(names_to_get.size());
        names.add(names_to_get.remove(rndInt));
        rndInt = rand.nextInt(names_to_get.size());
        names.add(names_to_get.remove(rndInt));
        Collections.shuffle(names);

        //Applying names to answer buttons
        btn1.setText(names.remove(0).toString());
        btn2.setText(names.remove(0).toString());
        btn3.setText(names.remove(0).toString());
        btn4.setText(names.remove(0).toString());
    }

    /**
     * Function used to retrieve the name of the ressource used in the imageView
     * @param aClass the imageView
     * @param resourceID the id of the ressource in drawable folder
     * @return the name of the ressource
     * @throws IllegalArgumentException if argument passed isn't usable
     */
    public String getResourceNameFromClassByID(Class<?> aClass, int resourceID)
            throws IllegalArgumentException {
        /* Get all Fields from the class passed. */
        Field[] drawableFields = aClass.getFields();

        /* Loop through all Fields. */
        for (Field f : drawableFields) {
            try {
                /* All fields within the subclasses of R
                 * are Integers, so we need no type-check here. */

                /* Compare to the resourceID we are searching. */
                if (resourceID == f.getInt(null))
                    return f.getName(); // Return the name.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* Throw Exception if nothing was found*/
        throw new IllegalArgumentException();
    }

    /**
     * Function used to get an Image from the list of drawables
     */
    public void getRndImg() {

        final Random rand = new Random();
        final int rndInt = rand.nextInt(imgs_black.length());
        final int resID = imgs_black.getResourceId(rndInt, 0);
        displaycolor = false;
        //setting color filter to make image black and white
        int color = ContextCompat.getColor(this, R.color.black);
        pokemon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        pokemon.setImageResource(resID);
        pokemon.setTag(resID);
    }

    /**
     * Function used to display the color image and to hide it
     */
    public void displayColor() {
        //if color is displayed, go to black and white
        if (displaycolor) {
            displaycolor = false;
            int color = ContextCompat.getColor(this, R.color.black);
            pokemon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else {        //else display the true color of the image
            displaycolor = true;
            pokemon.setColorFilter(null);
        }
    }

    /**
     * Function used to check if the answer is correct or not
     * @param btn the button clicked
     */
    private void checkResult(Button btn) {
        actualTimer.cancel();
        btn1.setClickable(false);
        btn2.setClickable(false);
        btn3.setClickable(false);
        btn4.setClickable(false);
        //if the name equals the button text
        if (actualPokemonName.toString().equals(btn.getText().toString())) {
            displayGoodAnswer();
        } else {
            displayBadAnswer();
        }
    }

    /**
     *  Function used to display the good answer animation
     */
    @SuppressLint("SetTextI18n")
    private void displayGoodAnswer() {
        //set good answer text
        chrono.setTextColor(ContextCompat.getColor(this, R.color.deep_green));
        chrono.setText("Bien joué ! Nos équipes préparent le prochain !");
        displayColor();
        actualScore.addPoint();
        scoresText.setText(String.valueOf(actualScore.score));

        new CountDownTimer(3000, 1) {

            public void onTick(long millisUntilFinished) {
                displayProgress(millisUntilFinished);
                if (millisUntilFinished == 1500) {
                    musicM.playPokedexRoll();
                }
            }

            public void onFinish() {
                displayColor();
                answerBar.setProgress(100);
                reload.callOnClick();
                answerBar.setProgress(0);
                chrono.setText("");
                reactiveButtons();
            }
        }.start();
    }

    /**
     * Function used to display bad answer animation
     */
    @SuppressLint("SetTextI18n")
    private void displayBadAnswer() {
        chrono.setTextColor(ContextCompat.getColor(this, R.color.red));
        chrono.setText("Dommage, ce pokémon était " + actualPokemonName.toString().toUpperCase(Locale.ROOT));
        displayColor();
        nbfaults++;
        changeImage();
        new CountDownTimer(3000, 1) {

            public void onTick(long millisUntilFinished) {
                displayProgress(millisUntilFinished);
                if (millisUntilFinished == 1500) {
                    musicM.playPokedexRoll();
                }
            }

            public void onFinish() {

                if (nbfaults >= MAXFAULTS) {
                    actualTimer = null;
                    Intent intent = new Intent(GameActivity.this, ScoresActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("nouveauscore", true);
                    intent.putExtra("score", actualScore);
                    getApplicationContext().startActivity(intent);
                } else {
                    displayColor();
                    answerBar.setProgress(100);
                    reload.callOnClick();
                    answerBar.setProgress(0);
                    chrono.setText("");
                    reactiveButtons();
                }
            }
        }.start();
    }

    /**
     * Function used to change the life counter of the player
     */
    private void changeImage() {
        switch (nbfaults) {
            case 1:
                scoresView.setImageResource(R.drawable.pixel_balls_2);
                break;
            case 2:
                scoresView.setImageResource(R.drawable.pixel_balls_1);
                break;
            case 3:
                scoresView.setImageResource(R.drawable.pixel_balls_0);
                break;
            default:
                scoresView.setImageResource(R.drawable.pixel_balls_3);
                break;
        }
    }

    /**
     * Function used to active buttons
     */
    private void reactiveButtons() {
        btn1.setClickable(true);
        btn2.setClickable(true);
        btn3.setClickable(true);
        btn4.setClickable(true);
    }

    /**
     * Function used to display progress of waiting process
     * @param millisUntilFinished millis until end of timer
     */
    private void displayProgress(long millisUntilFinished) {
        int maxValue = answerBar.getMax();
        long progress = ((3000 - millisUntilFinished) * maxValue) / 3000;
        answerBar.setProgress((int) progress);
    }

    /**
     *  Function used to resume music on app resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        musicM.start();
    }

    /**
     * Function used to pause music on app paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        musicM.pause();
    }

    /**
     * Function used to pause music on app stop
     */
    @Override
    protected void onStop() {
        super.onStop();
        musicM.pause();
    }

    /**
     * Function used to pause music when app destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicM.pause();
    }
}