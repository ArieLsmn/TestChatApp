package com.android.testchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.crypto.FileReadWrite;

public class FetchPrivateKey extends AppCompatActivity {
String privKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final Intent nextActivity = new Intent(FetchPrivateKey.this, Chat.class);


        privKey= FileReadWrite.readFromFile(FetchPrivateKey.this,UserDetails.username);
        Toast.makeText(FetchPrivateKey.this, "S:"+privKey, Toast.LENGTH_SHORT).show();
        nextActivity.putExtra("priv",privKey);
    }
}
