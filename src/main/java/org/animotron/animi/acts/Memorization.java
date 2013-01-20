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
	
	cl_mem cl_linksWeight = null;
	cl_mem cl_freePackageCols = null;
	
	int count = 0;

	@RuntimeParam(name = "порог активации колонки в цикле тремора для блокирования записей в окружении")
	public float K_POROG_ACTIVATION_FINAL = 0.2f;
	
	@RuntimeParam(name = "порог активности пакета при дозапоминании")
	public float K_POROG_ACT_PAKETA = 0.2f;

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
        
        clSetKernelArg(kernel,  6, Sizeof.cl_mem, Pointer.to(cz.cl_senapseOfinhibitoryLinks));
        clSetKernelArg(kernel,  7, Sizeof.cl_int, Pointer.to(new int[] {cz.number_of_inhibitory_links}));

        Mapping m = cz.in_zones[0];
    	
        if (cl_linksWeight == null) {
			cl_linksWeight = clCreateBuffer(
				cz.mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
				m.linksWeight.length * Sizeof.cl_float, Pointer.to(m.linksWeight), null
			);
        }

		clSetKernelArg(kernel,  8, Sizeof.cl_mem, Pointer.to(cl_linksWeight));
        clSetKernelArg(kernel,  9, Sizeof.cl_int, Pointer.to(new int[] {m.ns_links}));

        clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel, 11, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));

        clSetKernelArg(kernel, 12, Sizeof.cl_int, Pointer.to(new int[] {count++}));

        clSetKernelArg(kernel, 13, Sizeof.cl_float, Pointer.to(new float[] {K_POROG_ACTIVATION_FINAL}));
        clSetKernelArg(kernel, 14, Sizeof.cl_float, Pointer.to(new float[] {K_POROG_ACT_PAKETA}));
	}

	@Override
    protected void enqueueReads(cl_command_queue commandQueue) {
        cl_event events[] = new cl_event[] { new cl_event(), new cl_event(), new cl_event() };

        super.enqueueReads(commandQueue, events);
        	
    	// Read the contents of the cl_freePackageCols memory object
    	Pointer freePackageColsTarget = Pointer.to(cz.freePackageCols);
    	clEnqueueReadBuffer(
			commandQueue, cl_freePackageCols, 
			CL_TRUE, 0, cz.freePackageCols.length * Sizeof.cl_int, 
			freePackageColsTarget, 0, null, events[1]);

    	Mapping m = cz.in_zones[0];

        // Read the contents of the cl_linksWeight memory object
        Pointer target = Pointer.to(m.linksWeight);
        clEnqueueReadBuffer(
            commandQueue, cl_linksWeight, 
            CL_TRUE, 0, m.linksWeight.length * Sizeof.cl_float, 
            target, 0, null, events[2]);

        clWaitForEvents(3, events);
    	
    	clReleaseEvent(events[0]);
    	clReleaseEvent(events[1]);
    	clReleaseEvent(events[2]);
    }
	
	@Override
    protected void release() {
		clReleaseMemObject(cl_linksWeight);
		cl_linksWeight = null;

		clReleaseMemObject(cl_freePackageCols);
		cl_freePackageCols = null;
    }
	
	float rememberCols[];
	
	private float rememberCols(int xi, int yi) {
		return rememberCols[(yi * cz.width) + xi];
	}

	private void rememberCols(float value, int xi, int yi) {
		rememberCols[(yi * cz.width) + xi] = value;
	}
	
	public void inhibitoryByActivity(int x, int y) {
		//set 0 in inhibitory zone of the active column
		if (cz.cols(x, y) > K_POROG_ACTIVATION_FINAL) {
	    
		    for(int l = 0; l < cz.number_of_inhibitory_links; l++) {
		    	int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
		    	int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
		        
		        if (xi != x && yi != y) {
	        		rememberCols(0, xi, yi);
	    		}
		    }
	    } else {
    		rememberCols(0, x, y);
	    }
	}
	
	//all packages should be active
	public void checkThatAllActive(int x, int y) {
		int pN = 0;
		int pP = 0;

		if (rememberCols(x, y) > 0.0f) {
		    for (int p = 0; p < cz.package_size; p++) {

			    if (cz.freePackageCols(x, y, p) > 0) {
			    	pN++;
			    	if (cz.packageCols(x, y, p) > K_POROG_ACT_PAKETA) {
			    		pP++;
			    	}
			    }
		    }
		    
		    if (pN != 0) {
			    if (pN == pP) {
				    //запоминаем если все сработали
				    for (int p = 0; p < cz.package_size; p++) {
					    if (cz.freePackageCols(x, y, p) == 0 && cz.packageCols(x, y, p) > 0.0f) {
					    	cz.freePackageCols(count, x, y, p);
					    }
				    }
			    }
			   	rememberCols(0, x, y);
		   	}
	    }
	}
	
	//calculate active neighbor
	public int neighbor(int x, int y) {
		
		int neighbor = 0;

		//поиск незначительной активности по соседям
		//matrix 3x3
	    for (int dx = -1; dx <= 1; dx++) {
	    	if (dx == 0) continue;
	    	
		    for (int dy = -1; dy <= 1; dy++) {
		    	if (dy == 0) continue;
		    	
		    	int xi = x + dx;
		    	int yi = y + dy;

				if (xi < 0 || xi >= cz.width || yi < 0 || yi >= cz.height)
					continue;
			    
		    	for (int p = 0; p < cz.package_size; p++) {
				    if (cz.freePackageCols(xi, yi, p) > 0) {
				    	neighbor++;
				    	break;
		    		}
				}
		    }
	    }
	    return neighbor;
	}
	
	//neighborhood...
	public void inhibitoryByNeighborhood(int x, int y) {
		if (rememberCols(x, y) > 0.0f) {

			//ищим максимум
			float maximum = 0.0f;
	    	
		    for(int l = 0; l < cz.number_of_inhibitory_links; l++) {
		    	int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
		    	int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
		        
		        if (xi != x && yi != y) {
				    for (int p = 0; p < cz.package_size; p++) {

					    if (cz.freePackageCols(xi, yi, p) == 0) {
		        			maximum = Math.max(maximum, cz.packageCols(xi, yi, p));
		        		}
	        		}
	    		}
		    }
		    
	    	float own = 0.0f;
		    for (int p = 0; p < cz.package_size; p++) {
			    
				if (cz.freePackageCols(x, y, p) == 0) {
					own = Math.max(own, cz.packageCols(x, y, p));
				}
			}
		    
		    if (neighbor(x, y) == 0.0f || maximum > own) {
		    	rememberCols(0, x, y);
		    }
	    }
	}
	
	public void remember(int x, int y) {
		if (rememberCols(x, y) > 0.0f) {
		    
			for (int p = 0; p < cz.package_size; p++) {
			    
			    if (cz.freePackageCols(x, y, p) == 0) {
				    
			    	if (cz.packageCols(x, y, p) > 0) {
			    		cz.packageCols(count, x, y, p);
					}
			    }
		    }
	    }
	}
	
	public void cleanup(int x, int y) {
		//free up
		Mapping m = cz.in_zones[0];
		
	    for (int p = 0; p < cz.package_size; p++) {
		    if (cz.freePackageCols(x, y, p) == 0) {
			    for (int l = 0; l < m.ns_links; l++) {
			        m.linksWeight(0, x, y, p, l);
			    }
		    }
	    }
	}
	
	public void execute() {
		count++;
		super.execute();
	}
	
	int phaze = 0;
	
	public void gpuMethod(int x, int y) {

		switch (phaze) {
		case 0:
			rememberCols = new float[sz.cols.length];
			Arrays.fill(rememberCols, 1);
			inhibitoryByActivity(x, y);
			break;

		case 1:
			checkThatAllActive(x, y);
			break;

		case 2:
			inhibitoryByNeighborhood(x, y);
			break;

		case 3:
			remember(x, y);
			break;

		case 4:
			sz.cols = rememberCols;
//			cleanup(x, y);
			break;

		default:
			break;
		}
	}
	
	public boolean isDone() {
		if (phaze == 4) {
			phaze = 0;
			return true;
		}
		
		phaze++;
		return false;
	}
}