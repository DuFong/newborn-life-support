package com.example.nls;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    Button btnGte100, btnLt100, btnLt60;
    Chronometer chmTimer;
    TextView txtTitle, txtStatus;                // txtTitle은 추후 실제 심박수를 측정하는 위젯으로 변경
    TextView txtChogi, txtYangap, txtMrsopa, txtGigwan, txtHeart, txtEpinephrine;

    short flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.start);
        btnGte100 = findViewById(R.id.gte_100);
        btnLt100 = findViewById(R.id.lt_100);
        btnLt60 = findViewById(R.id.lt_60);

        chmTimer = findViewById(R.id.timer);

        txtTitle = findViewById(R.id.title);
        txtStatus = findViewById(R.id.status);

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
                btnGte100.setBackgroundResource(R.drawable.btn_redcolor);
                btnLt100.setBackgroundColor(Color.WHITE);
                btnLt60.setBackgroundColor(Color.WHITE);

                // 1분 이상: 응급처치 종료
            }
        });

        btnLt100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 플래그 1로 변경(기본값), 버튼 색 변경
                flag = 1;
                btnGte100.setBackgroundColor(Color.WHITE);
                btnLt100.setBackgroundColor(0xFF8C00);
                btnLt60.setBackgroundColor(Color.WHITE);
            }
        });

        btnLt60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 플래그 2로 변경, 버튼 색 변경
                flag = 2;
                btnGte100.setBackgroundColor(Color.WHITE);
                btnLt100.setBackgroundColor(Color.WHITE);
                btnLt60.setBackgroundColor(0xFF8C00);
            }
        });

        chmTimer.start();
        chmTimer.getContentDescription();

        txtTitle.setText(chmTimer.getContentDescription());
    }
}
