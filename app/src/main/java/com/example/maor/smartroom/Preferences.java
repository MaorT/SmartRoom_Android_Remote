package com.example.maor.smartroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {

    private static Context context = null;

    // MQTT Variable todo : move some of them to the save preference
    // private  MqttClient client = null;
    public static String mqtt_in_topic = "sClock_reply";
    public static String mqtt_out_topic = "sClock_cmd";
    public static String mqtt_server_address = "m12.cloudmqtt.com";
    public static String mqtt_userName = "androidG2";
    public static String mqtt_password = "Ag321";
    public static int mqtt_port = 16666; // for demo, now using 16666
    public static String notification_ringtone = "DEFAULT_RINGTONE_URI";
    public static boolean notification_vibration = true;
    public static boolean notification_sound = true;


    // private static boolean settingsChanged_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    /****************************/
    /**   Preference Fragment   **/
    /****************************/

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener
    {
        @Override

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String preferenceName = preference.getKey();

            /** General validators - No empty setting at all **/
            if(newValue.toString().length() == 0)
            {
                Toast.makeText(getActivity(),"Empty value not allowed! Try again",Toast.LENGTH_SHORT).show();
                return false;
            }

            /** Set validators **/
            switch (preferenceName){
                case "mqttUserName":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Username can't be empty! try again ",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case "mqttUserPassword":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Password can't be empty! try again ",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case "mqttServerUrl":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Server Url can't be empty! try again ",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
            }

            if(preferenceName.startsWith("mqtt")){
                Toast.makeText(getActivity(),"New MQTT settings - please restart the service",Toast.LENGTH_SHORT).show();
                return true;
            }

            Toast.makeText(getActivity(),"The new " + preferenceName+" has been saved",Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            /** Get each preference and set it's PreferenceChangeListener **/
            SetPreferenceListenerByKey("mqttUserName");
            SetPreferenceListenerByKey("mqttUserPassword");
            SetPreferenceListenerByKey("mqttServerUrl");
            SetPreferenceListenerByKey("mqttServerPort");
            SetPreferenceListenerByKey("notification_sound");
            SetPreferenceListenerByKey("notification_vibration");
            SetPreferenceListenerByKey("notification_ringtone");
        }

        private void SetPreferenceListenerByKey(String keyName){

            final Preference pref = getPreferenceScreen().findPreference(keyName);
            pref.setOnPreferenceChangeListener(this);

        }


        // When any preference change has been made, used to update the changes immediately
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            LoadPreferences(context);
        }


        // Register and Unregister the OnSharedPreferenceChangeListener
        // When any preference is changed, it runs "onSharedPreferenceChanged"
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }/// End of the internal class


    public static void LoadPreferences(Context context){

        if(context == null)
            return;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

        mqtt_userName = SP.getString("mqttUserName", "NA");
        mqtt_password = SP.getString("mqttUserPassword", "undefined");
        mqtt_server_address = SP.getString("mqttServerUrl", "undefined.mqtt.com");
        mqtt_in_topic = SP.getString("mqttInTopic", "undefined");
        mqtt_out_topic = SP.getString("mqttOutTopic", "undefined");
        mqtt_port = Integer.parseInt(SP.getString("mqttServerPort", "1883"));
        notification_vibration = SP.getBoolean("notification_vibration", false);
        notification_sound = SP.getBoolean("notification_sound", false);
        notification_ringtone = SP.getString("notification_ringtone", "DEFAULT_RINGTONE_URI");
    }

    public static void SetContext(Context newContext){
        context = newContext;
    }

} /// End of the Preferences
