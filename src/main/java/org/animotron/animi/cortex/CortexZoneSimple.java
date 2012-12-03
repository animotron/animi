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

import static org.jocl.CL.*;

import org.animotron.animi.InitParam;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.Utils;
import org.animotron.animi.acts.Act;
import org.animotron.animi.acts.ActWithMax;
import org.animotron.animi.acts.UpDownCNActivation;
import org.animotron.animi.acts.Zero;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;
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
	public int width = 100;
	@InitParam(name="height")
	public int height = 100;
	
	@InitParam(name="speed")
	public double speed = Integer.MAX_VALUE;
	
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
     * The OpenCL memory object which store the activity for each neuron.
     */
    public cl_mem cl_cols;
    public float cols[];
    
    public float beforeInhibitoryCols[];
    
    public cl_mem cl_colsNy;
    public float colsNy[];

	@RuntimeParam(name = "first_ny")
	public float first_ny = 1f;
	@RuntimeParam(name = "ny")
	public float ny = 0.15f;

	public BufferedImage image;

    /**
     * Initializes the OpenCL memory object and the BufferedImage which will later receive the pixels
     */
    public void init() {
    	
    	cols = new float[width * height];
    	Arrays.fill(cols, 0);
    	
    	beforeInhibitoryCols = new float[width * height];
    	Arrays.fill(beforeInhibitoryCols, 0);
    	
        cl_cols = clCreateBuffer(
    		mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
    		cols.length * Sizeof.cl_float, Pointer.to(cols), null
		);
        
        colsNy = new float[width * height];
    	Arrays.fill(colsNy, first_ny);
    	
        cl_colsNy = clCreateBuffer(
    		mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
    		colsNy.length * Sizeof.cl_float, Pointer.to(colsNy), null
		);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void release() {
        // Release all existing memory objects
        if (cl_cols != null) {
            clReleaseMemObject(cl_cols);
            cl_cols = null;
        }
    }
    
    public void refreshImage() {
    	DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();
      
    	for (int i = 0; i < cols.length; i++) {
    		final float value = cols[i];
      	
    		if (Float.isNaN(value))
    			data[i] = Color.RED.getRGB();
    		else {
    			int c = (int)(value * 255);
    			if (this instanceof CortexZoneComplex) {
    				if (value != 1 && value > 0)
    					System.out.println(value);
    			}
    			if (c > 255) 
    				c = 255;
					
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

    Random rnd = new Random();

    public boolean saccade = false;
    
    private int next() {
		return rnd.nextInt(40) - 20;
//		int r = 20 + rnd.nextInt(20);
//		
//		if (rnd.nextBoolean())
//			return -r;
//		
//		return r;
    }

	@Override
	public void process() {
//    	if (saccade) {
//    		int vX = next();
//    		int vY = next();
//    		int steps = 10;//rnd.nextInt(20);
//    		
//    		int dx, dy = 0;
//    		for (int step = 1; step <= steps; step++) {
//    			dx = vX * step;
//    			dy = vY * step;
//    			for (int x = 0; x < width; x++) {
//    	    		if (x+dx < 0 || x+dx >= width)
//    	    			continue;
//    	    		
//        			for (int y = 0; y < height; y++) {
//        	    		if (y+dy < 0 || y+dy >= height)
//        	    			continue;
//        	    		
//        	    		col[x][y].activity[0] += col[x + dx][y + dy].activity[0];
//        	    		if (col[x][y].activity[0] > 1)
//        	    			col[x][y].activity[0] = 1;
//        			}
//    			}
//    		}
//    	}
	}

	CortexZoneSimple[] nextLayers = null;
	
	@Override
	public void set(int x, int y, float b) {
//		final NeuronComplex cn = col[x][y];
//		if (b != 0)
//			System.out.println();
//		cn.activity[0] = b;
////		cn.backProjection[0] = b;
//		cn.posActivity[0] = b;
		
		cols[(y*width)+x] = b;
	}

	@Override
	public void shift(int x, int y, float b) {
    	//XXX: optimize

//		final NeuronComplex cn = col[x][y];
//		for (int i = cn.activity.length - 1; i > 0 ; i--) {
//			cn.activity[i] = cn.activity[i-1];
////			cn.backProjection[i] = cn.backProjection[i-1];
//			cn.posActivity[i] = cn.posActivity[i-1];
//		}
//		
		set(x, y, b);
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
	
    public void cycle (int x1, int y1, int x2, int y2, Act<CortexZoneSimple> task) {
    	try {
	        for (int x = x1; x < x2; x++) {
	            for (int y = y1; y < y2; y++) {
	                task.process(this, x, y);
	            }
	        }
    	} catch (Throwable e) {
    		e.printStackTrace();
		}
    }

    public double cycle (int x1, int y1, int x2, int y2, ActWithMax<CortexZoneSimple> task, double max) {
    	try {
	        for (int x = x1; x < x2; x++) {
	            for (int y = y1; y < y2; y++) {
	            	max = task.process(this, x, y, max);
	            }
	        }
		} catch (Throwable e) {
		}
        return max;
    }
    
	public boolean learning = false;
	public boolean isLearning() {
		return learning;
	}
	
    public boolean active = false;

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
		write(out, "learning", learning);
		write(out, "speed", speed);
		out.write("/>");
	}
}