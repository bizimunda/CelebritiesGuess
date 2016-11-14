package com.example.hamid.celebrities;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hamid.celebrities.task.GetAsyncTask;
import com.example.hamid.celebrities.task.ImageDownloader;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by temp on 13/11/2016.
 */
public class Main extends AppCompatActivity implements GetAsyncTask.GetAsynTaskCallback {

    private ImageView imageView;
    private Button button1, button2, button3, button4;
    private ArrayList<String> celebURLs = new ArrayList<>();
    private ArrayList<String> celebNames = new ArrayList<>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;


    public void celebChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong it was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        createNewQuestion();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected){
            GetAsyncTask task = new GetAsyncTask(Main.this);
            task.execute("http://www.posh24.com/celebrities");
        } else{
            Toast.makeText(Main.this, "No data connection", Toast.LENGTH_SHORT).show();
        }



    }


    @Override
    public void onPreGet() {

    }

    @Override
    public void onPostGet(String s) {

        Log.i("Content: ", s);

        String[] splitResult = s.split("<div class=\"sidebarContainer\">");


        Pattern p = Pattern.compile("<img src=\"(.*?)\"");
        Matcher m = p.matcher(splitResult[0]);

        while (m.find()) {
            celebURLs.add(m.group(1));
        }

        Pattern c = Pattern.compile("alt=\"(.*?)\"");
        Matcher d = c.matcher(splitResult[0]);

        while (d.find()) {
            celebNames.add(d.group(1));
        }

        createNewQuestion();

    }

    public void createNewQuestion() {
        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celeImage;
        try {
            celeImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            imageView.setImageBitmap(celeImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i = 0; i < 4; i++) {

                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = random.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button1.setText(answers[0]);
            button2.setText(answers[1]);
            button3.setText(answers[2]);
            button4.setText(answers[3]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
