package com.android.testchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.crypto.FileReadWrite;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FetchPublicKey extends AppCompatActivity {
DatabaseReference pubRef;
String chatWith;
PubKeyCoord pubKey;
String privKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatWith = UserDetails.chatWith;
        FirebaseDatabase myDB = FirebaseDatabase.getInstance();
        Toast.makeText(FetchPublicKey.this, "Fetching "+chatWith, Toast.LENGTH_SHORT).show();

        pubRef = myDB.getReference("users/"+chatWith+"/publickey");

       //if (pubKey ==null || pubKey.x == ""){
        pubRef.addValueEventListener (
        new ValueEventListener() {

            @Override
            public void onDataChange( com.google.firebase.database.DataSnapshot dataSnapshot) {
                pubKey = dataSnapshot.getValue(PubKeyCoord.class);

                                final Intent chatActivity = new Intent(FetchPublicKey.this, Chat.class);

                                chatActivity.putExtra("pubx",pubKey.x);
                                chatActivity.putExtra("puby",pubKey.y);

                                startActivity(chatActivity);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
