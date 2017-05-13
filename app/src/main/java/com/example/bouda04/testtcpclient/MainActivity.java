package com.example.bouda04.testtcpclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    TcpClient tcp;
    TextView tvResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tcp = TcpClient.getInstance();
        BroadcastReceiver br = new TcpReceiver();

        this.registerReceiver(br, new IntentFilter(TcpClient.TCP_RESULT_ACTION));
        tvResult = (TextView) findViewById(R.id.result);
        final EditText edInput = (EditText) findViewById(R.id.input);
        Button btnSend = (Button) findViewById(R.id.send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = String.valueOf(edInput.getText());
                if (!input.isEmpty()){
                    byte[] data = input.getBytes();
                    tcp.send(data);
                }
            }
        });
    }

    public class TcpReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            byte[] data = intent.getByteArrayExtra("data");
            tvResult.setText(new String(data));
        }
    }
}
