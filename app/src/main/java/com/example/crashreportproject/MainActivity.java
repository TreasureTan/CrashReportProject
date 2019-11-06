package com.example.crashreportproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ysten.ystenreport.YstenLogReport;
import com.ysten.ystenreport.crash.CrashHandler;


public class MainActivity extends AppCompatActivity {
    private static final String test_host = "http://tangula.ysten.com:8091/apm/v1/";
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///初始化上报
        YstenLogReport.getInstance().init(MainActivity.this, "12345",true);
        YstenLogReport.getInstance().setReportHost(test_host);

        Button btn_crash = findViewById(R.id.btn_crash);
        Button btn_anr = findViewById(R.id.btn_anr);
        Button btn_exception = findViewById(R.id.btn_exception);
        Button btn_setUrl = findViewById(R.id.btn_setUrl);
        btn_crash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: 2019-10-24  触发crash异常

                crashForLib();
//                crashFor0();
//                crashForNoId();

            }
        });

        btn_anr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: 2019-10-24  触发anr异常
                //                sleepTest();
                forTest();
            }
        });

        btn_exception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YstenLogReport.getInstance().getAnrMessage();
            }
        });

        btn_setUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YstenLogReport.getInstance().setReportHost(test_host);
            }
        });
    }

    public void sleepTest() {
        SystemClock.sleep(300000);
    }

    public void forTest() {
        for (; ; ) {
        }
    }

    private void crashFor0() {
        int i = 0;
        int j = 1;
        i = j / i;
    }

    private void crashForLib() {
        CrashHandler.getInstance().init2();
    }

    private void crashForNoId() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
