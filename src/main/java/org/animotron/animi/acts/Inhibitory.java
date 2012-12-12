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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Inhibitory extends Task {

	@RuntimeParam(name = "k")
	public float k = 0.3f;
	
	cl_mem _cols;
	
	public Inhibitory(CortexZoneComplex cz) {
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

        clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(cz.cl_senapseOfinhibitoryLinks));
        clSetKernelArg(kernel,  3, Sizeof.cl_int, Pointer.to(new int[] {cz.number_of_inhibitory_links}));
        
    	clSetKernelArg(kernel,  4, Sizeof.cl_mem, Pointer.to(cz.cl_pCols));
        clSetKernelArg(kernel,  5, Sizeof.cl_int, Pointer.to(new int[] {cz.package_size}));

        clSetKernelArg(kernel,  6, Sizeof.cl_mem, Pointer.to(cz.cl_rememberCols));
        clSetKernelArg(kernel,  7, Sizeof.cl_mem, Pointer.to(cz.cl_cycleCols));
        clSetKernelArg(kernel,  8, Sizeof.cl_mem, Pointer.to(cz.cl_freeCols));

        final float cols[] = new float[cz.cols.length];
        System.arraycopy(cz.cols, 0, cols, 0, cols.length);
        _cols = clCreateBuffer(
    		cz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
    		cols.length * Sizeof.cl_float, Pointer.to(cols), null
		);
        
        clSetKernelArg(kernel,  9, Sizeof.cl_mem, Pointer.to(_cols));
    }

	@Override
    protected void enqueueReads(cl_command_queue commandQueue, cl_event events[]) {
    	super.enqueueReads(commandQueue, events);
    	
        // Read the contents of the cl_neighborCols memory object
    	Pointer target = Pointer.to(sz.rememberCols);
    	clEnqueueReadBuffer(
			commandQueue, sz.cl_rememberCols, 
			CL_TRUE, 0, sz.rememberCols.length * Sizeof.cl_float, 
			target, 0, null, events[0]);

    	clWaitForEvents(1, events);
	}

//	@Override
//    protected void processColors(float array[]) {
//    	DataBufferInt dataBuffer = (DataBufferInt)cz.image.getRaster().getDataBuffer();
//    	int data[] = dataBuffer.getData();
//      
//    	for (int i = 0; i < data.length; i++) {
//    		final float value = sz.rememberCols[i];
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
		clReleaseMemObject(_cols);
	}
}
