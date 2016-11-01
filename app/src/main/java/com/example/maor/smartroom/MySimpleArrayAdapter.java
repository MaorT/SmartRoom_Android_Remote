package com.example.maor.smartroom;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Looper.loop;
import static com.example.maor.smartroom.MainActivity.mqtt;

/**
 * Created by Maor on 21/09/2016.
 */

public class MySimpleArrayAdapter extends ArrayAdapter<IrDevice> {
    private final Context context;
    private final  ArrayList<IrDevice> devices;
    private MainActivity mainActivity;


    public MySimpleArrayAdapter(Context context, ArrayList<IrDevice> devices) {
        super(context, -1, devices);
        this.context = context;
        this.devices = devices;

    }

    public void SetMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.label);

        IrDevice device = devices.get(position);
        LinearLayout buttonLayout = (LinearLayout)rowView.findViewById(R.id.layoutDeviceButtons);
        final ArrayList<IrCommand> commands = device.getCommands();

        for(int i=0 ; i< commands.size() ; i++){
            final IrCommand command = commands.get(i);
            Button btn = new Button(buttonLayout.getContext());
            btn.setText(command.getCommandName());
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mqtt != null) {
                        mqtt.Publish(MainActivity.mqtt_out_topic, command.getCommandData());
                    //    mainActivity.CheckForNewMessage();
                      //  final Thread thread = null;
                    }
                }
            });
            buttonLayout.addView(btn);
        }
        textView.setText(device.getDeviceName());

        return rowView;
    }
}
