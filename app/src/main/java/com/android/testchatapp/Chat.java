package com.android.testchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypto.ArrUtil;
import com.android.crypto.CryptoOperation;
import com.android.crypto.ECUtil;
import com.android.crypto.FileReadWrite;
import com.android.crypto.GenerateKey;
import com.android.crypto.MessageData;
import com.firebase.client.ChildEventListener;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Chat extends AppCompatActivity {
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2, ref3,ref4;
    SimpleDateFormat sdf;
    DatabaseReference dbRef;
    PubKeyCoord pubKey;
    ECPoint pubkeyPoint;
    String[] msgDeciph;
    ECParameterSpec ECP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sdf = new SimpleDateFormat("EEE, MMM d 'AT' HH:mm a");
        pubKey = new PubKeyCoord(getIntent().getStringExtra("pubx"),getIntent().getStringExtra("puby"));
        pubkeyPoint = new ECPoint(new BigInteger(pubKey.x),new BigInteger(pubKey.y));
        //Toast.makeText(Chat.this,"X: "+ pubKey.x, Toast.LENGTH_LONG).show();

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.fullScroll(View.FOCUS_DOWN);


        FirebaseDatabase myDB = FirebaseDatabase.getInstance();
        dbRef = myDB.getReference();
        final String linkFirebase = dbRef.toString();


        Firebase.setAndroidContext(this);
        reference1 = new Firebase(linkFirebase+"/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase(linkFirebase+"/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        ref3 = new Firebase(linkFirebase+"/cipher/" + UserDetails.username + "_" + UserDetails.chatWith);
        ref4 = new Firebase(linkFirebase+"/cipher/" + UserDetails.chatWith + "_" + UserDetails.username);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                Map<String, String> map = new HashMap<String, String>();
                Map<String,String> msgMap = new HashMap<String,String>();

                if (!messageText.equals("")) {
               /**/

                       String currentDateandTime = sdf.format(new Date());
                       map.put("message", messageText);
                       map.put("user", UserDetails.username);
                       map.put("time", currentDateandTime);
                       reference1.push().setValue(map);
                       reference2.push().setValue(map);

                       messageArea.setText("");

                    try {
                        //Toast.makeText(Chat.this,"Sent and Encrypted", Toast.LENGTH_SHORT).show();
                        //ECParameterSpec ECP = ECUtil.getCurveSpec("secp256k1");
                        //Map<String,String> kg = new HashMap<String,String>();

                        GenerateKey genKey =new GenerateKey() ;
                        ECP = genKey.getPrivKey().getParams();
                        MessageData dt = CryptoOperation.msgEncrypt(messageText,ECP,pubkeyPoint);
                        String [] msgEnc = dt.decapPC();
                        String cipherText = ArrUtil.arrayDelimited(msgEnc);
                        String kg=dt.decapKG()[0]+","+dt.decapKG()[1];
                        //Toast.makeText(Chat.this,msgEnc[0], Toast.LENGTH_SHORT).show();
                        msgMap.put("kg",kg);
                        msgMap.put("user", UserDetails.username);
                        msgMap.put("time", currentDateandTime);
                        msgMap.put("msg",cipherText);

                        ref3.push().setValue(msgMap);
                        ref4.push().setValue(msgMap);
                        //Intent back = new Intent(Chat.this,FetchPublicKey.class);
                    }
                    catch(NoSuchAlgorithmException e){
                        e.printStackTrace();
                        Toast.makeText(Chat.this,"Exception occured: No Such Algo", Toast.LENGTH_SHORT).show();
                    }
                    catch(InvalidAlgorithmParameterException e){
                        e.printStackTrace();
                        Toast.makeText(Chat.this,"Exception occured: Invalid Algo", Toast.LENGTH_SHORT).show();
                    }
                    catch(InvalidParameterSpecException e){
                        e.printStackTrace();
                        Toast.makeText(Chat.this,"Exception occured: Invalid Parameter Spec", Toast.LENGTH_SHORT).show();
                    }
               }

            }
    });
ref3.addChildEventListener(new ChildEventListener() {
    @Override
    public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
        Map map = dataSnapshot.getValue(Map.class);

        String kg = map.get("kg").toString();
        String toDeciph =map.get("msg").toString();
        String time = map.get("time").toString();
        String userName = map.get("user").toString();
        String[] kgPoint = kg.split( "," );
        msgDeciph = toDeciph.split(";");
        MessageData dt = new MessageData (kgPoint,msgDeciph);
        String fetchPrivate;
        if(userName.equals(UserDetails.username)){
            fetchPrivate = UserDetails.chatWith;
        }
        else fetchPrivate=UserDetails.username;
        try {
            GenerateKey genKey =new GenerateKey() ;
            ECP = genKey.getPrivKey().getParams();
            String privKey = FileReadWrite.readTextFile(Chat.this, fetchPrivate);
            String msgResult = CryptoOperation.msgDecrypt(dt, ECP, new BigInteger(privKey));
            Toast.makeText(Chat.this, privKey, Toast.LENGTH_SHORT).show();

            if(userName.equals(UserDetails.username)){
                addMessageBox("You " , msgResult,time, 1);
            }
            else{
                addMessageBox(UserDetails.chatWith , msgResult,time, 2);
            }
        }

        catch(GeneralSecurityException e){
            e.printStackTrace();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
});
 /*       reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                String time = map.get("time").toString();
                //message = msgToDisplay;
                if(userName.equals(UserDetails.username)){
                    addMessageBox("You " , message,time, 1);
                }
                else{
                    addMessageBox(UserDetails.chatWith , message,time, 2);
                }
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    */}

    public void addMessageBox(String name,String message,String time, int type){

        TextView textmsg = new TextView(Chat.this);
        TextView textname = new TextView(Chat.this);
        TextView texttime = new TextView(Chat.this);

        textname.setText(name);
        textname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        textmsg.setText(message);
        texttime.setText(time);
        texttime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp1.gravity = Gravity.RIGHT;
            lp2.gravity = Gravity.RIGHT;
            lp3.gravity = Gravity.RIGHT;
            textmsg.setBackgroundResource(R.drawable.text_in);

        }
        else{
            lp1.gravity = Gravity.LEFT;
            lp2.gravity = Gravity.LEFT;
            lp3.gravity = Gravity.LEFT;
            textmsg.setBackgroundResource(R.drawable.text_out);
        }


        textname.setLayoutParams(lp1);
        textmsg.setLayoutParams(lp2);
        texttime.setLayoutParams(lp3);

        layout.addView(textname);
        layout.addView(textmsg);
        layout.addView(texttime);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

 /*   private void collectMsg(Map<String,Object> users) {
        List<String> phoneNumbers = new ArrayList<String>();
       String[] msg;

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){
int i =0;
            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            msg[i]=singleUser.get(Integer.toString(i));
        i++;
        }*/
}