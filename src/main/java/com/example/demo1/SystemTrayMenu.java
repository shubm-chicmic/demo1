package com.example.demo1;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
public class SystemTrayMenu {
    public void showSystemTray(){
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image trayIconImage = new ImageIcon(getClass().getResource("/download.png")).getImage(); // Replace with the actual path to your tray icon image
            TrayIcon trayIcon = new TrayIcon(trayIconImage, "Your Application Name");
            trayIcon.setImageAutoSize(true);


            PopupMenu popupMenu = new PopupMenu();

            // Create menu items
            MenuItem menuItem1 = new MenuItem("Menu Item 1");
            MenuItem menuItem2 = new MenuItem("Menu Item 2");
            MenuItem menuItem3 = new MenuItem("Menu Item 3");
            MenuItem exitMenuItem = new MenuItem("Exit");

            // Add menu items to the popup menu
            popupMenu.add(menuItem1);
            popupMenu.add(menuItem2);
            popupMenu.add(menuItem3);
            popupMenu.addSeparator();
            popupMenu.add(exitMenuItem);
            trayIcon.setPopupMenu(popupMenu);
            // Create a system tray icon

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }

            // Define actions for menu items
            menuItem1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Code to execute when Menu Item 1 is clicked
                    String answer = ChatGptApi.generateAnswer("Generate we what a intern should learn during microservice with java and springboot , generate 3 day plan");
                    System.out.println("answer = ");
                    System.out.println(answer);
                }
            });

            menuItem2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Code to execute when Menu Item 2 is clicked
                }
            });

            menuItem3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Code to execute when Menu Item 3 is clicked
                }
            });

            exitMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Code to execute when Exit is clicked
                    System.exit(0);
                }
            });


        }
    }
}
