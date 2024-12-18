package com.example.lab9;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private LinearLayout chatLayout;
    private EditText messageArea;
    private Button sendButton;
    private final String databaseURL = "https://androidapp-1abbd-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        chatLayout = findViewById(R.id.chatLayout);
        messageArea = findViewById(R.id.messageArea);
        sendButton = findViewById(R.id.buttonChat);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 使用者登入，初始化資料庫參考
            mDatabase = FirebaseDatabase.getInstance(databaseURL).getReference("Messages");
            setupChatListener();
        }

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageArea.getText().toString().trim();
        if (!messageText.isEmpty()) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                // 準備資料
                String userId = user.getUid();
                String username = user.getEmail(); // 或使用自定義的用戶名稱

                // 新建訊息資料
                Map<String, String> message = new HashMap<>();
                message.put("user", username);
                message.put("message", messageText);

                // 寫入 Realtime Database
                mDatabase.push().setValue(message);
                messageArea.setText("");
            }
        }
    }

    private void setupChatListener() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatLayout.removeAllViews();  // 清空現有訊息
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String message = snapshot.child("message").getValue(String.class);
                    String user = snapshot.child("user").getValue(String.class);

                    // 顯示訊息
                    TextView messageView = new TextView(Chat.this);
                    messageView.setText(user + " : " + message);
                    chatLayout.addView(messageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Chat.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}