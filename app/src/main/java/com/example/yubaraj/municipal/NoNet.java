package com.example.yubaraj.municipal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NoNet extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internate);
        txtMessage=findViewById(R.id.no_net_message);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        Intent intent=getIntent();
        String message=intent.getStringExtra("message");
        txtMessage.setHint(message);
    }

    private void checkNetConnection() {
        CheckInternet checkInternet=new CheckInternet();
        if (checkInternet.isNetworkAvailable(this)==true) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
            return;

        } else if(checkInternet.isOnline()==true) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
            return;

        }else{
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        checkNetConnection();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Do you want to Exit?");
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }
}
