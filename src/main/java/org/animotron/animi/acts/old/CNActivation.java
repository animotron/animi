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
package org.animotron.animi.acts.old;

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
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CNActivation extends Task {
	
	cl_mem cl_linksWeight = null;
	cl_mem cl_freePackageCols = null;
	
	@RuntimeParam(name = "порог узнавания образа слоем, предотврощяющий кратковременное запоминание")
	public float K_POROG_PAKET_UZNAVANIYA = 0.2f;
	
	@RuntimeParam(name = "порог значимости образа при запоминании")
	public float K_POROG_ZNACH_OBRAZA = 0.14f;

//	@RuntimeParam(name = "соотношение позитивных и негативных весов")
//	public float K_SOOTN_POS_I_NEGATIVE = 0.5f;

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
        
//    	clSetKernelArg(kernel,  2, Sizeof.cl_mem, Pointer.to(cz.cl_tremor));
//        clSetKernelArg(kernel,  3, Sizeof.cl_int, Pointer.to(new int[] {cz.tremor.length / 2}));

    	clSetKernelArg(kernel,  4, Sizeof.cl_mem, Pointer.to(cz.cl_packageCols));
    	
        if (cl_freePackageCols == null) {
	        cl_freePackageCols = clCreateBuffer(
	    		cz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
	    		cz.freePackageCols.length * Sizeof.cl_int, Pointer.to(cz.freePackageCols), null
			);
        }
        clSetKernelArg(kernel,  5, Sizeof.cl_mem, Pointer.to(cl_freePackageCols));

        clSetKernelArg(kernel,  6, Sizeof.cl_int, Pointer.to(new int[] {cz.package_size}));

        if (cl_linksWeight == null) {
			cl_linksWeight = clCreateBuffer(
				cz.mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
				m.linksWeight.length * Sizeof.cl_float, Pointer.to(m.linksWeight), null
			);
        }

		clSetKernelArg(kernel,  7, Sizeof.cl_mem, Pointer.to(cl_linksWeight));
        clSetKernelArg(kernel,  8, Sizeof.cl_mem, Pointer.to(m.cl_senapseOfLinks));
        clSetKernelArg(kernel,  9, Sizeof.cl_int, Pointer.to(new int[] {m.ns_links}));
        
        clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(m.frZone.cl_cols));
        clSetKernelArg(kernel, 11, Sizeof.cl_int, Pointer.to(new int[] {m.frZone.width}));

        clSetKernelArg(kernel, 12, Sizeof.cl_float, Pointer.to(new float[] {K_POROG_PAKET_UZNAVANIYA}));
        clSetKernelArg(kernel, 13, Sizeof.cl_float, Pointer.to(new float[] {K_POROG_ZNACH_OBRAZA}));
