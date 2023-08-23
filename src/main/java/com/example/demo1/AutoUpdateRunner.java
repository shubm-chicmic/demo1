package com.example.demo1;

import com.example.demo1.AutoUpdate.AutoUpdateManager;
import com.example.demo1.HelloApplication;
import com.example.demo1.TimeUpdateScheduler;
import com.example.demo1.UpdateTask;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AutoUpdateRunner implements PropertyChangeListener {
    private final HelloApplication mainApp;

    public AutoUpdateRunner(HelloApplication mainApp) {
        this.mainApp = mainApp;
    }
    TimeUpdateScheduler timeUpdateScheduler = new TimeUpdateScheduler();
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if ("autoUpdateTime".equals(evt.getPropertyName()) && (boolean) evt.getNewValue()) {
            // The value of autoUpdateTime has changed to true, execute the desired code

            UpdateTask updateTask = new UpdateTask(mainApp, mainApp.empCode[0], mainApp.timeField[0]);
            timeUpdateScheduler.startUpdateTask(updateTask);
        }else if("autoUpdateTime".equals(evt.getPropertyName()) && !(boolean) evt.getNewValue()){
            System.out.println("\u001B[33mProperty Change elwse working\u001B[0m");
//            stop the schedular
            timeUpdateScheduler.stopUpdateTask();
        }
    }

    public void setAutoUpdateTime(boolean newValue) {
        mainApp.autoUpdateTime = newValue;
        AutoUpdateManager.setAutoUpdateTime(newValue);
    }
}
