package org.animotron.animi;

import java.awt.Color;
import java.awt.image.BufferedImage;


// Simple cortex zone
public class CortexZoneSimple implements Layer {

    String name;
    NeuronComplex[][] col;        // State of complex neurons (outputs cortical columns)
    int width;              //
    int height;             //

    public CortexZoneSimple(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.col = new NeuronComplex[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                NeuronComplex cn = new NeuronComplex();
                cn.active = false;
                this.col[x][y] = cn;
            }
        }
    }

    public BufferedImage getColImage() {
    	int c;
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = col[x][y].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
    }
    
    public String toString() {
    	return name;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

	@Override
	public void process() {
		// TODO Auto-generated method stub
	}

	@Override
	public void set(int x, int y, boolean b) {
		col[x][y].active = b;
	}
}