//        clSetKernelArg(kernel, 14, Sizeof.cl_float, Pointer.to(new float[] {K_SOOTN_POS_I_NEGATIVE}));
	}
    
	@Override
    protected void enqueueReads(cl_command_queue commandQueue) {
        cl_event events[] = new cl_event[] { new cl_event() , new cl_event() , new cl_event() };

    	super.enqueueReads(commandQueue, events);
    	
    	// Read the contents of the cl_pCols memory object
    	Pointer pColsTarget = Pointer.to(cz.packageCols);
    	clEnqueueReadBuffer(
			commandQueue, cz.cl_packageCols, 
			CL_TRUE, 0, cz.packageCols.length * Sizeof.cl_float, 
			pColsTarget, 0, null, events[1]);

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
	
	private float activity(Mapping m, int x, int y, int p, int empty, int shiftX, int shiftY) {
		float sum = 0.0f;
	    for(int l = 0; l < m.ns_links; l++) {
	    	int xi = m.linksSenapse(x, y, l, 0) + shiftX;
	    	int yi = m.linksSenapse(x, y, l, 1) + shiftY;
	        
	    	if (xi >= 0 && xi < m.frZone.width && yi >= 0 && yi < m.frZone.height) {
		    	if (empty == 0) {
		        	sum += m.frZone.cols(xi, yi) / (float)m.ns_links;
		        } else {
		        	sum += m.frZone.cols(xi, yi) * m.linksWeight(x, y, p, l);
		        }
	        }
	    }
	    
	    return sum;
	}
	
	private void remember(Mapping m, int x, int y, int p, int shiftX, int shiftY) {
		
//		float activity = m.toZone.cols(x, y);

		float factor = 0.03f; // ny / Math.pow(2, layer.count / count);

		float sumQ2 = 0.0f;
	    for(int l = 0; l < m.ns_links; l++) {
	    	int xi = m.linksSenapse(x, y, l, 0) + shiftX;
	    	int yi = m.linksSenapse(x, y, l, 1) + shiftY;
	        
			if (xi >= 0 && xi < m.frZone.width && yi >= 0 && yi < m.frZone.height) {
		    	float synapse = m.frZone.cols(xi, yi);
		        
		        float q = synapse * factor ; //* activity;
		        
		        m.linksWeight(q, x, y, p, l);
		        
		        sumQ2 += q * q;
			}
	    }
	    
		float norm = (float) Math.sqrt(sumQ2);
		//XXX: why?
		if (norm == 0)
			return;
		
	    for (int l = 0; l < m.ns_links; l++) {
	        m.linksWeight(
        		m.linksWeight(x, y, p, l) / norm,
        		x, y, p, l
    		);
	    }
	}

	public void gpuMethod(int x, int y) {
		Mapping m = cz.in_zones[0];
		
		//zero unused links' weight
		for (int p = 0; p < cz.package_size; p++) {
			if (cz.freePackageCols(x, y, p) == 0) {
				for (int l = 0; l < m.ns_links; l++) {
					m.linksWeight(0, x, y, p, l);
				}
				cz.packageCols(0, x, y, p);
			}
		}
		
		int rememberOn = 0;
	    float maximum = 0.0f;
		
		//
//		for (int step = 0; step < cz.tremor.length / 2; step++) {
			int shiftX = 0;//cz.tremor(step, 0);
			int shiftY = 0;//cz.tremor(step, 1);
			
			boolean toRemember = true;
			
			for (int p = 0; p < cz.package_size; p++) {
				
				int empty = cz.freePackageCols(x, y, p);
				
				//вычисление активности
				float sum = activity(m, x, y, p, empty, shiftX, shiftY);
			    
			    //проверка на порог
			    if (empty == 0 && sum < K_POROG_ZNACH_OBRAZA) {
			    	continue;
			    }
			    
			    //не более единицы
			    if (sum > 1) {
			    	sum = 1;
			    }
			    
			    //запонимаем?
			    if (empty == 0) {
			    	if (toRemember && p >= rememberOn) {
			    		//remember only if another do not recognize
						for (int pi = 0; pi < cz.package_size; pi++) {
							//skip current package
							if (pi == p) {
								continue;
							}
							
							if (activity(m, x, y, pi, empty, shiftX, shiftY) >= K_POROG_PAKET_UZNAVANIYA) {
								//другой узнал, не запоминать
								toRemember = false;
								break;
							}
						}
						
						if (toRemember) {
							toRemember = false;
							rememberOn = p+1;
							
							//запоминаем
							remember(m, x, y, p, shiftX, shiftY);
							cz.packageCols(sum, x, y, p);
						}
			    	}
			    } else {
		    		//busy
				    maximum = Math.max(sum, maximum);
	    		
			    	//record maximum of package activity
				    if (cz.packageCols(x, y, p) < sum) {
				    	cz.packageCols(sum, x, y, p);
				    }
			    }
			    
//		    	cz.packageCols(1, x, y, p);
			}
//		}
		
		//вычисляем активность тремора
	    int _cycleCols = 0;
	    int pN = 0;
	    for (int p = 0; p < cz.package_size; p++) {
		    
	    	if (cz.freePackageCols(x, y, p) > 0) {
			    pN++;
			    _cycleCols += cz.packageCols(x, y, p);
		    }
	    }
	    
	    if (pN > 0) {
			sz.cols(_cycleCols / (float)pN, x, y);
		} else {
			sz.cols(0, x, y);
		}
	}
}
