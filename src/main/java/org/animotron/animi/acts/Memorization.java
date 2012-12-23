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
public class Memorization extends Task {
	
	cl_mem cl_freePackageCols = null;

	@RuntimeParam(name = "count")
	public int count = 10000;
	
	public Memorization(CortexZoneComplex cz) {
		super(cz);
	}

    /**
     * Set up the OpenCL arguments for this task for the given kernel
     * 
     * @param kernel The OpenCL kernel for which the arguments will be set
     */
	@Override
    protected void setupArguments(cl_kernel kernel) {
    	clSetKernelArg(kernel,  0, Sizeof.cl_mem, Pointer.to(cz.cl_cols));
        clSetKernelArg(kernel,  1, Sizeof.cl_int, Pointer.to(new int[] {cz.width}));

    	clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(cz.cl_packageCols));
    	
        if (cl_freePackageCols == null) {
	        cl_freePackageCols = clCreateBuffer(
	    		cz.mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, 
	    		cz.freePackageCols.length * Sizeof.cl_int, Pointer.to(cz.freePackageCols), null
			);
        }
    	clSetKernelArg(kernel,  3, Sizeof.cl_mem, Pointer.to(cl_freePackageCols));
        clSetKernelArg(kernel,  4, Sizeof.cl_int, Pointer.to(new int[] {cz.package_size}));

        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(cz.cl_rememberCols));
        
    	Mapping m = cz.in_zones[0];
    	
        clSetKernelArg(kernel,  6, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel,  7, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));
    }

	@Override
    protected void enqueueReads(cl_command_queue commandQueue) {
        cl_event events[] = new cl_event[] { new cl_event(), new cl_event() };

        super.enqueueReads(commandQueue, events);
        	
    	// Read the contents of the cl_freePackageCols memory object
    	Pointer freePackageColsTarget = Pointer.to(cz.freePackageCols);
    	clEnqueueReadBuffer(
			commandQueue, cl_freePackageCols, 
			CL_TRUE, 0, cz.freePackageCols.length * Sizeof.cl_int, 
			freePackageColsTarget, 0, null, events[1]);

    	clWaitForEvents(2, events);
    	
    	clReleaseEvent(events[0]);
    	clReleaseEvent(events[1]);
    }
	
	@Override
    protected void release() {
		clReleaseMemObject(cl_freePackageCols);
		cl_freePackageCols = null;
    }
}