package com.cmp2004termproject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
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

public class Client extends JFrame {
    
    private String name; // Name of the client
    
    private int specialSendCode; // 13 to send message, 26 to ask question to server, 99 to send name to server, 5 to send leave message, 45 to send response message after receiving terminate message
    
    private int specialReceiveCode; // Special receive code to receive the sended special send code
    
    private Socket socket; // Socket to communicate between two process
    
    private ObjectOutputStream oos;
    
    private ObjectInputStream ois;
    
    protected static ArrayList<Points> rectangleList = new ArrayList<Points>(); // List to store rectangle coordinates
    
    protected static ArrayList<Points> ovalList = new ArrayList<Points>(); // List to store oval coordinates
    
    protected static ArrayList<Points> lineList = new ArrayList<Points>(); // List to store line coordinates
    
    protected static Color color;
    
    private JMenuItem raiseYourHand;
    
    private JMenuItem leaveClass;
    
    private JTextArea chatTextArea; // The area where chat messages are displayed
    
    private JTextField chatTextField; // The area where chat messages are written
    
    private JButton chatSendButton; // Button to send chat messages
    
    private JLabel shapeLabel; // Label to display the drawn shape number
    
    private JLabel timeLabel; // Label to display the time counter
    
    private Timer timer; // Timer to count time
    
    private int currentMinute; // current minute of time counter
    
    private int currentSecond; // current second of time counter
    
