package com.example.nls;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnGte100, btnLt100, btnLt60;
    TextView txtTitle, txtStatus, txtTimer;
    TextView txtChogi, txtYangap, txtMrsopa, txtGigwan, txtHeart, txtEpinephrine;

    short flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGte100 = findViewById(R.id.gte_100);
        btnLt100 = findViewById(R.id.lt_100);
        btnLt60 = findViewById(R.id.lt_60);

        txtStatus = findViewById(R.id.status);
        txtTimer = findViewById(R.id.timer);

        txtChogi = findViewById(R.id.chogi);
        txtYangap = findViewById(R.id.yangap);
        txtMrsopa = findViewById(R.id.mrsopa);
        txtGigwan = findViewById(R.id.gigwan);
        txtHeart = findViewById(R.id.heart);
        txtEpinephrine = findViewById(R.id.epinephrine);

        btnGte100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 플래그 0으로 변경, 버튼 색 변경
                flag = 0;

                // 1분 이상: 응급처치 종료
            }
        });

        btnLt100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 플래그 1로 변경(기본값)
                flag = 1;
            }
        });

        btnLt60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 플래그 2로 변경
                flag = 2;
            }
        });
    }
}
