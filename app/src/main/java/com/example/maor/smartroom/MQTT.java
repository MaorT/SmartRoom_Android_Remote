package com.example.maor.smartroom;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MQTT implements MqttCallback {

    private static MqttClient client;
    private static boolean newDataFlag = false;
    private String lastSubscribeMsg = "0";

    public static boolean isNewDataFlag() {
        return newDataFlag;
    }


    public MQTT() {}

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

    public  boolean Publish(String topic, String payload) {
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


    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //
       // Log.d("SmartRoom","New message arrived");
       lastSubscribeMsg = message.toString();
    //    Context context = MainActivity.GetContext();
     //   Toast.makeText(context.getApplicationContext(), message.toString().replace("replied:",""), Toast.LENGTH_LONG).show();
       // Toast.makeText(context, "dsfsfsf", Toast.LENGTH_LONG).show();
        newDataFlag = true;
        //HandleMessage(topic,message.toString());
        //todo :hanlde subscribed messages - check for right topic

    }

    public String Get_Last_Subscribe_Msg()
    {
        newDataFlag = false;
        return lastSubscribeMsg;
    }

    public boolean HaveMessage(){
        if(lastSubscribeMsg == "0")
            return false;
        return newDataFlag;
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }


    public boolean IsConnected()
    {
        return client.isConnected();
    }

}