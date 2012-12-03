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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
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
	
	public Inhibitory(CortexZoneComplex cz) {
		super(cz);
	}

    /**
     * Set up the OpenCL arguments for this task for the given kernel
     * 
     * @param kernel The OpenCL kernel for which the arguments will be set
     */
    protected void setupArguments(cl_kernel kernel) {
    	super.setupArguments(kernel);

        clSetKernelArg(kernel,  2, Sizeof.cl_float, Pointer.to(new float[] {cz.inhibitory_w * k}));
        clSetKernelArg(kernel,  3, Sizeof.cl_mem, Pointer.to(cz.cl_senapseOfinhibitoryLinks));
        clSetKernelArg(kernel,  4, Sizeof.cl_int, Pointer.to(new int[] {cz.number_of_inhibitory_links}));
        
        final float cols[] = new float[cz.cols.length];
        System.arraycopy(cz.cols, 0, cols, 0, cols.length);
        cl_mem _cols = clCreateBuffer(
    		cz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
    		cols.length * Sizeof.cl_float, Pointer.to(cols), null
		);
        
//        System.out.println("Inhibitory Original");
//        System.out.println(Arrays.toString(cols));

        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(_cols));
    }

	@Override
	protected void processColors(float[] array) {
    	System.out.println("Inhibitory "+array.length);
        System.out.println(Arrays.toString(array));
        
//        cz.refreshImage();
    }

	@Override
	protected void release() {
	}
	
//	@Override
//    public double process(final CortexZoneSimple layer, final int x, final int y, double max) {
//    	final NeuronComplex cn = layer.col[x][y];
//    	
//    	if (cn.activity[0] == 0)
//    		return max;
//
//    	double delta = 0;
//    	for (Link link : cn.s_inhibitoryLinks) {
//    		for (int i = 0; i < 3; i++) {
//    			delta += link.w[i] * link.synapse.activity[i];
//    		}
//    	}
//    	
//    	delta *= k;
//    	
//    	cn.activity[0] = cn.activity[0] - delta;
//    	if (cn.activity[0] < 0 || Double.isNaN(cn.activity[0])) 
//    		cn.activity[0] = 0;
//
//    	return Math.max(max, delta);
//    }
}