    Client(){
        
        super("Client");
        
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
        
        try {
            connection();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void menu(){ // This method creates menu
    
        JMenuBar menuBar = new JMenuBar();
        
        setJMenuBar(menuBar);
        
        JMenu lesson = new JMenu("Lesson");
        
        menuBar.add(lesson);
        
        raiseYourHand = new JMenuItem("Raise Your Hand");
        
        raiseYourHand.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                
                try {
                    raiseHand();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        lesson.add(raiseYourHand);
        
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
    }
    
    public void chat(){ // This method creates chat area
    
        chatTextArea = new JTextArea();

        chatTextArea.setEditable(false);
        
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        
        chatScrollPane.setBounds(575, 0, 225, 472);
        
        add(chatScrollPane);

        chatTextField = new JTextField();

        chatTextField.setBounds(575, 470, 225, 34);

        add(chatTextField);

        chatSendButton = new JButton("Send");

        chatSendButton.setBounds(575, 502, 225, 40);
        
        chatSendButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    
                    sendMessage(chatTextField.getText());
                    
                    setTextFieldText("chatTextField", "");
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        add(chatSendButton);
    }
    
    public void paint(){ // This method creates panel to paint
        
        JPanel clientPaintPanel = new ClientPaintPanel();
        
        clientPaintPanel.setBounds(0, 42, 600, 458);
        
        clientPaintPanel.setBackground(Color.white);
        
        add(clientPaintPanel);
    }
    
    
    
    
    
    public void shapeCounterLabel(){ // This method creates label to display the number of drawn shapes
        
        shapeLabel = new JLabel("Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
    
        shapeLabel.setBounds(20, 0, 545, 42);
        
        shapeLabel.setVisible(false);
        
        add(shapeLabel);
    }
    
    public void timeCounter(){ // This method starts the time counter
        
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
    
    
    
    public void connection() throws IOException, ClassNotFoundException { // This method connects the client to the server and starts stream
        
        socket = new Socket(InetAddress.getByName("127.0.0.1"), 9999);
        
        enterName();
        
        oos = new ObjectOutputStream(socket.getOutputStream());
        
        oos.flush();
        
        ois = new ObjectInputStream(socket.getInputStream());
        
        sendName();
        
        currentMinute = 0;
        
        currentSecond = 0;
        
        int stateCode = (int) ois.readObject();
        
        Points points;
        
        if(stateCode == 2 || stateCode == 3 || stateCode == 4){
            
            if(stateCode == 2 || stateCode == 3){
            
                displayMessage("You are late to lesson!");
                
            }
            
            else if(stateCode == 4){
            
                displayMessage("You missed the lesson!");
            }
        
            currentMinute = (int) ois.readObject();
            
            currentSecond = (int) ois.readObject();
            
            int rectangleListSize = (int) ois.readObject();
            
            for(int i = 0; i < rectangleListSize; i++){
            
                points = (Points) ois.readObject();
                
                rectangleList.add(points);
            }
            
            int ovalListSize = (int) ois.readObject();
            
            for(int i = 0; i < ovalListSize; i++){
            
                points = (Points) ois.readObject();
                
                ovalList.add(points);
            }
            
            int lineListSize = (int) ois.readObject();
            
            for(int i = 0; i < lineListSize; i++){
            
                points = (Points) ois.readObject();
                
                lineList.add(points);
            }
            
            color = (Color) ois.readObject();
            
            setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
            
            setLabelText("timeLabel", "Time:  " + currentMinute + "  min  " + currentSecond + "  sec");
            
            setLabelVisible("shapeLabel", true);
            
            setLabelVisible("timeLabel", true);
            
            if(stateCode == 2){
            
                timer.start();
            }
            
            repaint();
            
        }
        
        boolean disconnect = false;
        
        String text;
        
        boolean received;
        
        while(!disconnect){
            
            received = false;
            
            specialReceiveCode = (int) ois.readObject();
            
            while(!received){
                
                switch (specialReceiveCode) {
                    
                    case 13:
                       
                        text = (String) ois.readObject();
                        
                        if(!text.substring(text.indexOf(":") + 1).trim().equals("")){ // if the received message is an empty message, it will not appear in clients chat area
                        
                            displayMessage(text);
                        }
                
                        received = true;
                       
                        break;

                    case 18:

                        points =  (Points) ois.readObject();
                        
                        rectangleList.add(points);

                        repaint();
                        
                        setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
                        
                        received = true;
                        
                        break;
                    
                    case 15:
                        
                        points = (Points) ois.readObject();
                        
                        ovalList.add(points);

                        repaint();
                        
                        setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
                        
                        received = true;
                      
                        break;
                    
                    case 12:

                        points = (Points) ois.readObject();
                        
                        lineList.add(points);

                        repaint();
                       
                        setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
                        
                        received = true;
                       
                        break;
                    
                    case 31:
                        
                        color = (Color) ois.readObject();

                        repaint();
                        
                        received = true;
                       
                        break;
                    
                    case 30:
                        
                        rectangleList.clear();
                        
                        ovalList.clear();
                       
                        lineList.clear();
                       
                        repaint();
                        
                        setLabelText("shapeLabel", "Drawn Rectangle Number  =  " + rectangleList.size() + "          Drawn Oval Number  =  " + ovalList.size() + "          Drawn Line Number  =  " + lineList.size());
                       
                        received = true;
                       
                        break;
                    
                    case 19:
                        
                        setLabelVisible("timeLabel", true);
                      
                        setLabelVisible("shapeLabel", true);
                    
                        timer.start();
                      
                        received = true;
                      
                        break;
                    
                    case 5:
                        
                        disconnect = true;
                        
                        received = true;
                        
                        break;
                        
                    case 45:

                        responseLeave();
                        
                        disconnect = true;
                        
                        received = true;
                        
                        break;
                        
                    case 51:
                        
                        timer.stop();
                        
                        received = true;
                        
                        break;
                        
                    case 61:
                        
                        timer.start();
                        
                        received = true;
                        
                        break;
                        
                    case 71:
                        
                        timer.stop();
                        
                        received = true;
                        
                        break;
                   
                    default:
                       
                        break;
                }
            }
        }
        
        oos.close();
             
        ois.close();
            
        socket.close();

        System.exit(0);
    }
    
    public void sendCode(final int code) throws IOException{ // This method sends code to specify the type of object before object is sended
    
        oos.writeObject(code);
        
        oos.flush();
    }
    
    public void sendName() throws IOException{ // This method sends client name
        
        specialSendCode = 99;
        
        sendCode(specialSendCode);
    
        oos.writeObject(name);
        
        oos.flush();
    }
    
    public void sendLeave() throws IOException{ // This method sends leave message to connected ClientWorker object to break the loop of its run method
    
        specialSendCode = 5;
        
        sendCode(specialSendCode);
        
    }
    
    public void responseLeave() throws IOException{ // This method sends leave message to connected ClientWorker object to break the loop of its run method
    
        specialSendCode = 45;
        
        sendCode(specialSendCode);
    }
    
    public void sendMessage(final String text) throws IOException{ // This method sends message
    
        specialSendCode = 13;
        
        sendCode(specialSendCode);
        
        oos.writeObject(name + "(client): " + text);
        
        oos.flush();
        
    }
    
    public void raiseHand() throws IOException{ // This method sends "asking question" message
    
        specialSendCode = 26;
        
        sendCode(specialSendCode);
    }
    
    public void displayMessage(final String text){ // This method displays all messages and announcements
    
        SwingUtilities.invokeLater(new Runnable(){
            
            @Override
            public void run(){
                
                chatTextArea.append("\n" + text + "\n");
            }
        });
    }
    
    public void setTextFieldText(final String textFieldName, final String text){ // This method sets text fields text
    
        SwingUtilities.invokeLater(new Runnable(){
            
            @Override
            public void run(){
                
                if(textFieldName.equals("chatTextField")){
                
                    chatTextField.setText(text);
                }
            }
        });
    }
    
    public void setLabelVisible(final String labelName, final boolean bool){ // This method sets labels visible or invisible
    
        SwingUtilities.invokeLater(new Runnable(){
            
            @Override
            public void run(){

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
    
        SwingUtilities.invokeLater(new Runnable(){
            
            @Override
            public void run(){
                
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