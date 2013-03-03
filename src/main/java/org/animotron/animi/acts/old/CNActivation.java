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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CNActivation extends Task {
	
	@RuntimeParam(name = "порог узнавания образа слоем, предотврощяющий кратковременное запоминание")
	public float K_POROG_PAKET_UZNAVANIYA = 0.2f;
	
	@RuntimeParam(name = "порог значимости образа при запоминании")
	public float K_POROG_ZNACH_OBRAZA = 0.14f;

//	@RuntimeParam(name = "соотношение позитивных и негативных весов")
//	public float K_SOOTN_POS_I_NEGATIVE = 0.5f;

	public CNActivation(CortexZoneComplex cz) {
		super(cz);
	}

	private float activity(Mapping m, int x, int y, int p, int empty, int shiftX, int shiftY) {
		float sum = 0.0f;
	    for(int l = 0; l < m.ns_links; l++) {
	    	int xi = m.linksSenapse(x, y, l, 0) + shiftX;
	    	int yi = m.linksSenapse(x, y, l, 1) + shiftY;
	        
	    	if (xi >= 0 && xi < m.frZone.width && yi >= 0 && yi < m.frZone.height) {
		    	if (empty == 0) {
		        	sum += m.frZone.cols.get(xi, yi) / (float)m.ns_links;
		        } else {
		        	sum += m.frZone.cols.get(xi, yi) * m.linksWeight.get(x, y, p, l);
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
		    	float synapse = m.frZone.cols.get(xi, yi);
		        
		        float q = synapse * factor ; //* activity;
		        
		        m.linksWeight.set(q, x, y, p, l);
		        
		        sumQ2 += q * q;
			}
	    }
	    
		float norm = (float) Math.sqrt(sumQ2);
		//XXX: why?
		if (norm == 0)
			return;
		
	    for (int l = 0; l < m.ns_links; l++) {
	        m.linksWeight.set(
        		m.linksWeight.get(x, y, p, l) / norm,
        		x, y, p, l
    		);
	    }
	}

	public void gpuMethod(int x, int y) {
//		Mapping m = cz.in_zones[0];
//		
//		//zero unused links' weight
//		for (int p = 0; p < cz.package_size; p++) {
//			if (cz.freePackageCols.get(x, y, p) == 0) {
//				for (int l = 0; l < m.ns_links; l++) {
//					m.linksWeight.set(0, x, y, p, l);
//				}
//				cz.packageCols(0, x, y, p);
//			}
//		}
//		
//		int rememberOn = 0;
//	    float maximum = 0.0f;
//		
//		//
////		for (int step = 0; step < cz.tremor.length / 2; step++) {
//			int shiftX = 0;//cz.tremor(step, 0);
//			int shiftY = 0;//cz.tremor(step, 1);
//			
//			boolean toRemember = true;
//			
//			for (int p = 0; p < cz.package_size; p++) {
//				
//				int empty = cz.freePackageCols.get(x, y, p);
//				
//				//вычисление активности
//				float sum = activity(m, x, y, p, empty, shiftX, shiftY);
//			    
//			    //проверка на порог
//			    if (empty == 0 && sum < K_POROG_ZNACH_OBRAZA) {
//			    	continue;
//			    }
//			    
//			    //не более единицы
//			    if (sum > 1) {
//			    	sum = 1;
//			    }
//			    
//			    //запонимаем?
//			    if (empty == 0) {
//			    	if (toRemember && p >= rememberOn) {
//			    		//remember only if another do not recognize
//						for (int pi = 0; pi < cz.package_size; pi++) {
//							//skip current package
//							if (pi == p) {
//								continue;
//							}
//							
//							if (activity(m, x, y, pi, empty, shiftX, shiftY) >= K_POROG_PAKET_UZNAVANIYA) {
//								//другой узнал, не запоминать
//								toRemember = false;
//								break;
//							}
//						}
//						
//						if (toRemember) {
//							toRemember = false;
//							rememberOn = p+1;
//							
//							//запоминаем
//							remember(m, x, y, p, shiftX, shiftY);
//							cz.packageCols(sum, x, y, p);
//						}
//			    	}
//			    } else {
//		    		//busy
//				    maximum = Math.max(sum, maximum);
//	    		
//			    	//record maximum of package activity
//				    if (cz.packageCols(x, y, p) < sum) {
//				    	cz.packageCols(sum, x, y, p);
//				    }
//			    }
//			    
////		    	cz.packageCols(1, x, y, p);
//			}
////		}
//		
//		//вычисляем активность тремора
//	    int _cycleCols = 0;
//	    int pN = 0;
//	    for (int p = 0; p < cz.package_size; p++) {
//		    
//	    	if (cz.freePackageCols.get(x, y, p) > 0) {
//			    pN++;
//			    _cycleCols += cz.packageCols(x, y, p);
//		    }
//	    }
//	    
//	    if (pN > 0) {
//			sz.cols.set(_cycleCols / (float)pN, x, y);
//		} else {
//			sz.cols.set(0, x, y);
//		}
	}

	@Override
    protected void release() {
    }
}
