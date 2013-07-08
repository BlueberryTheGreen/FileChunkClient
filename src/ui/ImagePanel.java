package com.agiac.filechunk.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * This class is an extension of JPanel in order to load up an image
 * 
 * @author Adam Giacobbe
 */
class ImagePanel extends JPanel {

        BufferedImage image;
        int w, h;
        int mancalaCount;
        boolean isCounter;

        public ImagePanel(String fileName) {
            setImage(fileName);
            isCounter = false;
        }

        public void setImage(String fileName) {
            Color color = new Color(255, 255, 255);
            this.setBackground(color);
            try {
                image = ImageIO.read(getClass().getResource(fileName));
                w = image.getWidth();
                h = image.getHeight();
            } catch (Exception e) {
                System.out.println(e);
                //System.exit(0);
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(w, h);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            super.setSize(w, h);
            g.drawImage(image, 0, 0, this);
        //    System.out.println("painting stuff? " + isCounter);
            if(isCounter)
            {
                Font font = new Font("ff", 0, 40);
                g.setFont(font);
                g.drawString(String.valueOf(mancalaCount),20 , 56);
            }
        }
    }


