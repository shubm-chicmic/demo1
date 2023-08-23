package com.example.demo1.AutoUpdate;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AutoUpdateManager {
    private static boolean autoUpdateTime = false;
    private static PropertyChangeSupport support = new PropertyChangeSupport(new Object());

    public static boolean isAutoUpdateTime() {
        return autoUpdateTime;
    }

    public static void setAutoUpdateTime(boolean newValue) {
        boolean oldValue = autoUpdateTime;
        autoUpdateTime = newValue;
        support.firePropertyChange("autoUpdateTime", oldValue, newValue);
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
