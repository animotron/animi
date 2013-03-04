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
import org.animotron.animi.cortex.old.NeuronComplex;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

/**
 * Simple cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneSimple implements Layer {

	String id = UUID.randomUUID().toString();

	String name;
    public final MultiCortex mc;
    
    /** State of complex neurons (outputs cortical columns) **/
//    public NeuronComplex[][] col;
    
	@InitParam(name="width")
	public int width = 30;//160;
	@InitParam(name="height")
	public int height = 30;//120;
	
    public MatrixFloat cols;
    
    public MatrixFloat neighborLearning;
    
    int neighborLearningStage = 0;
    public void history() {
//    	if (neighborLearningStage >= 2) {
//    		neighborLearningStage--;
//    		return;
//    	}
//    	switch (neighborLearningStage) {
//		case 1:
//    		neighborLearningStage--;
//    		neighborLearning.fill(0);
//			
//			break;
//		case 0:
//    		for (int i = 0; i < cols.length; i++) {
//    			if (cols[i] > 0) {
//    				neighborLearningStage = 1;
//    				break;
//    			}
//        	}
//    		if (neighborLearningStage == 1) {
//        		neighborLearning.copy(cols);
//        		neighborLearningStage = 3;
//    		}
//			
//			break;
//		default:
//			break;
//		}
    }
    
    public MatrixFloat rememberCols;

	public BufferedImage image;

//	@InitParam(name="speed")
//	public double speed = Integer.MAX_VALUE;
	
	public int count = 0;

	CortexZoneSimple() {
    	name = null;
    	mc = null;
    }

    public CortexZoneSimple(String name, MultiCortex mc) {
        this.name = name;
        this.mc = mc;
    }
    
    /**
     * Initializes the OpenCL memory object and the BufferedImage which will later receive the pixels
     */
    public void init() {
    	
    	cols = new MatrixFloat(width, height);
    	cols.fill(0f);

    	neighborLearning = new MatrixFloat(width, height);
    	neighborLearning.fill(0f);
    	
        rememberCols = new MatrixFloat(width, height);
    	rememberCols.fill(0f);
    	
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void release() {
        // Release all existing memory objects
    }
    
    public void refreshImage() {
    	DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();
      
    	for (int i = 0; i < cols.data.length; i++) {
    		final float value = cols.data[i];
      	
    		if (Float.isNaN(value))
    			data[i] = Color.RED.getRGB();
    		else {
    			int c = (int)(value * 255);
    			if (c > 255) c = 255;
					
				data[i] = Utils.create_rgb(255, c, c, c);
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

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

	@Override
	public void process() {
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

	public NeuronComplex getCol(int x, int y) {
		return null;//col[x][y];
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
}