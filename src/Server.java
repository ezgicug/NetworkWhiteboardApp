package com.cmp2004termproject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Server extends JFrame {
    
    protected static int state; // 1 for before lesson started, 2 for continuing lesson, 3 for break-time, 4 for after lesson finished
    
    private String name; // Name of the server
    
    protected static int specialSendCode; // 13 to send message and announcement, 18 to send Points object where rectangle coordinates are saved, 15 to send Points object where oval coordinates are saved, 12 to send Points object where line coordinates are saved, 31 to send color object, 30 to send clear message(to clean the client's paint panel), 19 to send start message(to inform clients that the lesson has started), 5 to send response message after receiving leave message, 45 to send leave message, 51 to send break-time message, 61 to send continue message, 71 to send finish message
    
    protected static int specialReceiveCode; // Special receive code to receive the sended special send code
           
    private Timer timer; // Timer to count time
    
    protected static int currentMinute; // current minute of time counter
    
    protected static int currentSecond; // current second of time counter
    
    protected static Color selectedColor; // To hold the selected color object

    private boolean firstClick; // Boolean to check if the mouse clicked to paint panel for the first time

    private String draw; // determines which shape will be drawn
    
    private String lastDrawn;

    protected static ArrayList<Points> rectangleList = new ArrayList<Points>(); // List to store rectangle coordinates

    protected static ArrayList<Points> ovalList = new ArrayList<Points>(); // List to store oval coordinates

    protected static ArrayList<Points> lineList = new ArrayList<Points>(); // List to store line coordinates
    
    private Points points; // Points object to save the coordinates of the shape

    protected static JTextArea chatTextArea; // The area where chat messages are displayed

    private JTextField chatTextField; // The area where chat messages are written

    private JButton chatSendButton; // Button to send chat messages

    private JMenu shape;

    private JMenu color;
    
    private JMenu clear;
    
    private JMenuItem startLesson;
    
    private JMenuItem breakTime;
    
    private JMenuItem continueLesson;
    
    private JMenuItem finishLesson;
    
    private JMenuItem leaveClass;
    
    private JMenuItem rectangle; // Menu button to draw rectangle

    private JMenuItem oval; // Menu button to draw oval

    private JMenuItem line; // Menu button to draw line

    private JMenuItem setColor; // Menu button to set color
    
    private JMenuItem clearAll; // Menu button to erase all drawn shapes

    private JMenuItem attendanceList; // Menu Button to display attendance list
    
    private JLabel timeLabel; // Label to display the time counter
    
    private JLabel shapeLabel; // Label to display the drawn shape number
    
    protected static ArrayList<String> attendants = new ArrayList<String>(); // List of the names of clients in the lesson
    
    protected static HashMap<Integer, ClientWorker> clientWorkerList = new HashMap<Integer, ClientWorker>(); // Thread list to take care of the clients on streaming

    public Server() {
        
        super("Server");
        
        setSize(800, 600);
        
        setResizable(false); // size of running application cannot be changed
        
        setLayout(null);
        
        menu();
        
        chat();
        
        paint();

        shapeCounterLabel();
        
        timeCounter();
        
        timeCounterLabel();
        
        setVisible(true);
        
        enterName();
        
        try {
            connection();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
    
    public void menu() { // This method creates menu

        JMenuBar menuBar = new JMenuBar();
        
        setJMenuBar(menuBar);
        
        JMenu lesson = new JMenu("Lesson");
        
        menuBar.add(lesson);

        shape = new JMenu("Shape");
        
        shape.setEnabled(false);

        menuBar.add(shape);

        color = new JMenu("Color");
        
        color.setEnabled(false);
        
        menuBar.add(color);
        
        clear = new JMenu("Clear");
        
        clear.setEnabled(false);
        
        menuBar.add(clear);

        JMenu attendance = new JMenu("Attendance");

        menuBar.add(attendance);
        
        startLesson = new JMenuItem("Start Lesson");
        
        startLesson.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                timer.start();
                
                state = 2;
                
                setLabelVisible("timeLabel", true);
                
                setLabelVisible("shapeLabel", true);
                
                setButtonEnabled("startLesson", false);
                
                setButtonEnabled("shape", true);
                
                setButtonEnabled("color", true);
                
                setButtonEnabled("clear", true);
                
                setButtonEnabled("breakTime", true);
                
                setButtonEnabled("finishLesson", true);

                try {
                    sendStart();
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        lesson.add(startLesson);
        
        breakTime = new JMenuItem("Break-Time");
        
        breakTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                timer.stop();
                
                state = 3;
                
                setButtonEnabled("shape", false);
                
                setButtonEnabled("color", false);
                
                setButtonEnabled("clear", false);
                
                setButtonEnabled("breakTime", false);
                
                setButtonEnabled("continueLesson", true);
                
                firstClick = true;
                
                try {
                    sendBreakTime();
                    
                    announcement("***Break-Time***");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                lastDrawn = draw;
                
                draw = "noname";
                
                
                
            }
        });
        
        breakTime.setEnabled(false);
        
        lesson.add(breakTime);
        
        continueLesson = new JMenuItem("Continue To Lesson");
        
        continueLesson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                timer.start();
                
                state = 2;
                
                try {
                    sendContinue();
                    
                    announcement("***Break-Time is over***");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                setButtonEnabled("continueLesson", false);
                
                setButtonEnabled("breakTime", true);
                
                setButtonEnabled("shape", true);
                
                setButtonEnabled("color", true);
                
                setButtonEnabled("clear", true);
                
                draw = lastDrawn;
                
            }
        });
        
        continueLesson.setEnabled(false);
        
        lesson.add(continueLesson);
        
        finishLesson = new JMenuItem("Finish Lesson");
        
        finishLesson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                timer.stop();
                
                state = 4;
                
                try {
                    sendFinish();
                    
                    announcement("***Lesson has ended***");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                draw = "noname";
                
                setButtonEnabled("shape", false);
                
                setButtonEnabled("color", false);
                
                setButtonEnabled("clear", false);
                
                setButtonEnabled("finishLesson", false);
                
                if(breakTime.isEnabled()){
                
                    setButtonEnabled("breakTime", false);
                }
                
                else {
                
                    setButtonEnabled("continueLesson", false);
                }
            }
        });
        
        finishLesson.setEnabled(false);
        
        lesson.add(finishLesson);
        
        leaveClass = new JMenuItem("Leave Class");
        
        leaveClass.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    sendLeave();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        lesson.add(leaveClass);
        
        rectangle = new JMenuItem("Draw Rectangle");

        rectangle.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
 
                draw = "rectangle";
            
                firstClick = true;
            }
        });

        shape.add(rectangle);

        oval = new JMenuItem("Draw Oval");

        oval.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                draw = "oval";

                firstClick = true;
            }
        });

        shape.add(oval);

        line = new JMenuItem("Draw Line");

        line.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                draw = "line";

                firstClick = true;
            }
        });

        shape.add(line);

        setColor = new JMenuItem("Set Color");

        setColor.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                firstClick = true;
                
                selectedColor = JColorChooser.showDialog(setColor, "Set Color", Color.black); // Displays color choosing chart
                
                repaint();
                
                try {
                    sendColor(selectedColor);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    
                }
            }
        });

        color.add(setColor);
        
        selectedColor = Color.black;

        clearAll = new JMenuItem("Clear All");
        
        clearAll.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
            
                rectangleList.clear();
            
                ovalList.clear();
            
                lineList.clear();
            
                firstClick = true;
   
                setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
            
                repaint();
                
                try {
                    sendClear();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        clear.add(clearAll);

        attendanceList = new JMenuItem("See Attendance List");

        attendanceList.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    displayAttendanceList();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                firstClick = true;
            }
        });

        attendance.add(attendanceList);
        
    }

    public void chat() { // This method creates chat area

        chatTextArea = new JTextArea();

        chatTextArea.setEditable(false);
        
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        
        chatScrollPane.setBounds(575, 0, 225, 472);
        
        add(chatScrollPane);

        chatTextField = new JTextField();

        chatTextField.setBounds(575, 470, 225, 34);

        add(chatTextField);

        chatSendButton = new JButton("Send");
        
        chatSendButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                
                try {
                        
                    sendMessage(chatTextField.getText());
                        
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

        chatSendButton.setBounds(575, 502, 225, 40);

        add(chatSendButton);
    }
    
    public void paint(){ // This method creates panel to paint
        
        JPanel serverPaintPanel = new ServerPaintPanel();
        
        serverPaintPanel.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseClicked(MouseEvent e) {

                if (draw.equals("rectangle")) {
            
                    if (firstClick) { // In the first click of mouse, points object is created, x coordinate assigned to x1(x1 is data member of Points class) and y coordinate assigned to y1(y1 is data member of Points class)
               
                        points = new Points();
               
                        points.x1 = e.getX();
               
                        points.y1 = e.getY();
               
                        firstClick = false;
          
                    }
                    
                    else { // In the second click of mouse, x coordinate assigned to x2(x2 is data member of Points class) and y coordinate assigned to y2(y2 is data member of Points class)
                
                        points.x2 = e.getX();
                
                        points.y2 = e.getY();
                        
                        if(!(points.x1 == points.x2 || points.y1 == points.y2)){
                        
                            
                            rectangleList.add(points);
                        
                            setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size()); // shape counter label updated
                
                            firstClick = true;
                                                
                            repaint();
                        
                            try {
                                sendRectangle(points);
                            } catch (IOException ex) {
                            ex.printStackTrace();
                            }
                        }
                    }
                }
        
                if (draw.equals("oval")) {
            
                    if (firstClick) {
               
                        points = new Points();
               
                        points.x1 = e.getX();
               
                        points.y1 = e.getY();

                        firstClick = false;
        
                    }
                    
                    else {
                        
                        points.x2 = e.getX();
               
                        points.y2 = e.getY();
                        
                        if(!(points.x1 == points.x2 && points.y1 == points.y2)){
                        
                            ovalList.add(points);
                
                            firstClick = true;
                                
                            setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
               
                            repaint();
                        
                            try {
                                sendOval(points);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            
                        }
                
                        
                    }
                }
       
                if (draw.equals("line")) {
           
                    if (firstClick) {
              
                        points = new Points();
              
                        points.x1 = e.getX();
                
                        points.y1 = e.getY();
               
                        firstClick = false;
                    }
                    
                    else {
                        
                        points.x2 = e.getX();
               
                        points.y2 = e.getY();
                        
                        if(!(points.x1 == points.x2 && points.y1 == points.y2)){
                            
                            lineList.add(points);
               
                            firstClick = true;
                               
                            setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
              
                            repaint();
                        
                            try {
                            sendLine(points);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            
                        }
               
                        

                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}
            
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        serverPaintPanel.setBackground(Color.white);

        serverPaintPanel.setBounds(0, 42, 575, 458);
        
        add(serverPaintPanel);
        
        draw = "noname";
    }

    
    
    public void shapeCounterLabel(){ // This method creates label to display the number of drawn shapes
        
        shapeLabel = new JLabel("Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
        
        shapeLabel.setBounds(20, 0, 545, 42);
        
        shapeLabel.setVisible(false);
        
        add(shapeLabel);
    }
    
    public void timeCounter(){ // This method starts the time counter
        
        currentMinute = 0;
        
        currentSecond = 0;
        
        timer = new Timer(1000, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                currentSecond++;               
                    
                if(currentSecond == 60){
                                            
                    currentSecond = 0;
                                         
                    currentMinute++;

                    setLabelText("timeLabel", "Time:  " + currentMinute + "  min  " + currentSecond + "  sec");
                }
                
                else{

                    setLabelText("timeLabel", "Time:  " + currentMinute + "  min  " + currentSecond + "  sec");
                }
                
            }
        });
        
        timer.setInitialDelay(0);
    }
    
    public void timeCounterLabel(){ // This method creates label to display the time counter
      
        timeLabel = new JLabel("Time:  0  min  0  sec");
        
        timeLabel.setBounds(425, 500, 140, 42);
        
        timeLabel.setVisible(false);
        
        add(timeLabel);
    }
    
    public void enterName(){ // This method displays the input panel to user to enter user's name
    
        JPanel namePanel = new JPanel();
        
        JTextField nameField = new JTextField(10);
        
        namePanel.add(new JLabel("Name: "));
        
        namePanel.add(nameField);
        
        JOptionPane.showConfirmDialog(this, namePanel, "Please enter your name", JOptionPane.OK_CANCEL_OPTION);
        
        if(nameField.getText().isEmpty()){
        
            name = "noname";
        }
        
        else{
        
            name = nameField.getText();
        }
        
        displayMessage("Welcome " + name + "!");
    }
    
    
    
    

    public void connection() throws IOException{ // This method accepts the clients connection into an infinite loop
        
        state = 1;
        
        int queueCounter = 0;
        
        ServerSocket serverSocket = new ServerSocket(9999, 10, InetAddress.getByName("127.0.0.1")); // serverSocket object to accept client connections
        
        ClientWorker clientWorker; // clientWorker object to be used as a thread
        
        Thread thread;
        
        while(true){

            clientWorker = new ClientWorker(serverSocket.accept(), queueCounter);
            
            clientWorkerList.put(queueCounter, clientWorker); // orderNumber(key) and clientWorker(value) are added to the clientWorkerList
            
            thread = new Thread(clientWorker);
            
            thread.start(); // Run method of clientWorker object is invoked
            
            queueCounter++;
        }
    }
    
    public void sendCode(final int code) throws IOException{ // This method sends code to specify the type of object before object is sended
    
        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(code);
            
            entry.getValue().getOutputStream().flush();
        }
    }
    
    public void sendStart() throws IOException{ // This method sends information that the lesson has started
        
        specialSendCode = 19;
        
        sendCode(specialSendCode);
        
        announcement("***Lesson has started***");
    }
    
    public void sendBreakTime() throws IOException{ 
    
        specialSendCode = 51;
        
        sendCode(specialSendCode);
    }
    
    public void sendContinue() throws IOException{ // This method sends continue message to clients to restart their timer
    
        specialSendCode = 61;
        
        sendCode(specialSendCode);
    }
    
    public void sendFinish() throws IOException{ // This method sends finish message to clients to stop their timer
    
        specialSendCode = 71;
        
        sendCode(specialSendCode);
        
    }
    
    public void sendLeave() throws IOException{ // This method sends leave message to clients to terminate their own running program
        
        specialSendCode = 45;
        
        sendCode(specialSendCode);
        
        System.exit(0);
    }
    
    
    
    public void sendRectangle(final Points points) throws IOException{ // This method sends points objects of rectangleList
    
        specialSendCode = 18;
        
        sendCode(specialSendCode);
        
        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(points);
            
            entry.getValue().getOutputStream().flush();
        }
    }
    
    public void sendOval(final Points points) throws IOException{ // This method sends points objects of ovalList
    
        specialSendCode = 15;
        
        sendCode(specialSendCode);
        
        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(points);
            
            entry.getValue().getOutputStream().flush();
        }
    }
    
    public void sendLine(final Points points) throws IOException{ // This method sends points objects of lineList
    
        specialSendCode = 12;
        
        sendCode(specialSendCode);
        
        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(points);
            
            entry.getValue().getOutputStream().flush();
        }
    }
    
    public void sendColor(final Color color) throws IOException{ // This method sends color object
    
        specialSendCode = 31;
        
        sendCode(specialSendCode);
        
        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(color);
            
            entry.getValue().getOutputStream().flush();
        }
    }
    
    public void sendClear() throws IOException{ // This method sends clear message(to clean the client's paint panel)
    
        specialSendCode = 30;
        
        sendCode(specialSendCode);
    }
    
    public void sendMessage(final String text) throws IOException{ // This method sends message
        
        specialSendCode = 13;
        
        sendCode(specialSendCode);

        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
            entry.getValue().getOutputStream().writeObject(name + "(server): " + text);
        
            entry.getValue().getOutputStream().flush();
        }
        
        displayMessage(name + "(server): " + text);
        
        setTextFieldText("chatTextField", "");
    }
    
    public void announcement(final String text) throws IOException{ // This method sends announcements
    
        specialSendCode = 13;
        
        sendCode(specialSendCode);

        for(HashMap.Entry<Integer, ClientWorker> entry : clientWorkerList.entrySet()){
        
        
            entry.getValue().getOutputStream().writeObject(text);
        
            entry.getValue().getOutputStream().flush();
        }
        
        displayMessage(text);
    }
    
    public void displayMessage(final String text){ // This method displays all messages and announcements
    
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                if(!text.substring(text.indexOf(":") + 1).trim().equals("")){

                    chatTextArea.append("\n" + text + "\n");
                }
            }
        });
    }
    
    public void displayAttendanceList() throws IOException{ // This method displays names of attendants
    
        int attendantNumber = 0;
        
        FileWriter fileWriter = new FileWriter("AttendanceList.txt"); // fileWriter object to write attendant names to the specified file
        
        for(String attendantName : attendants){
        
            fileWriter.write(attendantName + "\n");
        }
        
        fileWriter.close();
        
        Scanner scanner = new Scanner(new FileReader("AttendanceList.txt")); // scanner object to read attendant names from the specified file
        
        JTextArea attendanceTextArea = new JTextArea(10, 10);
        
        attendanceTextArea.setEditable(false);
        
        JScrollPane attendanceScrollPane = new JScrollPane(attendanceTextArea);
        
        JPanel attendancePanel = new JPanel();
        
        attendancePanel.add(attendanceScrollPane);
        
        while(scanner.hasNextLine()){
            
            attendantNumber++;
            
            attendanceTextArea.append(attendantNumber + "-) " + scanner.nextLine() + "\n");
        }
        
        JOptionPane.showMessageDialog(this, attendancePanel, "Attendance List", JOptionPane.INFORMATION_MESSAGE); // Displays attendant names
        
        scanner.close();
    }
    
    
    
    public void setTextFieldText(final String textFieldName, final String text){ // This method sets text fields text
    
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                if(textFieldName.equals("chatTextField")){
                
                    chatTextField.setText(text);
                }
            }
        });
    }
 
    public void setButtonEnabled(final String buttonName, final boolean bool){ // This method sets buttons enabled or disabled
    
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                if(buttonName.equals("shape")) {
                
                    shape.setEnabled(bool);
                }
                
                else if(buttonName.equals("color")){
                
                    color.setEnabled(bool);
                }
                
                else if(buttonName.equals("clear")){
                
                    clear.setEnabled(bool);
                }
                
                else if(buttonName.equals("startLesson")){
                
                    startLesson.setEnabled(bool);
                }
                
                else if(buttonName.equals("breakTime")){
                
                    breakTime.setEnabled(bool);
                }
                
                else if(buttonName.equals("continueLesson")){
                
                    continueLesson.setEnabled(bool);
                }
                
                else if(buttonName.equals("finishLesson")){
                
                    finishLesson.setEnabled(bool);
                }
            }
        });
    }
    
    public void setLabelVisible(final String labelName, final boolean bool){ // This method sets labels visible or invisible
    
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                if(labelName.equals("timeLabel")){
                
                    timeLabel.setVisible(bool);
                }
                
                else if(labelName.equals("shapeLabel")){
                    
                    shapeLabel.setVisible(bool);
                }
            }
        });
    }
    
    public void setLabelText(final String labelName, final String text){ // This method sets labels text
    
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                if(labelName.equals("shapeLabel")){
                
                    shapeLabel.setText(text);
                }
                
                else if(labelName.equals("timeLabel")){
                
                    timeLabel.setText(text);
                }
            }
        });
    }
}