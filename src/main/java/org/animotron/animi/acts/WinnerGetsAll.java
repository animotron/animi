/*
 *  Copyright (C) 2012-2013 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
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
package org.animotron.animi.acts;

import static org.jocl.CL.*;

import java.util.Arrays;

import org.animotron.animi.cortex.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

/**
 * Winner gets all
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class WinnerGetsAll extends Task {

	cl_mem _cols;
	
	public WinnerGetsAll(CortexZoneComplex cz) {
		super(cz);
	}

    /**
     * Set up the OpenCL arguments for this task for the given kernel
     * 
     * @param kernel The OpenCL kernel for which the arguments will be set
     */
	@Override
    protected void setupArguments(cl_kernel kernel) {
        clSetKernelArg(kernel,  0, Sizeof.cl_mem, Pointer.to(cz.cl_rememberCols));
        clSetKernelArg(kernel,  1, Sizeof.cl_int, Pointer.to(new int[] {cz.width}));

    	//max is winner & winner gets all
    	int maxPos = -1;
    	float max = 0;
        int cPos = -1;

        final int linksNumber = cz.inhibitory_number_of_links;
    	
    	float[] rememberCols = new float[sz.cols.length];
    	System.arraycopy(sz.cols, 0, rememberCols, 0, sz.cols.length);

    	float[] cols = new float[sz.rememberCols.length];
    	System.arraycopy(sz.rememberCols, 0, cols, 0, sz.rememberCols.length);
    	
    	while (true) {
	    	maxPos = -1;
	    	max = 0;
	    	for (int pos = 0; pos < cols.length; pos++) {
	    		if (cols[pos] > max) {
	    			max = cols[pos];
	    			maxPos = pos;
	    		}
	    	}
	    	
	        if (maxPos == -1) {
	        	break;
	        }
	        
        	int y = (int)(maxPos / cz.width);
        	int x = maxPos - (y * cz.width);
        	
            int XSize = (linksNumber * 2);
            int offset = ((y * cz.width) + x) * XSize;
            
    		for (int l = 0; l < linksNumber; l++) {
    	    	int xi = cz.inhibitoryLinksSenapse[offset + (l * 2)    ];
    	    	int yi = cz.inhibitoryLinksSenapse[offset + (l * 2) + 1];
    	        
    	    	cPos = (yi * cz.width) + xi;
            	cols[cPos] = 0;
            	if (cPos != maxPos)
            		rememberCols[cPos] = 0;
    		}
        	cols[(y * cz.width) + x] = 0;
    	}

        _cols = clCreateBuffer(
    		cz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
    		rememberCols.length * Sizeof.cl_float, Pointer.to(rememberCols), null
		);
        
        clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(_cols));
    }

	@Override
    protected void enqueueReads(cl_command_queue commandQueue, cl_event events[]) {
//    	super.enqueueReads(commandQueue, events);
    	
        // Read the contents of the cl_neighborCols memory object
    	Pointer target = Pointer.to(sz.rememberCols);
    	clEnqueueReadBuffer(
			commandQueue, sz.cl_rememberCols, 
			CL_TRUE, 0, sz.rememberCols.length * Sizeof.cl_float, 
			target, 0, null, events[0]);

    	clWaitForEvents(1, events);
	}

	@Override
	protected void release() {
		clReleaseMemObject(_cols);
	}
//	
//    protected void processColors(float array[]) {
//    	DataBufferInt dataBuffer = (DataBufferInt)cz.image.getRaster().getDataBuffer();
//    	int data[] = dataBuffer.getData();
//      
//    	for (int i = 0; i < data.length; i++) {
//    		final float value = array[i];
//      	
//    		if (Float.isNaN(value))
//    			data[i] = Color.RED.getRGB();
//    		else {
//    			int c = (int)(value * 255);
//    			if (c > 255) c = 255;
//					
//				data[i] = Utils.create_rgb(255, c, c, c);
//    		}
//    	}
//    }

	@Override
	public void gpuMethod(int x, int y) {
		if (x != 0 && y != 0) {
			return;
		}
		
		_(cz, cz.width, cz.cols, !cz.isSingleReceptionField());
	}
	
	public static void _(CortexZoneComplex cz, int width, float[] matrix, boolean inhibitory) {
		
    	float[] cols = new float[matrix.length];
    	System.arraycopy(matrix, 0, cols, 0, matrix.length);
    	
//    	System.arraycopy(sz.cols, 0, sz.rememberCols, 0, sz.cols.length);
//    	Arrays.fill(sz.rememberCols, 1);

    	//max is winner & winner gets all
    	int maxPos = -1;
    	float max = 0;
        int cPos = -1;

        final int linksNumber = cz.inhibitoryLinksSenapse.length;
    	
    	while (true) {
	    	maxPos = -1;
	    	max = 0;
	    	for (int pos = 0; pos < cols.length; pos++) {
	    		if (cols[pos] > max) {
	    			max = cols[pos];
	    			maxPos = pos;
	    		}
	    	}
	    	
//	    	System.out.println(maxPos);
	    	
	        if (maxPos == -1) {
	        	break;
	        }
	        
	        if (!inhibitory) {
	        	Arrays.fill(matrix, 0);
//	        	Arrays.fill(sz.rememberCols, 0);

	        	matrix[maxPos] = max;
//        		sz.rememberCols[maxPos] = max;
        		return;
	        }
	        
        	int maxY = (int)Math.floor(maxPos / width);
        	int maxX = maxPos - (maxY * width);
        	
    		for (int l = 0; l < linksNumber; l++) {
    	    	final int xi = cz.inhibitoryLinksSenapse(maxX, maxY, l, 0);
    	    	final int yi = cz.inhibitoryLinksSenapse(maxX, maxY, l, 1);
    	        
    	    	cPos = (yi * cz.width) + xi;
    	    	cols[cPos] = 0;
            	if (cPos != maxPos)
            		cz.rememberCols[cPos] = 0;
    		}
    		cols[maxPos] = 0;
    	}
	}
}
