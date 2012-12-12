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
package org.animotron.animi.cortex;

import static org.jocl.CL.*;

import java.awt.Color;
import java.awt.image.DataBufferInt;

import org.animotron.animi.Utils;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

/**
 * Abstract base class for tasks that refer to a region of the Mandelbrot 
 * set. The processColors method of this class may be implemented to 
 * either fill the whole image with the preview, or to fill a small 
 * region of the image with a single tile.
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public abstract class Task {
	
	protected CortexZoneSimple sz;
	protected CortexZoneComplex cz = null;
	
    protected cl_mem outputMem;
    
    /**
     * Creates a new Task that computes.
     * 
     * @param outputMem The target memory object
     */
    protected Task(CortexZoneSimple sz) {
    	this.sz = sz;
    	if (sz instanceof CortexZoneComplex) {
    		this.cz = (CortexZoneComplex) sz;
		}
    }
    
    /**
     * Set up the OpenCL arguments for this task for the given kernel
     * 
     * @param kernel The OpenCL kernel for which the arguments will be set
     */
    protected void setupArguments(cl_kernel kernel) {
        clSetKernelArg(kernel,  0, Sizeof.cl_mem, Pointer.to(sz.cl_cols));
        clSetKernelArg(kernel,  1, Sizeof.cl_int, Pointer.to(new int[] {sz.width}));
    }
    
    /**
     * Will execute this task with the given kernel on the given 
     * command queue
     * 
     * @param kernel The kernel
     * @param commandQueue The command queue
     */
    public void execute(cl_kernel kernel, cl_command_queue commandQueue) {
        setupArguments(kernel);
        
//        System.out.println(""+this.getClass().getName()+" "+sz.width+":"+sz.height);
        
        cl_event events[] = new cl_event[] { new cl_event() };
        
        long globalWorkSize[] = new long[2];
        globalWorkSize[0] = sz.width;
        globalWorkSize[1] = sz.height;

        clEnqueueNDRangeKernel(
            commandQueue, 
            kernel, 2, null, 
            globalWorkSize, null, 0, null, events[0]);
        
        clWaitForEvents(1, events);
        
//        Utils.printBenchmarkInfo("Event calc", events[0]);
        
        enqueueReads(commandQueue, events);

//        clWaitForEvents(1, events);
        
//        Utils.printBenchmarkInfo("Reading", events[0]);

//        cz.cols = result;
        
//        System.out.println(this.getClass().getName());
//        System.out.println(Arrays.toString(cz.cols));

//        convertIterationsToColors(result);
//        processColors(sz.cols);
        
        release();
        
        clReleaseEvent(events[0]);
    }
    
    protected void enqueueReads(cl_command_queue commandQueue, cl_event events[]) {
        // Read the contents of the cols memory object
    	Pointer target = Pointer.to(sz.cols);
    	clEnqueueReadBuffer(
			commandQueue, sz.cl_cols, 
			CL_TRUE, 0, sz.cols.length * Sizeof.cl_float, 
			target, 0, null, events[0]);

    	clWaitForEvents(1, events);
    	
//    	System.out.println("cols "+Utils.debug(sz.cols));
	}

	protected abstract void release();
    
//    protected void processColors(float array[]) {
//    	cz.refreshImage();
//    }
}
