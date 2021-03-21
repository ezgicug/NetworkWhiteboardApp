package com.cmp2004termproject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class ClientWorker implements Runnable {

    private Socket socket; // Socket to communicate between two process
    
    private ObjectOutputStream oos;
    
    private ObjectInputStream ois;
    
    private int queueNumber;
    
    private String attendantName; // name of the connected client
    
    public ClientWorker(Socket socket, int queueNumber){
    
        this.queueNumber = queueNumber;
        
        this.socket = socket;
        
        try {
            oos = new ObjectOutputStream(this.socket.getOutputStream());
            
            oos.flush();
            
            ois = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException ex) {
            
            ex.printStackTrace();
        }
    }

    @Override
    public void run(){ // This method starts stream
        
        boolean disconnect = false;
        
        String text;
        
        boolean received;
        
        try {
            sendStateCode();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        while(!disconnect){
            
            received = false;
            
            try {
                
                Server.specialReceiveCode = (int) ois.readObject();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            while(!received){
                            
                switch (Server.specialReceiveCode) {
                    
                    case 13:
                    
                        try {
                            text = (String) ois.readObject();
                        
                            printAndSendReceivedMessage(text);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    
                        received = true;
                        
                        break;

                    case 5:
                        
                        try {
                        
                            responseLeave();
                        
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        
                        displayMessage(attendantName + "(client) has left the class");
                        
                        Server.attendants.remove(attendantName);

                        Server.clientWorkerList.remove(queueNumber);
                        
                        
                        
                        disconnect = true;
                        
                        received = true;
                    
                        break;
                
                    case 99:
                        
                        try {
                            attendantName = (String) ois.readObject();
                        
                            Server.attendants.add(attendantName);
                        
                            displayMessage(attendantName + "(client) has joined to your class");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    
                        received = true;
                        
                        break;
                    
                    case 26:
                        
                        displayMessage(attendantName + "(client) wants to ask you a question");
                        
                        received = true;
                        
                        break;
                        
                    case 45:

                        disconnect = true;
                        
                        received = true;
                        
                        break;
                        
                    default:

                        break;
                }
            }
        }
        
        try {
            oos.close();
            
            ois.close();
        
            socket.close();
        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendStateCode() throws IOException{
        
        if(Server.state == 1){
        
            oos.writeObject(Server.state);
        
            oos.flush();
            
        }
        
        if(Server.state == 2 || Server.state == 3 || Server.state == 4){
        
            oos.writeObject(Server.state);
        
            oos.flush();
            
            oos.writeObject(Server.currentMinute);
            
            oos.flush();
            
            oos.writeObject(Server.currentSecond);
            
            oos.flush();
            
            oos.writeObject(Server.rectangleList.size());
            
            oos.flush();
            
            for(Points list : Server.rectangleList){
            
                oos.writeObject(list);
                
                oos.flush();
            }
            
            oos.writeObject(Server.ovalList.size());
            
            oos.flush();
            
            for(Points list : Server.ovalList){
            
                oos.writeObject(list);
                
                oos.flush();
            }
            
            oos.writeObject(Server.lineList.size());
            
            oos.flush();
            
            for(Points list : Server.lineList){
            
                oos.writeObject(list);
                
                oos.flush();
            }
            
            oos.writeObject(Server.selectedColor);
            
            oos.flush();
        }

        
    }
    
    public void sendCode(final ObjectOutputStream oos, final int code) throws IOException{ // This method sends code to specify the type of object before object is sended
    
        oos.writeObject(code);
        
        oos.flush();
    }
    
    public void printAndSendReceivedMessage(final String text) throws IOException{ // This method sends received message to all connected clients to the server

        if(!text.substring(text.indexOf(":") + 1).trim().equals("")){

            displayMessage(text);
            
            Server.specialSendCode = 13;

            for(HashMap.Entry<Integer, ClientWorker> entry : Server.clientWorkerList.entrySet()){
        
                
                sendCode(entry.getValue().getOutputStream(), Server.specialSendCode);
            
                entry.getValue().getOutputStream().writeObject(text);
            
            
                entry.getValue().getOutputStream().flush();
        
            }         
        }
    }
    
    public void responseLeave() throws IOException{ // This method sends leave message to connected client to terminate its own running program
    
        Server.specialSendCode = 5;
        
        sendCode(oos, Server.specialSendCode);
    }
    
    public void displayMessage(final String text){ // This method displays all messages and announcements
    
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                Server.chatTextArea.append("\n" + text + "\n");
            }
        });
    }
    
    public ObjectOutputStream getOutputStream(){
    
        return oos;
    }
}