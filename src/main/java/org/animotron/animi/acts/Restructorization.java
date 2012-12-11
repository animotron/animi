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
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Restructorization extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;
	
	public Restructorization(CortexZoneComplex cz) {
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

    	Mapping m = cz.in_zones[0];
    	
    	clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(cz.cl_pCols));
        clSetKernelArg(kernel,  3, Sizeof.cl_int, Pointer.to(new int[] {cz.package_size}));

        clSetKernelArg(kernel,  4, Sizeof.cl_mem, Pointer.to(m.cl_linksWeight));
        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(m.cl_senapseOfLinks));
        clSetKernelArg(kernel,  6, Sizeof.cl_int, Pointer.to(new int[] {m.ns_links}));
        
        clSetKernelArg(kernel,  7, Sizeof.cl_mem, Pointer.to(cz.cl_rememberCols));
        clSetKernelArg(kernel,  8, Sizeof.cl_mem, Pointer.to(cz.cl_freeCols));
        
//        System.out.println("Rest");
//        System.out.println("neighborCols: "+Utils.debug(cz.rememberCols));
//        System.out.println("cycleCols   : "+Utils.debug(cz.cycleCols));
//        System.out.println("freeCols    : "+Utils.debug(cz.freeCols));
//        System.out.println("linksWeight : "+Utils.debug(cz.in_zones[0].linksWeight, 100));

        clSetKernelArg(kernel,  9, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel,  10, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));
    }

	@Override
    protected void enqueueReads(cl_command_queue commandQueue, cl_event events[]) {
//    	super.enqueueReads(commandQueue, events);
        	
        Mapping m = cz.in_zones[0];

        // Read the contents of the cols memory object
        Pointer target = Pointer.to(m.linksWeight);
        clEnqueueReadBuffer(
            commandQueue, m.cl_linksWeight, 
            CL_TRUE, 0, m.linksWeight.length * Sizeof.cl_float, 
            target, 0, null, events[0]);

        clWaitForEvents(1, events);

        // Read the contents of the cl_pCols memory object
    	Pointer pColsTarget = Pointer.to(cz.pCols);
    	clEnqueueReadBuffer(
			commandQueue, cz.cl_pCols, 
			CL_TRUE, 0, cz.pCols.length * Sizeof.cl_float, 
			pColsTarget, 0, null, events[0]);

    	clWaitForEvents(1, events);
    }
	
	@Override
    protected void release() {
//		clReleaseMemObject(_cols);
    }
}