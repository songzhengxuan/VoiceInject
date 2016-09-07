package com.example.sks.voiceinject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView mTestText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestText = (TextView) findViewById(R.id.test);
        mTestText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("voicectrl", "onClick on text");
                Toast.makeText(MainActivity.this, "hoooooooo!", Toast.LENGTH_LONG).show();
            }
        });
        mTestText.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Log.d("voicectrl", "longclick on text");
                Toast.makeText(MainActivity.this, "hoooo, long click", Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }
}
