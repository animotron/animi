/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.animi.cortex;

import org.animotron.animi.Utils;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Simple cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneSimple implements Layer {

    String name;
    public NeuronComplex[][] col;        // State of complex neurons (outputs cortical columns)
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

    public BufferedImage getImage() {
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
    
    public String getImageName() {
    	return toString();
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