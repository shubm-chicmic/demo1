package com.example.demo1;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import org.controlsfx.control.CheckListView;

import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;


public class SystemTrayMenu {
    public static Image resizeImage(Image originalImage, int newWidth, int newHeight) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        System.out.println("Icon width: " + resizedImage.getWidth(null) + ", height: " + resizedImage.getHeight(null));

        return resizedImage;
    }

    public void showSystemTray() {
        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image icon = new ImageIcon(getClass().getResource("/clock4.png")).getImage();
//            icon = resizeImage(icon, 100, 100);
            TrayIcon trayIcon = new TrayIcon(icon, "Your Application Name");
            trayIcon.setImageAutoSize(true);

//

            // Create menu items
            MenuItem menuItem1 = new MenuItem("Auto TimeSheet Fill");
            MenuItem menuItem2 = new MenuItem("PC Turn Off");
            MenuItem menuItem3 = new MenuItem("Always On Top");
            MenuItem exitMenuItem = new MenuItem("Exit");

            // Add menu items to the popup menu
//            PopupMenu popupMenu = new PopupMenu();
//            popupMenu.add(menuItem);
////            popupMenu.add(menuItem2);
////            popupMenu.add(menuItem3);
////            popupMenu.addSeparator();
//            popupMenu.add(exitMenuItem);
//            trayIcon.setPopupMenu(popupMenu);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
            // Create a custom JPanel for menu appearance
            JPanel menuPanel = createCustomMenuPanel(menuItem1, menuItem2, menuItem3, exitMenuItem);

            // Define a JFrame to hold the custom menu panel
            JFrame menuFrame = new JFrame();
            menuFrame.setUndecorated(true); // Remove frame decorations
            menuFrame.getContentPane().add(menuPanel);
            menuFrame.pack();
            final int[] x = {-1};
            final int[] y = {-1};
            // Handle TrayIcon mouse click events

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                        if (SystemTray.isSupported()) {
                            if(x[0] == -1 && y[0] == -1) {
                                x[0] = e.getXOnScreen();
                                y[0] = e.getYOnScreen() - menuFrame.getHeight();
                                menuFrame.setLocation(x[0], y[0]);

                            }
                            if (!menuFrame.getBounds().contains(e.getPoint())) {
                                menuFrame.setVisible(!menuFrame.isVisible()); // Toggle menu frame visibility
                            }
                        }
//                    }

                }
            });
            exitMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("clicked");
                    menuFrame.dispose(); // Close the menu frame

                }
            });


        }
    }


    private JPanel createCustomMenuPanel(MenuItem... menuItems) {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(menuItems.length, 1));
        menuPanel.setBackground(new Color(40, 40, 40));
        Border border = new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, Color.DARK_GRAY),
                new EmptyBorder(10, 10, 10, 10)
        );
        menuPanel.setBorder(new RoundedBorder(border, 20));


        for (MenuItem item : menuItems) {
            JLabel label = createMenuItemLabel(item);
            menuPanel.add(label);
        }


        return menuPanel;
    }
    private JPopupMenu createDropdownMenu() {
        JPopupMenu dropdownMenu = new JPopupMenu();

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(70, 70, 70));
        Border border = new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, Color.DARK_GRAY),
                new EmptyBorder(5, 5, 5, 5)
        );
        menuPanel.setBorder(new RoundedBorder(border, 15)); // Rounded border for curved edges

        JRadioButtonMenuItem onMenuItem = new JRadioButtonMenuItem("On");
        JRadioButtonMenuItem offMenuItem = new JRadioButtonMenuItem("Off");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(onMenuItem);
        buttonGroup.add(offMenuItem);
        menuPanel.add(onMenuItem);
        menuPanel.add(offMenuItem);

        dropdownMenu.add(menuPanel);
        return dropdownMenu;
    }

    private JLabel createMenuItemLabel(MenuItem item) {
        JLabel label = new JLabel(item.getLabel());
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(40, 40, 40));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (item.getLabel().equals("PC Turn Off") || item.getLabel().equals("Auto TimeSheet Fill") || item.getLabel().equals("Always On Top")) {
            Boolean isActive = (!item.getLabel().equals("PC Turn Off")) ? HelloApplication.autoFillTimeSheet : HelloApplication.autoPCTurnOff;
            JPopupMenu dropdownMenu = new JPopupMenu();
            JRadioButtonMenuItem onMenuItem = new JRadioButtonMenuItem("Active");
            JRadioButtonMenuItem offMenuItem = new JRadioButtonMenuItem("Inactive");
            ButtonGroup buttonGroup = new ButtonGroup();


            buttonGroup.add(onMenuItem);
            buttonGroup.add(offMenuItem);
            if(item.getLabel().equals("Always On Top")){
                if(HelloApplication.alwaysOnTop){
                    onMenuItem.setSelected(true);
                }else {
                    offMenuItem.setSelected(true);
                }
            }else {
                // Set the default value based on the isActive boolean variable
                if (isActive) {
                    onMenuItem.setSelected(true); // Set "Active" as default
                } else {
                    offMenuItem.setSelected(true); // Set "Inactive" as default
                }
            }
            // Create a common action listener for both radio button menu items
            ActionListener radioActionListener = e -> {
                boolean setToActive = e.getSource() == onMenuItem;

                // Update the corresponding boolean variable based on the menu item and selection
                if (item.getLabel().equals("Auto TimeSheet Fill")) {
                    HelloApplication.autoFillTimeSheet = setToActive;
                    HelloApplication.updateCacheFile("autoFillTimeSheet", setToActive);
                } else if(item.getLabel().equals("PC Turn Off")) {
                    HelloApplication.autoPCTurnOff = setToActive;
                    HelloApplication.isMinimize = true;
                    HelloApplication.updateCacheFile("autoPCTurnOff", setToActive);

                }else {
                    HelloApplication.alwaysOnTop = !HelloApplication.alwaysOnTop;
                    HelloApplication.isMinimize = true;
                    HelloApplication.updateCacheFile("alwaysOnTop", setToActive);
                }
            };

            // Add the common action listener to both radio button menu items
            onMenuItem.addActionListener(radioActionListener);
            offMenuItem.addActionListener(radioActionListener);

            dropdownMenu.add(onMenuItem);
            dropdownMenu.add(offMenuItem);


            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        // Show the dropdown menu below the label
                        dropdownMenu.show(label, 0, label.getHeight());
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if(item.getLabel().equals("PC Turn Off")){

                        }
                        else {
                            // Handle right-click on the label
//                        JPopupMenu fillTimeSheetMenu = createFillTimeSheetMenu();
//                        fillTimeSheetMenu.show(label, 25, -fillTimeSheetMenu.getHeight());
                            JScrollPane editorPane = createFillTimeSheetEditor();
                            JOptionPane.showMessageDialog(null, editorPane, "Fill Time Sheet", JOptionPane.PLAIN_MESSAGE);
                        }

                    }
                }


                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBackground(new Color(70, 70, 70));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBackground(null);
                }
            });
        }
        else{
            label.addMouseListener(new MouseAdapter() {
                @Override
//                public void mouseClicked(MouseEvent e) {
//                    // Handle menu item click here
//                    System.out.println("click");
//                    if(item.getLabel().equals("Exit")){
//                        System.exit(0);
//                    }
//                    if(item.getLabel().equals("Always On Top")){
//                        System.out.println("clicked");
//                        item.setLabel("Minimized");
////                        HelloApplication.isMinimize = true;
////                        HelloApplication.alwaysOnTop = false;
//                    }
//                    if(item.getLabel().equals("Minimized")){
//                        item.setLabel("Always On Top");
////                        HelloApplication.alwaysOnTop = true;
//                    }
//
//                }
                public void mouseClicked(MouseEvent e) {
//
                        if (item.getLabel().equals("Exit")) {
                            System.exit(0);
                        }

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBackground(new Color(70, 70, 70));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBackground(null);
                }
            });
        }

        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }
