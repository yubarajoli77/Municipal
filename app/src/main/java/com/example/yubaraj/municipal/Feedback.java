package com.example.yubaraj.municipal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yubaraj.municipal.R;

/**
 * Created by yubar on 3/8/2018.
 */

public class Feedback extends AppCompatActivity {
    private EditText email,message;
    private Button feedBackSend;
    private String senderEmail,senderMessage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Feedback");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initialize();
    }

    private void initialize() {
        email=findViewById(R.id.feedback_email);
        message=findViewById(R.id.feedback_message);
        feedBackSend=findViewById(R.id.feedback_submit);

        feedBackSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senderEmail=email.getText().toString();
                senderMessage=message.getText().toString();
                sendMail(senderEmail,senderMessage);
            }
        });

    }

    private void sendMail(String sender,String feedback) {

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        String[] recipients = new String[]{"yubarajoli77@gmail.com", "",};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback from user");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "This is email's message");
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "From: "+sender+"\nMessage: "+feedback+"\n\nSent from the Lads to Leaders/Leaderettes Android App.");

        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        finish();


    }
}
