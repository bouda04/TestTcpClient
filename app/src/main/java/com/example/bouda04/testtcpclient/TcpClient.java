package com.example.bouda04.testtcpclient;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bouda04 on 19/2/2017.
 */

public class TcpClient {

    private static TcpClient instance;
    private TcpHandler myTcpHandler;
    private static final int SERVER_PORT = 12900;
    private static final String SERVER_ADDRESS = "10.0.0.26";

    public static final String TCP_RESULT_ACTION = "com.testtcpclient.tcp-result";



    private Context context;

    public static TcpClient getInstance() {
        if(instance == null) {
            instance = new TcpClient();
        }
        return instance;
    }

    private TcpClient(){
        this.context = MyApplication.getContext();
        Intent intent = new Intent(context, TcpHandler.class);
        if (!isMyServiceRunning(TcpHandler.class)) {
            Log.d("TctTest", "startService");
            context.startService(intent);
        }
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myTcpHandler = ((TcpHandler.MyBinder) iBinder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {}
        }, Context.BIND_AUTO_CREATE);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MyApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void send(byte[] data){
        myTcpHandler.send(data);
    }

    public void close() {
            context.stopService(new Intent(context, TcpHandler.class));
            instance = null;
    }


    public static class TcpHandler extends Service{
        public final static int NOTIF_TCP_ID =1;
        private Notification.Builder mNotifyBuilder;
        private Socket socket;
        private InputStream in;
        OutputStream out;

        public void send(byte[] data){
            try {
                out = socket.getOutputStream();
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
//            this.socket = intent.getParcelableExtra(SOCKET_KEY);
            Log.d("TctTest", "onStartCommand");
            startForeground(NOTIF_TCP_ID, createNotification());//this to force the service to stay alive even when leaving the application
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
                        Log.d("TctTest", "new Socket");
                        listenToServer(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
            return Service.START_NOT_STICKY;
         }


        private void listenToServer(Socket socket){
            try {

                in = socket.getInputStream();
                byte[] data = new byte[1024];
                int size=0;
                while (size != -1) {
                    size = in.read(data);
                    Intent i = new Intent(TCP_RESULT_ACTION);
                    i.putExtra("data", data);
                    updateNotification(NOTIF_TCP_ID, new String(data));
                    sendBroadcast(i);
                }
            } catch (IOException e) {
                Log.d("TctTest", "listenToServer - exception");
                e.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            Log.d("TctTest", "onDestroy");
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return new MyBinder();
        }

        public class MyBinder extends Binder {
            TcpHandler getService() {
                return TcpHandler.this;
            }
        }

        private Notification createNotification(){
            Context context = MyApplication.getContext();
            Intent targetIntent = new Intent(context, MainActivity.class);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            targetIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            this.mNotifyBuilder = new Notification.Builder(context);

            mNotifyBuilder.setTicker("TcpClient")
                    .setContentTitle("New info")
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.drawable.ic_server)
                    .setOnlyAlertOnce(false);
            return mNotifyBuilder.build();
        }

        public void updateNotification(int notid, String details){
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotifyBuilder.setContentText(details).setOnlyAlertOnce(false);
            Notification noti = mNotifyBuilder.build();
            noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            mNotifyMgr.notify(notid, noti);
        }
    }


    public static class MyNotification {
/*








        public void stop(int notid){
            mNotiMgr.cancel(notid);
        }
*/
    }

}
