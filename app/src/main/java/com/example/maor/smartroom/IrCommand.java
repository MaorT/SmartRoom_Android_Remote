package com.example.maor.smartroom;

/**
 * Created by Maor on 21/09/2016.
 */
// This class holds the Infra-red string command that will be send to the smartClock [ MQTT message]


public class IrCommand {
    private String commandName;
    private String commandData;

    public IrCommand(String commandName,String commandData)
    {
        this.commandName = commandName;
        this.commandData = commandData;
    }

    public String getCommandName()
    {
        return commandName;
    }

    public String getCommandData()
    {
        return commandData;
    }
}
