/*
 *  Copyright (C) 2012 The Animo Project
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

import java.awt.Color;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import org.animotron.animi.Utils;
import org.animotron.animi.cortex.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_event;
import org.jocl.cl_kernel;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CNActivation extends Task {
	
	public CNActivation(CortexZoneComplex cz) {
		super(cz);
	}

    /**
     * Set up the OpenCL arguments for this task for the given kernel
     * 
     * @param kernel The OpenCL kernel for which the arguments will be set
     */
	@Override
    protected void setupArguments(cl_kernel kernel) {
    	super.setupArguments(kernel);
    	Mapping m = cz.in_zones[0];
        
//    	System.out.println("before activation");
////    	System.out.println("frZone.cols "+Arrays.toString(m.frZone.cols));
//    	System.out.println("sz.cols "+Utils.debug(sz.cols));
//    	System.out.println("sz.freeCols "+Arrays.toString(sz.freeCols));
//    	System.out.println("cz.pCols "+Utils.debug(cz.pCols));

    	clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(cz.cl_pCols));
        clSetKernelArg(kernel,  3, Sizeof.cl_int, Pointer.to(new int[] {cz.package_size}));

        clSetKernelArg(kernel,  4, Sizeof.cl_mem, Pointer.to(m.cl_linksWeight));
        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(m.cl_senapseOfLinks));
        clSetKernelArg(kernel,  6, Sizeof.cl_int, Pointer.to(new int[] {m.ns_links}));
        
        clSetKernelArg(kernel,  7, Sizeof.cl_mem, Pointer.to(sz.cl_cycleCols));
        clSetKernelArg(kernel,  8, Sizeof.cl_mem, Pointer.to(sz.cl_freeCols));

        clSetKernelArg(kernel,  9, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel,  10, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));
    }
    
	@Override
    protected void enqueueReads(cl_command_queue commandQueue, cl_event events[]) {
    	super.enqueueReads(commandQueue, events);
    	
        // Read the contents of the cl_cycleCols memory object
    	Pointer cycleColsTarget = Pointer.to(sz.cycleCols);
    	clEnqueueReadBuffer(
			commandQueue, sz.cl_cycleCols, 
			CL_TRUE, 0, sz.cycleCols.length * Sizeof.cl_float, 
			cycleColsTarget, 0, null, events[0]);

    	clWaitForEvents(1, events);

    	// Read the contents of the cl_freeCols memory object
    	Pointer freeColsTarget = Pointer.to(sz.freeCols);
    	clEnqueueReadBuffer(
			commandQueue, sz.cl_freeCols, 
			CL_TRUE, 0, sz.freeCols.length * Sizeof.cl_float, 
			freeColsTarget, 0, null, events[0]);

    	clWaitForEvents(1, events);

    	Mapping m = cz.in_zones[0];

    	// Read the contents of the cl_pCols memory object
    	Pointer pColsTarget = Pointer.to(m.toZone.pCols);
    	clEnqueueReadBuffer(
			commandQueue, m.toZone.cl_pCols, 
			CL_TRUE, 0, m.toZone.pCols.length * Sizeof.cl_float, 
			pColsTarget, 0, null, events[0]);

    	clWaitForEvents(1, events);
    	
//    	System.out.println("after activation");
////    	System.out.println("frZone.cols "+Arrays.toString(m.frZone.cols));
//    	System.out.println("sz.cols     "+Utils.debug(sz.cols));
//    	System.out.println("sz.freeCols "+Utils.debug(sz.freeCols));
////    	System.out.println("sz.freeCols "+Arrays.toString(sz.freeCols));
//    	System.out.println("cz.pCols    "+Utils.debug(cz.pCols));
    }

//	@Override
//    protected void processColors(float array[]) {
//    	DataBufferInt dataBuffer = (DataBufferInt)cz.image.getRaster().getDataBuffer();
//    	int data[] = dataBuffer.getData();
//      
//    	for (int i = 0; i < data.length; i++) {
//    		final float value = sz.freeCols[i];
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
    protected void release() {
    }
}
