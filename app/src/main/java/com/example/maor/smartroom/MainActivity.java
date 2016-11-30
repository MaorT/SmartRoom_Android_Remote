package com.example.maor.smartroom;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{


    private  String LOG_TAG = "smartRoom";

    private Intent serviceIntent;
    private IntentFilter mIntentFilter;
    public static final String mBroadcastStringAction = "com.example.maorservice.string";



    TextView viewerConnectionTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewerConnectionTxt = (TextView)findViewById(R.id.textViewStatus);
      //  Log.d(LOG_TAG,"MainActivity onCreate");
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Preferences.SetContext(this);

        // Create buttons and devices list from a json file
        JsonAsyncTask jsonTask = new JsonAsyncTask();
        try {
            if(jsonTask.execute().get() == -1){
                Toast.makeText(getBaseContext(),"Can't load the JSON file, please check the file", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        StartService(); // Run foreground service, used to listen mqtt replies

        // Register broadcast receiver, the broadcast receiver makes the toast notification for replies
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);
        registerReceiver(mReceiver,mIntentFilter);
    }


    private boolean BuildFromJson()
    {
        ListView lv = (ListView) findViewById(R.id.listViewButtons);
        ArrayList<IrDevice> devices = new ArrayList<>();
        boolean jsonFileOk = true;

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
            return false;
        }

        // Add all devices and their buttons to the listview
        MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,devices);
        adapter.SetMainActivity(this);
        lv.setAdapter(adapter);



        return true;
    }


    private void StopService(){
        if(serviceIntent == null)
            return;
        serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        stopService(serviceIntent);
        unregisterReceiver(mReceiver);
    }


    private void StartService(){
        serviceIntent = new Intent(MainActivity.this, MQTT.class);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        StopService();
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



    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
                String topic = intent.getStringExtra("Topic");
                String data = intent.getStringExtra("Data");
                boolean retained = intent.getExtras().getBoolean("Retained");

                if(data.length() == 0) // Ignore empty messages
                    return;

                // When receiving a system notifications and not a regular data
                if(topic.equals("system")){
                    if(data.equals("connected"))
                        ChangeConnectionText(true);
                    else if(data.equals("disconnected"))
                        ChangeConnectionText(false);

                    Toast.makeText(context,"Smartroom: " + data, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Preferences.notification_vibration)
                    Vibrate(100);
                Toast.makeText(context,"Replied: " + data, Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void ChangeConnectionText(boolean connected)
    {
        if(connected){
            viewerConnectionTxt.setText("Connected");
            viewerConnectionTxt.setTextColor(Color.GREEN);
        }
        else {
            viewerConnectionTxt.setText("Disconnected");
            viewerConnectionTxt.setTextColor(Color.RED);
        }
    }

    private void Vibrate(int ms){
        Vibrator v = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(ms);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    private class JsonAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            if(!BuildFromJson()) return -1;
            return 0;
        }
    } // End of JsonAsyncTask




} // End of MainActivity

