package com.example.maor.smartroom;
/**
 * Created by Maor on 21/09/2016.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;



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
                        MQTT.Publish(Preferences.mqtt_out_topic, command.getCommandData());
                }
            });
            buttonLayout.addView(btn);
        }
        textView.setText(device.getDeviceName());

        return rowView;
    }
}
