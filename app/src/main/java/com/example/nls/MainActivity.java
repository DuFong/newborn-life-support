package com.example.nls;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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

    Button btnStart, btnReset;
    Button btnGte100, btnLt100, btnLt60;
    TextView txtTitle, txtStatus;                // txtTitle은 추후 실제 심박수를 측정하는 위젯으로 변경
    TextView txtChogi, txtYangap, txtMrsopa1, txtMrsopa2, txtGigwan, txtHeart, txtEpinephrine;

    static Timer timer;
    static Chronometer chmTimer;

    // 뒤로가기 막기에서 사용하는 변수
    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;


    Handler handler;

    short flag = 1;
    boolean isRestartGigwan = false;
    boolean isTimerStarted = false;
    boolean is2MStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.start);
        btnReset = findViewById(R.id.reset);
        btnGte100 = findViewById(R.id.gte_100);
        btnLt100 = findViewById(R.id.lt_100);
        btnLt60 = findViewById(R.id.lt_60);

        txtTitle = findViewById(R.id.title);
        txtStatus = findViewById(R.id.status);

        txtChogi = findViewById(R.id.chogi);
        txtYangap = findViewById(R.id.yangap);
        txtMrsopa1 = findViewById(R.id.mrsopa1);
        txtMrsopa2 = findViewById(R.id.mrsopa2);
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
                // reset 버튼 활성화
                btnReset.setVisibility(View.VISIBLE);

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
                                    txtStatus.setBackgroundResource(R.drawable.state_listen);
                                    txtYangap.setBackgroundResource(R.drawable.r_yangap);
                                    txtChogi.setBackgroundResource(R.drawable.b_chogi);
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finishCpr(true);
                                }
                            });
                        }
                    }
                };
                // 1분 30초 경과
                TimerTask task1M30S = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우(flag = 1) 양압환기, MRSOPA1
                        if(flag != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtMrsopa1.setBackgroundResource(R.drawable.r_mrsopa1);
                                }
                            });
                        }
                    }
                };
                // 2분 경과
                TimerTask task2M = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우 (flag = 1) 양압환기, MRSOPA2
                        if(flag != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtMrsopa1.setBackgroundResource(R.drawable.b_mrsopa1);
                                    txtMrsopa2.setBackgroundResource(R.drawable.r_mrsopa2);
                                }
                            });
                        }
                    }
                };

                TimerTask task2M30S = new TimerTask() {
                    @Override
                    public void run() {
                        // 100 미만인 경우 기관삽관
                        if(flag != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    afterGigwan();
                                }
                            });
                        }
                        // 100 이상인 경우 호흡 및 전신상태 확인
                        else {
                            // 대화상자 띄우기
                            checkBreathAndBody();
                        }

                    }
                };

                timer.schedule(task1M, 60000 - 7);
                timer.schedule(task1M30S, 90000 - 7);
                timer.schedule(task2M, 120000 - 7);
                timer.schedule(task2M30S, 150000 -7);

                chmTimer.start();
            }
        });

        // 타이머 리셋
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setInitialActivity();

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


                    long currentTime = SystemClock.elapsedRealtime() - chmTimer.getBase();
                    // 1분 이상: 종료 및 신생아실 이송
                    if(60000 <= currentTime && currentTime < 61000) {
                        finishCpr(true);
                    }
                    // 1분 1초 ~ 2분 31초 사이: 호흡 및 전신상태 확인
                    else if(61000 <= currentTime && currentTime < 151000) {
                        checkBreathAndBody();
                    }
                    // 2분 31초 이상: 종료 후 신생아 중환자실 이동
                    else if(151000 <= currentTime) {
                        finishCpr(false);
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
        if(chmTimer != null) {
            chmTimer.stop();
        }
        super.onDestroy();
    }

    private void afterGigwan() {
        if(flag == 1){
            // 60이상 100 미만
            isRestartGigwan = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    turnOffAllAttributes();
                    txtGigwan.setBackgroundResource(R.drawable.r_gigwan);
                }
            });
        }
        else if(flag == 2) {
            // 60 미만
            // 60 미만이지만 처음 2분에 실행 되는 경우 기관삽관부터 해야 함
            if(!is2MStart) {
                isRestartGigwan = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        turnOffAllAttributes();
                        txtGigwan.setBackgroundResource(R.drawable.r_gigwan);
                    }
                });
            }
            // 일반적인 60 미만 처리
            else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        turnOffAllAttributes();
                        if (isRestartGigwan) {
                            txtEpinephrine.setBackgroundResource(R.drawable.r_epineprine);
                        }
                        isRestartGigwan = true;
                        txtYangap.setBackgroundResource(R.drawable.r_yangap);
                        txtHeart.setBackgroundResource(R.drawable.r_heart);
                    }
                });
            }
        }
        is2MStart = true;
        // 현재 지난 시간
        long currentTime = SystemClock.elapsedRealtime() - chmTimer.getBase();
        // 10분 경과
        if(currentTime > 570000) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    finishCpr(false);
                }
            });
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
        txtMrsopa1.setBackgroundResource(R.drawable.b_mrsopa1);
        txtMrsopa2.setBackgroundResource(R.drawable.b_mrsopa2);
        txtGigwan.setBackgroundResource(R.drawable.b_gigwan);
        txtHeart.setBackgroundResource(R.drawable.b_heart);
        txtEpinephrine.setBackgroundResource(R.drawable.b_epineprine);
    }

    // 초기 시작 화면 복구
    private void setInitialActivity() {

        chmTimer.stop();
        chmTimer.setBase(SystemClock.elapsedRealtime());
        timer.cancel();
        timer = null;

        flag = 1;
        isRestartGigwan = false;
        isTimerStarted = false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "신생아 소생술 종료", Toast.LENGTH_SHORT).show();

                chmTimer.stop();
                chmTimer.setBase(SystemClock.elapsedRealtime());
                if(timer != null)
                    timer.cancel();
                timer = null;

                flag = 1;
                isRestartGigwan = false;
                isTimerStarted = false;

                btnStart.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.GONE);

                btnGte100.setBackgroundResource(R.drawable.btn_hr_gt100);
                btnLt100.setBackgroundResource(R.drawable.btn_hr_lt100);
                btnLt60.setBackgroundResource(R.drawable.btn_hr_lt60);

                txtStatus.setBackgroundResource(R.drawable.state_chogi);

                turnOffAllAttributes();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }

    // 호흡 및 전신상태 체크 대화상자
    private void checkBreathAndBody() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle("상태 확인");
        dialog.setMessage("호흡이 안정적이고 전신상태가 양호합니까?");

        // 예: 종료 및 신생아실 이송
        dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishCpr(true);
            }
        });

        // 아니오: 종료 후 신생아 중환자실 이동
        dialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishCpr(false);
            }
        });

        dialog.show();
    }

    // 소생술 종료 알림 대화상자
    private void finishCpr(boolean isGoToNewborn) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle("심폐소생술 종료");

        if(isGoToNewborn)
            dialog.setMessage("신생아실으로 이송합니다.");
        else
            dialog.setMessage("중환자실로 이송합니다.");


        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setInitialActivity();
            }
        });

        dialog.show();
    }
}
