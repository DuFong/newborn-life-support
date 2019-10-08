package com.example.nls;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    Button btnGte100, btnLt100, btnLt60;
    TextView txtTitle, txtStatus;                // txtTitle은 추후 실제 심박수를 측정하는 위젯으로 변경
    TextView txtChogi, txtYangap, txtMrsopa, txtGigwan, txtHeart, txtEpinephrine;

    static Timer timer;
    static Chronometer chmTimer;

    Handler handler;

    short flag = 1;
    boolean restartGigwan = false;
    boolean isTimerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.start);
        btnGte100 = findViewById(R.id.gte_100);
        btnLt100 = findViewById(R.id.lt_100);
        btnLt60 = findViewById(R.id.lt_60);

        txtTitle = findViewById(R.id.title);
        txtStatus = findViewById(R.id.status);

        txtChogi = findViewById(R.id.chogi);
        txtYangap = findViewById(R.id.yangap);
        txtMrsopa = findViewById(R.id.mrsopa);
        txtGigwan = findViewById(R.id.gigwan);
        txtHeart = findViewById(R.id.heart);
        txtEpinephrine = findViewById(R.id.epinephrine);

        handler = new Handler();

        // 타이머 시작
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start 버튼 숨기기
                btnStart.setVisibility(View.GONE);

                txtChogi.setBackgroundResource(R.drawable.r_chogi);

                chmTimer = findViewById(R.id.timer);
                chmTimer.setBase(SystemClock.elapsedRealtime());
                timer = new Timer();
                isTimerStarted = true;

                // 1분 경과
                TimerTask task1M = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우(flag 값 = 1) 양압환기
                        if(flag != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtStatus.setText("호흡음 청진");
                                    txtYangap.setBackgroundResource(R.drawable.r_yangap);
                                    txtChogi.setBackgroundResource(R.drawable.b_chogi);
                                }
                            });
                        }
                        else {
                            setInitialActivity();
                        }
                    }
                };
                // 1분 30초 경과
                TimerTask task1M30S = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우(flag = 1) 양압환기, MRSOPA
                        if(flag != 0) {
                            txtMrsopa.setBackgroundResource(R.drawable.r_mrsopa);
                            //txtYangap.setBackgroundResource(R.drawable.b_yangap);
                        }
                    }
                };
                // 2분 경과
                TimerTask task2M = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우 기관삽관
                        if(flag != 0) {
                            txtGigwan.setBackgroundResource(R.drawable.r_gigwan);;
                            txtMrsopa.setBackgroundResource(R.drawable.b_mrsopa);
                        }
                    }
                };
                // 2분 30초 경과
                TimerTask task2M30S = new TimerTask() {
                    @Override
                    public void run() {
                        afterGigwan();
                    }
                };

                timer.schedule(task1M, 60000 - 7);
                timer.schedule(task1M30S, 90000 - 7);
                timer.schedule(task2M, 120000 - 7);
                timer.schedule(task2M30S, 150000 - 7);

                chmTimer.start();
            }
        });

        btnGte100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimerStarted) {
                    // 플래그 0으로 변경, 버튼 색 변경
                    flag = 0;
                    btnGte100.setBackgroundResource(R.drawable.btn_clicked_gt100);
                    btnLt100.setBackgroundResource(R.drawable.btn_hr_lt100);
                    btnLt60.setBackgroundResource(R.drawable.btn_hr_lt60);

                    // 1분 이상: 응급처치 종료
                    long currentTime = SystemClock.elapsedRealtime() - chmTimer.getBase();
                    if(currentTime > 60000) {
                        setInitialActivity();
                    }
                }
            }
        });

        btnLt100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimerStarted) {
                    // 플래그 1로 변경(기본값), 버튼 색 변경
                    flag = 1;
                    btnGte100.setBackgroundResource(R.drawable.btn_hr_gt100);
                    btnLt100.setBackgroundResource(R.drawable.btn_clicked_lt100);
                    btnLt60.setBackgroundResource(R.drawable.btn_hr_lt60);
                }
            }
        });

        btnLt60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimerStarted) {
                    // 플래그 2로 변경, 버튼 색 변경
                    flag = 2;
                    btnGte100.setBackgroundResource(R.drawable.btn_hr_gt100);
                    btnLt100.setBackgroundResource(R.drawable.btn_hr_lt100);
                    btnLt60.setBackgroundResource(R.drawable.btn_clicked_lt60);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chmTimer.stop();
    }

    private void afterGigwan() {
        if(flag == 1){
            // 60이상 100 미만
            restartGigwan = false;
            turnOffAllAttributes();
            txtGigwan.setBackgroundResource(R.drawable.r_gigwan);
        }
        else if(flag == 2) {
            // 60 미만
            turnOffAllAttributes();
            if(restartGigwan) {
                txtEpinephrine.setBackgroundResource(R.drawable.r_epineprine);
            }
            restartGigwan = true;
            txtYangap.setBackgroundResource(R.drawable.r_yangap);
            txtHeart.setBackgroundResource(R.drawable.r_heart);
        }
        // 현재 지난 시간
        long currentTime = SystemClock.elapsedRealtime() - chmTimer.getBase();
        // 10분 경과
        if(currentTime > 600000) {
            setInitialActivity();
            return;
        }
        TimerTask test = new TimerTask() {
            @Override
            public void run() {
                afterGigwan();
            }
        };
        timer.schedule(test, 30000 - 7);
    }

    private void turnOffAllAttributes() {
        txtChogi.setBackgroundResource(R.drawable.b_chogi);
        txtYangap.setBackgroundResource(R.drawable.b_yangap);
        txtMrsopa.setBackgroundResource(R.drawable.b_mrsopa);
        txtGigwan.setBackgroundResource(R.drawable.b_gigwan);
        txtHeart.setBackgroundResource(R.drawable.b_heart);
        txtEpinephrine.setBackgroundResource(R.drawable.b_epineprine);
    }

    private void setInitialActivity() {
        Toast.makeText(MainActivity.this, "신생아 소생술 종료", Toast.LENGTH_SHORT).show();

        chmTimer.stop();
        chmTimer.setBase(SystemClock.elapsedRealtime());
        timer.cancel();
        timer = null;

        flag = 1;
        isTimerStarted = false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                btnStart.setVisibility(View.VISIBLE);

                btnGte100.setBackgroundResource(R.drawable.btn_hr_gt100);
                btnLt100.setBackgroundResource(R.drawable.btn_hr_lt100);
                btnLt60.setBackgroundResource(R.drawable.btn_hr_lt60);

                txtStatus.setText("초기처치");

                turnOffAllAttributes();
            }
        });
    }
}
