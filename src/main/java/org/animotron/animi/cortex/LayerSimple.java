/*
 *  Copyright (C) 2012-2013 The Animo Project
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

import org.animotron.animi.*;
import org.animotron.animi.gui.Application;
import org.animotron.matrix.Matrix;
import org.animotron.matrix.MatrixDelay;
import org.animotron.matrix.MatrixFloat;
import org.animotron.matrix.MatrixZeroAvg;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.UUID;

/**
 * Simple layer.
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LayerSimple implements ILayer {

	String id = UUID.randomUUID().toString();
	String name;
    
	public final Application app;
    
	@InitParam(name="width")
	public int width = 30;//160;

	@InitParam(name="height")
	public int height = 30;//120;
    
	@InitParam(name="depth")
	public int depth = 9;
	
	public boolean isZeroAvgAxons = true;

	public MatrixDelay.Attenuation attenuation;
	
    public MatrixDelay axons;
    
    public MatrixFloat neurons;
    public MatrixFloat neighbors;

    public MatrixFloat pure;
    public MatrixFloat learning;
    public MatrixFloat toLearning;

    private BufferedImage image;

	public int count = 0;

	LayerSimple() {
    	name = null;
    	app = null;
    }

    public LayerSimple(String name, Application app, int width, int height, int depth, MatrixDelay.Attenuation attenuation) {
        this.name = name;
        this.app = app;

		this.width = width;
		this.height = height;
		this.depth = depth;

		this.attenuation = attenuation;
    }
    
    /**
     * Initializes the OpenCL memory object and the BufferedImage which will later receive the pixels
     */
    public void init() {
    	
    	neurons = new MatrixFloat(width, height, depth);
    	neurons.fill(0f);
    	
    	pure = new MatrixFloat(width, height);
    	pure.fill(0f);

    	learning = new MatrixFloat(width, height, depth);
    	learning.fill(0f);
    	
    	toLearning = new MatrixFloat(width, height, depth);
    	toLearning.fill(0f);

    	neighbors = new MatrixFloat(width, height, depth);
    	neighbors.fill(0f);

    	if (isZeroAvgAxons) {
    		axons = new MatrixZeroAvg(attenuation, width, height, depth);
    	} else {
    		axons = new MatrixDelay(attenuation, width, height, depth);
    	}
    	axons.fill(0f);

    	image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void release() {
        // Release all existing memory objects
    }
    
    public void refreshImage() {
    	DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();
    	
    	int R = 0, G = 0, B = 0;
//    	for (int i = 0; i < cols.length(); i++) {
    	for (int i = 0; i < data.length; i++) {
    		
    		R = 0; G = 0; B = 0;
    		final float value = axons.getByIndex(i);
      	
    		if (Float.isNaN(value))
    			data[i] = Color.WHITE.getRGB();
    		else {
    			if (value >= 0) {
        			R = (int)(value * 255);
        			if (R > 255) R = 255;
    			} else {
	    			B = (int)(-value * 255 * 1000000);
	    			if (B > 255) B = 255;
    			}
					
				data[i] = Utils.rgb(255, R, G, B);
    		}
    	}
    }
    
    public BufferedImage getImage() {
    	refreshImage();
        return image;
    }

	@Override
	public Object whatAt(Point point) {
		return null;//col[point.x][point.y];
	}

    public String getImageName() {
    	return toString();
    }

    public String toString() {
    	return name;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

    @Override
	public void process() {
		axons.step(neurons);
		neurons.step();
	}

	@Override
	public void focusGained(Point point) {
	}

	@Override
	public void focusLost(Point point) {
	}

	@Override
	public void closed(Point point) {
	}

	public boolean isLearning = true;
	public boolean isLearning() {
		return isLearning;
	}
	
    public boolean active = true;
	public boolean isActive() {
		return active;
	}
	
	protected void write(Writer out, String name, Object value) throws IOException {
		out.write(" ");
		out.write(name);
		out.write("='");
		out.write(String.valueOf(value));
		out.write("'");
	}

	public void save(Writer out) throws IOException {
		out.write("<zone type='simple'");
		write(out, "name", name);
		write(out, "id", id);
		write(out, "width", width);
		write(out, "height", height);
		write(out, "active", active);
		write(out, "learning", isLearning);
//		write(out, "speed", speed);
		out.write("/>");
	}

	
	private void debug(String comment, Matrix<Float> neurons) {
		System.out.println(comment);
		
		DecimalFormat df = new DecimalFormat("0.00000");
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++) {
					System.out.print(df.format(neurons.get(x, y, z)));
					System.out.print(" ");
				}
				if (depth > 1)
					System.out.print("| ");
			}
			System.out.println();
		}
	}

	public void debugNeurons(String comment) {
		debug(comment, neurons);
	}

	public void debugAxons(String comment) {
		debug(comment, axons);
	}

	@Override
	public Matrix<Float> axons() {
		return axons;
	}
}