//    private JPopupMenu createFillTimeSheetMenu() {
//        JPopupMenu fillTimeSheetMenu = new JPopupMenu();
//        // Add items to the "Fill Time Sheet" menu as needed
//        JMenuItem item1 = new JMenuItem("Fill Time Sheet");
////        JMenuItem item2 = new JMenuItem("Item 2");
//        // Add action listeners for these items if necessary
//        fillTimeSheetMenu.add(item1);
////        fillTimeSheetMenu.add(item2);
//        return fillTimeSheetMenu;
//    }
private JScrollPane createFillTimeSheetEditor() {
    JTextPane editor = new JTextPane();
    editor.setEditable(true);
    JScrollPane scrollPane = new JScrollPane(editor);

    // Add a "Save" button to save the content to a file
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = editor.getText();
            saveTextToFile(text);
        }
    });

    // Add the "Save" button to the editor
    JPanel editorPanel = new JPanel();
    editorPanel.setLayout(new BorderLayout());
    editorPanel.add(scrollPane, BorderLayout.CENTER);
    editorPanel.add(saveButton, BorderLayout.SOUTH);

    return new JScrollPane(editorPanel);
}

    // Add this method to save the text to a file
    private void saveTextToFile(String text) {
        try {
            File desktopDir = new File(System.getProperty("user.home"), "Desktop");
            File file = new File(desktopDir, "erpTimeSheetInput.txt");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();

            JOptionPane.showMessageDialog(null, "Text saved to 'erpTimeSheetInput.txt' on the desktop.");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving text to file.");
        }
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Border delegate;

        RoundedBorder(Border delegate, int radius) {
            this.delegate = delegate;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return delegate.getBorderInsets(c);
        }

        @Override
        public boolean isBorderOpaque() {
            return delegate.isBorderOpaque();
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            delegate.paintBorder(c, g, x, y, width, height);
            if (radius > 0) {
                g.setColor(Color.DARK_GRAY);
                g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
        }
    }
}
