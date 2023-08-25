package com.example.demo1;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

class RoundedDialogPanel extends JPanel {
    public RoundedDialogPanel(InputData inputData, JDialog jDialog) {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Create AWT components
        JLabel empCodeLabel = new JLabel("Emp Code:");
        JTextField empCodeField = new JTextField(10);

        JLabel timeLabel = new JLabel("Time Field:");
        JTextField timeFieldInput = new JTextField(10);

        // Set font for labels and text fields
        Font font = new Font("Varela Round", Font.BOLD, 14);
        empCodeLabel.setFont(font);
        timeLabel.setFont(font);
        empCodeField.setFont(font);
        timeFieldInput.setFont(font);
        empCodeField.setText(inputData.getEmpCode());

        // Set foreground color for labels and text fields
        Color textColor = Color.WHITE;
        empCodeLabel.setForeground(textColor);
        timeLabel.setForeground(textColor);
        empCodeField.setForeground(textColor);
        timeFieldInput.setForeground(textColor);
        timeFieldInput.setText(inputData.getTimeField());

        // Set background color for text fields
        Color backgroundColor = new Color(68, 70, 84);
        empCodeField.setBackground(backgroundColor);
        timeFieldInput.setBackground(backgroundColor);

        add(empCodeLabel);
        add(empCodeField);
        add(timeLabel);
        add(timeFieldInput);

        // Add KeyListener to the text fields
        KeyListener enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String time = timeFieldInput.getText();
                    String empCode = empCodeField.getText();
                    System.out.println("Values: inside the form  " + time + " " + empCode);
                    inputData.setEmpCode(empCode);
                    inputData.setTimeField(time);
                    jDialog.dispose();
                }
            }
        };
        empCodeField.addKeyListener(enterKeyListener);
        timeFieldInput.addKeyListener(enterKeyListener);
    }
}

public class EmployeeForm {
    private static JDialog jDialog = new JDialog();

    public static JDialog showForm(final InputData inputData) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI(inputData);
        });
        return jDialog;
    }
    public static void hideForm(){
        jDialog.dispose();
    }

    public static void setFormPosition(double x, double y) {
        SwingUtilities.invokeLater(() -> {
            if (jDialog != null) {
                jDialog.setLocation((int) x, (int) y);
            }
        });
    }

    private static InputData createAndShowGUI(InputData inputData) {

        jDialog.setUndecorated(true);
        jDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        RoundedDialogPanel dialogPanel = new RoundedDialogPanel(inputData,jDialog); // Your custom panel

        int arcWidth = 30;
        int arcHeight = 30;
        Shape roundedRectangle = new RoundRectangle2D.Double(0, 0, dialogPanel.getPreferredSize().getWidth(), dialogPanel.getPreferredSize().getHeight(), arcWidth, arcHeight);
        jDialog.setShape(roundedRectangle); // Set the shape of the dialog

        jDialog.add(dialogPanel);
        jDialog.pack();

        jDialog.setVisible(true);
        return inputData;
    }
}

