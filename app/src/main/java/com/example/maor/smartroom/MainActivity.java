package com.example.maor.smartroom;


import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static MQTT mqtt;
    Thread muThread = null;

    // MQTT Variable todo : move to save preference
    public static String mqtt_in_topic = "sClock_reply";
    public static String mqtt_out_topic = "sClock_cmd";
    public static String mqtt_server_address = "m12.cloudmqtt.com";
    public static String mqtt_userName = "xxxx";
    public static String mqtt_password = "xxxx";



    private boolean jsonFileOk = true;

    TextView viewerConnectionTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = (ListView) findViewById(R.id.listViewButtons);
        viewerConnectionTxt = (TextView)findViewById(R.id.textViewStatus);

        ArrayList<IrDevice> devices = new ArrayList<>();


        // Load the Devices and Commands from a JSON file
        try {
            String jsonStr = OpenFile();
            JSONArray jsonDevices_array = new JSONArray(jsonStr);
            String deviceName = "";
            ArrayList<IrCommand> IrCommands = null;

            for (int i = 0; i < jsonDevices_array.length(); i++) {
                JSONObject jsonDevice = jsonDevices_array.getJSONObject(i);
                deviceName = jsonDevice.getString("deviceName");
                IrCommands = new ArrayList<>();
                String commands = jsonDevice.getString("commands");
                JSONArray jsonCommands_array = new JSONArray(commands);
                for (int j = 0; j < jsonCommands_array.length(); j++) {
                    JSONObject jsonCommand = jsonCommands_array.getJSONObject(j);
                        String commandName = jsonCommand.getString("cmdName");
                        String commandData = jsonCommand.getString("cmdData");
                        IrCommands.add(new IrCommand(commandName,commandData));
                     }

                IrDevice device = new IrDevice(deviceName,IrCommands);
                devices.add(device);
                }

        }
        catch (Exception ex)
        {
            System.out.println(ex);
            jsonFileOk = false; // if where errors during JSON parsing
        }

        if(!jsonFileOk)
        {

            Toast.makeText(getBaseContext(),"Can't load JSON file, please check it", Toast.LENGTH_SHORT).show();
            return;
        }
        // Add all devices and their buttons to the listview
        MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,devices);
        adapter.SetMainActivity(this);
        lv.setAdapter(adapter);


        mqtt = new MQTT();

        RunConnectionAndSubscribeThread();
        CheckForNewMessage_RunThread();

    }

    private void RunConnectionAndSubscribeThread()
    {
        muThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ClientId = System.getProperty("user.name") + "." + System.currentTimeMillis();
                // must connect first time otherwise it will be null and can't ask isConnected
                //todo: change to SettingsActivity values
               // mqtt.Connect(SettingsActivity.GetServerIP(), ClientId);
               // mqtt.Subscribe(SettingsActivity.Get_MQTT_Topic());

                mqtt.Connect(mqtt_server_address,16666,ClientId,mqtt_userName,mqtt_password);

                mqtt.Subscribe(mqtt_in_topic);


                while(!isDestroyed()) //while activity is running
                {
                    //todo: change to SettingsActivity values
                   // final String newIp = SettingsActivity.GetServerIP();
                  //  final String newTopic = SettingsActivity.Get_MQTT_Topic();
                    final String newIp = mqtt_server_address;
                    final String newTopic = mqtt_in_topic;
                    //  Log.d("mbed_log","ViewerCompassThread");

                    if(!mqtt.IsConnected())
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewerConnectionTxt.setText("Disconnected");
                                viewerConnectionTxt.setTextColor(Color.RED);
                               // txtServerIP.setText(newIp);
                            }
                        });
                        // todo:change to connect with values from settings
                       // mqtt.Connect(newIp, ClientId);
                        mqtt.Connect(newIp,16666,ClientId,mqtt_userName,mqtt_password);
                        mqtt.Subscribe(newTopic);
                        try { Thread.sleep(50);} // todo: change re-connect timing
                        catch (InterruptedException e) {e.printStackTrace();}

                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewerConnectionTxt.setText("Connected");
                                viewerConnectionTxt.setTextColor(Color.parseColor("#8BC34A")); //green
                               // txtServerIP.setText(newIp);
                               // CheckForNewMessage();
                            }
                        });
                        try {Thread.sleep(1000);}
                        catch (InterruptedException e) {e.printStackTrace();}

                    }
                }
                mqtt.Disconnect();
            }
        });
        muThread.start();
    }


    public void CheckForNewMessage() // todo: delete this func ?
    {

        Thread thread;

       // Log.d("SmartRoom","entered to CheckForNewMessage function");

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("SmartRoom","thread runing");
                int triesConuter = 20;
                while(!mqtt.HaveMessage() && triesConuter > 0 ){
                    try {Thread.sleep(250);}
                    catch (InterruptedException e) {e.printStackTrace();}
                    triesConuter--;
                }
                if(mqtt.HaveMessage()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), mqtt.Get_Last_Subscribe_Msg().replace("replied:", ""), Toast.LENGTH_SHORT).show();
                            Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(30);
                        }
                    });

//            // Vibrate for 500 milliseconds
//            v.vibrate(100);
                }

            }
        });


        thread.start();


    }

    public void CheckForNewMessage_RunThread()
    {
        Thread thread;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(!isDestroyed()){
                    if(mqtt.HaveMessage()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), mqtt.Get_Last_Subscribe_Msg().replace("replied:", ""), Toast.LENGTH_SHORT).show();
                                Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                vibe.vibrate(50);
                            }
                        });
                        try {Thread.sleep(500);}
                        catch (InterruptedException e) {e.printStackTrace();}
                     }
                    else {
                        try {Thread.sleep(100);}
                        catch (InterruptedException e) {e.printStackTrace();}
                    }
                }
            }
        });
        thread.start();
    }

    public String OpenFile()
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("commands.txt")));

            // do reading, usually loop until end of file reading
            String data = "";
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                data += mLine;
            }

            return data;

        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return "";
    }
}

