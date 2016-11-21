package com.example.maor.smartroom;

/**
 * Created by Maor on 21/11/2016.
 */

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "com.example.maor.smartroom.action.main";
        String STARTFOREGROUND_ACTION = "com.example.maor.smartroom.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.example.maor.smartroom.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 102;
    }
}