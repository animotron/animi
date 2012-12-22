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

import org.animotron.animi.*;
import org.animotron.animi.cortex.old.NeuronComplex;
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
	public int width = 160;
	@InitParam(name="height")
	public int height = 120;
	
    /**
     * The OpenCL memory object which store the activity for each neuron.
     */
    public cl_mem cl_cols;
    public float cols[];
    
    /**
     * The OpenCL memory object which store the remember or not activity at RF of neuron.
     */
    public cl_mem cl_rememberCols;
    public float rememberCols[];

    /**
     * The OpenCL memory object which store the maximum activity of neuron during cycle.
     */
    public cl_mem cl_cycleCols;
    public float cycleCols[];

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
    	
    	cols = new float[width * height];
    	Arrays.fill(cols, 0);
    	
        cl_cols = clCreateBuffer(
    		mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
    		cols.length * Sizeof.cl_float, Pointer.to(cols), null
		);
        
        rememberCols = new float[width * height];
    	Arrays.fill(rememberCols, 0);
    	
    	cl_rememberCols = clCreateBuffer(
    		mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
    		rememberCols.length * Sizeof.cl_float, Pointer.to(rememberCols), null
		);

    	cycleCols = new float[width * height];
    	Arrays.fill(cycleCols, 0);
    	
    	cl_cycleCols = clCreateBuffer(
    		mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
    		cycleCols.length * Sizeof.cl_float, Pointer.to(cycleCols), null
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
//		write(out, "speed", speed);
		out.write("/>");
	}

	public void reset() {
		Arrays.fill(cycleCols, 0);
	}
}