package com.example.maor.smartroom;
/**
 * Created by Maor on 21/09/2016.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Wrapper;
import java.util.ArrayList;



public class MySimpleArrayAdapter extends ArrayAdapter<IrDevice> {
    private final Context context;
    private final  ArrayList<IrDevice> devices;
    private MainActivity mainActivity;
    private int buttonsPerLine = 4;


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
        // todo : get the actual width, it's not 720 constant!!
        int width = 720-(buttonsPerLine-1)*8; // remove num of gaps between the buttons multiple by the gap
        int buttonWidth = width/buttonsPerLine;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(buttonWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(8,8,8,8);
//        buttonLayout.setLayoutParams(lp);
        final ArrayList<IrCommand> commands = device.getCommands();


        for(int i=0 ; i< commands.size() ; i += buttonsPerLine){

            // For each X items, create a new layout (new buttons row)
            LinearLayout newLayout = new LinearLayout(context);
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
          //  newLayout.setLayoutParams(lp);
            newLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            // Add buttons to the specific layout
            for(int j=0 ; j < buttonsPerLine && i+j< commands.size()  ; j++)
            {
                final IrCommand command = commands.get(i+j);
                Button btn = new Button(buttonLayout.getContext());
                btn.setLayoutParams(lp);
                btn.setBackgroundResource(R.drawable.button_shape);
            //    btn.setPadding(15,0,15,0);

                btn.setText(command.getCommandName());
                btn.setTextColor(Color.WHITE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MQTT.Publish(Preferences.mqtt_out_topic, command.getCommandData());
                    }
                });
                // buttonLayout.addView(btn);
                newLayout.addView(btn);
            }

            buttonLayout.addView(newLayout); // Add new layout into the static layout

        }

      //  buttonLayout.addView(newLayout1);

        textView.setText(device.getDeviceName());

        return rowView;
    }
}
