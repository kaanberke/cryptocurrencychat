package com.project.cryptocurrencychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Adapters.MessageAdapter;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private User u;
    private List<Message> messages;

    private RecyclerView rvMessage;
    private EditText edtTxtMessage;
    private ImageButton imgBtn;
    private TextView tvPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseDatabase.getInstance();
        u = new User();

        rvMessage = (RecyclerView) findViewById(R.id.rvMessage);
        edtTxtMessage = (EditText) findViewById(R.id.edtTxtMessage);
        imgBtn = (ImageButton) findViewById(R.id.imgBtn);
        imgBtn.setOnClickListener(this);
        messages = new ArrayList<>();
        tvPrice = (TextView) findViewById(R.id.tvPrice);

        final TextView textView = (TextView) findViewById(R.id.text);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.coinbase.com/v2/prices/spot?currency=USD";

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject reader = new JSONObject(response);
                                    JSONObject data = reader.getJSONObject("data");
                                    Double price = data.getDouble("amount");
                                    if (!tvPrice.getText().toString().trim().equals("$")){
                                        if (Double.parseDouble(tvPrice.getText().toString().trim().substring(1)) < price){
                                            tvPrice.setTextColor(Color.GREEN);
                                        }
                                        else if (Double.parseDouble(tvPrice.getText().toString().trim().substring(1)) > price){
                                            tvPrice.setTextColor(Color.RED);
                                        }
                                    }
                                    tvPrice.setText("$" + price);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // tvPrice.setText("That didn't work!");
                    }
                });

                queue.add(stringRequest);            }
        }, 0, 10000);//put here time 1000 milliseconds=1 second

    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(edtTxtMessage.getText().toString().trim())){
            Message message = new Message(edtTxtMessage.getText().toString().trim(), u.getName());
            reference.push().setValue(message);
            edtTxtMessage.setText("");
        }
        else{
            edtTxtMessage.setError("You cannot send an empty message!");
            edtTxtMessage.requestFocus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        u.setEmail(currentUser.getEmail());

        mDB.getReference("Users")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        u = snapshot.getValue(User.class);
                        AllMethods.name = u.getName();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        reference = mDB.getReference("messages");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                message.setKey(snapshot.getKey());
                messages.add(message);
                displayMessages(messages);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                message.setKey(snapshot.getKey());

                List<Message>  newMessages = new ArrayList<Message>();
                for (Message m: messages){
                    if (m.getKey().equals(message.getKey())){
                        newMessages.add(message);
                    }
                    else {
                        newMessages.add(m);
                    }
                }
                messages = newMessages;
                displayMessages(messages);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Message message = snapshot.getValue(Message.class);
                message.setKey(snapshot.getKey());
                List<Message> newMessages = new ArrayList<Message>();
                for (Message m: messages){
                    if(!m.getKey().equals(message.getKey())){
                        newMessages.add(m);
                    }
                }
                messages = newMessages;
                displayMessages(messages);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuLogout){
            mAuth.signOut();
            finish();
            startActivity(new Intent(GroupChatActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messages = new ArrayList<>();

    }

    private void displayMessages(List<Message> messages) {
        if (messages.size() > 250) {
            messages = messages.subList(messages.size()-250, messages.size());
        }
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false);
        rvMessage.setLayoutManager(manager);
        rvMessage.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(GroupChatActivity.this, messages, reference);
        rvMessage.setAdapter(messageAdapter);
    }

    public void goBottom(View view) {
        rvMessage.scrollToPosition(messages.size()-1);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(GroupChatActivity.this, MainActivity.class));
        finish();
    }
}