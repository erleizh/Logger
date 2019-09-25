package com.erlei.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.erlei.logger.LoggerFactory;
import com.erlei.logger.LoggerPrinter;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.tv_test_result);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void start(View view) {

    }
}
