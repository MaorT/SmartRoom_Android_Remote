package com.example.maor.smartroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;

import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MQTT extends Service implements MqttCallback {

    private static MqttClient client;
    final static String LOG_TAG = "smartRoom";
    public static final String mBroadcastStringAction = "com.example.maorservice.string";
    String ClientId = System.getProperty("user.name") + "." + System.currentTimeMillis(); // Generate a unique user id
    private static boolean serviceOnFlag = false; //helps to know the service status (run/stop)
    Notification notification = null;

    //region LifeCycle region

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"On start command");
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            //context = this.getBaseContext();
            Preferences.LoadPreferences(getApplicationContext());

            Connect(Preferences.mqtt_server_address,Preferences.mqtt_port,ClientId,
                    Preferences.mqtt_userName,Preferences.mqtt_password);

            Subscribe(Preferences.mqtt_in_topic);
            serviceOnFlag = true;
            notification = BuildForegroundNotification(getString(R.string.app_name),"Connecting");
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);



            if(IsConnected()){
                Update_Foreground_Notification_Text("Connected"); // todo : Do something when unsuccessful connection
                NotifyBroadcast("system","connected",false);
            }

        }
        else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       // Log.d(LOG_TAG,"SmartRoom MQTT on destroy");
        serviceOnFlag = false;
        if(client != null && IsConnected()){
            UnSubscribe(Preferences.mqtt_in_topic);
            Disconnect();
        }
        //Toast.makeText(getApplicationContext(), "Smartroom Service has been stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // Toast.makeText(getApplicationContext(), "Smartroom Service has been started", Toast.LENGTH_SHORT).show();
      //  Log.d(LOG_TAG,"SmartRoom MQTT on create");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Wont be called as service is not bound
        return null;
    }

    //endregion


    //region Connection and publishing region

    public void Connect(String url,int port,String clientID) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            client = new MqttClient("tcp://" + url + ":"+port, clientID, persistance);
            client.connect();
            client.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Connect(String url, int port, String clientID, MqttConnectOptions options) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            client = new MqttClient("tcp://" + url + ":"+port, clientID, persistance);
            client.connect(options);
            client.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Connect(String url, int port, String clientID, String userName,String passWord) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            String brokerUrl = "tcp://" + url + ":"+port;
            client = new MqttClient(brokerUrl,clientID, persistance);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setCleanSession(true);
            client.connect(options);
            client.setCallback(this);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean IsConnected()
    {
        return client.isConnected();
    }

    public void Disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void UnSubscribe(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static boolean Publish(String topic, String payload) {
        if(client == null || !client.isConnected())
            return false;

        MqttMessage message = new MqttMessage(payload.getBytes());
        try {
            client.publish(topic, message);
            return true;
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }


    //endregion


    //region Events region

    @Override
    public void connectionLost(Throwable cause) {
        Update_Foreground_Notification_Text("Disconnected!");
        NotifyBroadcast("system","disconnected",false);
        Log.i(LOG_TAG,"SmartRoom - Connection lost");

        // Try to reconnect in a loop when the connection was lost, until service stopped or connected successfully
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!IsConnected() && serviceOnFlag)
                {
                    //  NotifyBroadcast("system","Trying to reconnect..",false);
                    Connect(Preferences.mqtt_server_address,Preferences.mqtt_port,ClientId,
                            Preferences.mqtt_userName,Preferences.mqtt_password);
                    if(!IsConnected())
                        try { Thread.sleep(10000);}
                        catch (Exception ex) {}
                }
                // NotifyBroadcast("system","Connected successfully",false);
                Update_Foreground_Notification_Text("Connected");
                NotifyBroadcast("system","connected",false);
                Log.i(LOG_TAG,"SmartRoom - Connected successfully");
                Subscribe(Preferences.mqtt_in_topic);
            }
        });
        thread.run();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        NotifyBroadcast(topic,message.toString(),message.isRetained());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

    //endregion


    //region Notifications region

    private void Update_Foreground_Notification_Text(String text){

        Notification note = BuildForegroundNotification(getString(R.string.app_name),text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,note);
    }

    private Notification BuildForegroundNotification(String title,String text)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // todo : add large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.remote1);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.remote1);
        builder.setLargeIcon(largeIcon);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    public void NotifyBroadcast(String topic,String message,boolean retained){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(mBroadcastStringAction);
        broadcastIntent.putExtra("Data",message); // Add data that is sent to service
        broadcastIntent.putExtra("Topic",topic); // Add data that is sent to service
        broadcastIntent.putExtra("Retained",retained); // Add data that is sent to service
        sendBroadcast(broadcastIntent);
    }

    //endregion

}