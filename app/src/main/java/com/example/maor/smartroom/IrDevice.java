package com.example.maor.smartroom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maor on 21/09/2016.
 */

// This class holds each device and its specific Ir commands

public class IrDevice {
    private String deviceName;
    private ArrayList<IrCommand> commands;

    public IrDevice(String deviceName,ArrayList<IrCommand> commands){
        this.deviceName = deviceName;
        this.commands = commands;
    }

    public IrDevice(String deviceName,String onCommand,String offCommand){
        ArrayList<IrCommand> commands = new ArrayList<>();
        commands.add(new IrCommand("OFF",offCommand));
        commands.add(new IrCommand("ON",onCommand));
        this.deviceName = deviceName;
        this.commands = commands;
    }

    public IrDevice(String deviceName,String onOffToggleCommand){
        ArrayList<IrCommand> commands = new ArrayList<>();
        commands.add(new IrCommand("ON/OFF",onOffToggleCommand));
        this.deviceName = deviceName;
        this.commands = commands;
    }

    public String getDeviceName(){
        return deviceName;
    }

    public ArrayList<IrCommand> getCommands(){
        return commands;
    }



}
