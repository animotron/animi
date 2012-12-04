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

import org.animotron.animi.cortex.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
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
    protected void setupArguments(cl_kernel kernel) {
    	super.setupArguments(kernel);
    	Mapping m = cz.in_zones[0];
        clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(m.cl_links));
        clSetKernelArg(kernel,  3, Sizeof.cl_mem, Pointer.to(m.cl_senapseOfLinks));
        clSetKernelArg(kernel,  4, Sizeof.cl_int, Pointer.to(new int[] {m.ns_links}));
        
//        final float[] cols = m.frZone.cols;
//        cl_mem _cols = clCreateBuffer(
//    		cz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
//    		cols.length * Sizeof.cl_float, Pointer.to(cols), null
//		);
        
//        System.out.println("Activation Original");
//        System.out.println(Arrays.toString(cols));

        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel,  6, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));
    }
    
    protected void release() {
    }


	@Override
	protected void processColors(float[] array) {
//		System.arraycopy(array, 0, cz.beforeInhibitoryCols, 0, array.length);
//    	System.out.println("Activation "+array.length);
//        System.out.println(Arrays.toString(array));
    }

//public class CNActivation implements Act<CortexZoneSimple> {
//
//    @Override
//    public void process(CortexZoneSimple layer, final int x, final int y) {
//    	NeuronComplex cn = layer.col[x][y];
//    	
//    	double activity = 0;
//    	
//    	for (LinkQ q : cn.Qs.values()) {
//    		if (q.delay < q.synapse.activity.length)
//    			activity += q.synapse.activity[q.delay] * q.q;
//    	}
//    	
//    	layer.shift(x, y, activity);
//    }
}
