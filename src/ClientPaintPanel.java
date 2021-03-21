package com.cmp2004termproject;

import java.awt.Graphics;
import javax.swing.JPanel;

public class ClientPaintPanel extends JPanel {

    @Override
    public void paint(Graphics g) {
        
        g.setColor(Client.color);
        
        for(Points list : Client.rectangleList){
            
            g.drawRect(Math.min(list.x1, list.x2), Math.min(list.y1, list.y2), Math.abs(list.x1 - list.x2), Math.abs(list.y1 - list.y2));
        }
            
        for(Points list : Client.ovalList){
            
            g.drawOval(Math.min(list.x1, list.x2), Math.min(list.y1, list.y2), Math.abs(list.x1 - list.x2), Math.abs(list.y1 - list.y2));
        }
            
        for(Points list : Client.lineList){
            
            g.drawLine(list.x1, list.y1, list.x2, list.y2);     
        }
    }
}
