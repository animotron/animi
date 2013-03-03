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
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Memorization extends Task {
	
	int count = 0;

	@RuntimeParam(name = "порог активации колонки в цикле тремора для блокирования записей в окружении")
	public float K_POROG_ACTIVATION_FINAL = 0.2f;
	
	@RuntimeParam(name = "порог активности пакета при дозапоминании")
	public float K_POROG_ACT_PAKETA = 0.2f;

	public Memorization(CortexZoneComplex cz) {
		super(cz);
	}

	Matrix rememberCols;
	
	public void inhibitoryByActivity(int x, int y) {
		//set 0 in inhibitory zone of the active column
		if (cz.cols.get(x, y) > K_POROG_ACTIVATION_FINAL) {
	    
		    for(int l = 0; l < cz.inhibitory_number_of_links; l++) {
		    	int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
		    	int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
		        
		        if (xi != x && yi != y) {
	        		rememberCols.set(0, xi, yi);
	    		}
		    }
	    } else {
    		rememberCols.set(0, x, y);
	    }
	}
	
	//all packages should be active
	public void checkThatAllActive(int x, int y) {
//		int pN = 0;
//		int pP = 0;
//
//		if (rememberCols.get(x, y) > 0.0f) {
//		    for (int p = 0; p < cz.package_size; p++) {
//
//			    if (cz.freePackageCols.get(x, y, p) > 0) {
//			    	pN++;
//			    	if (cz.packageCols.get(x, y, p) > K_POROG_ACT_PAKETA) {
//			    		pP++;
//			    	}
//			    }
//		    }
//		    
//		    if (pN != 0) {
//			    if (pN == pP) {
//				    //запоминаем если все сработали
//				    for (int p = 0; p < cz.package_size; p++) {
//					    if (cz.freePackageCols.get(x, y, p) == 0 && cz.packageCols(x, y, p) > 0.0f) {
//					    	cz.freePackageCols.set(count, x, y, p);
//					    }
//				    }
//			    }
//			   	rememberCols.set(0, x, y);
//		   	}
//	    }
	}
	
	//calculate active neighbor
	public int neighbor(int x, int y) {
		
		int neighbor = 0;
//
//		//поиск незначительной активности по соседям
//		//matrix 3x3
//	    for (int dx = -1; dx <= 1; dx++) {
//	    	if (dx == 0) continue;
//	    	
//		    for (int dy = -1; dy <= 1; dy++) {
//		    	if (dy == 0) continue;
//		    	
//		    	int xi = x + dx;
//		    	int yi = y + dy;
//
//				if (xi < 0 || xi >= cz.width || yi < 0 || yi >= cz.height)
//					continue;
//			    
//		    	for (int p = 0; p < cz.package_size; p++) {
//				    if (cz.freePackageCols.get(xi, yi, p) > 0) {
//				    	neighbor++;
//				    	break;
//		    		}
//				}
//		    }
//	    }
	    return neighbor;
	}
	
	//neighborhood...
	public void inhibitoryByNeighborhood(int x, int y) {
//		if (rememberCols.get(x, y) > 0.0f) {
//
//			//ищим максимум
//			float maximum = 0.0f;
//	    	
//		    for(int l = 0; l < cz.inhibitory_number_of_links; l++) {
//		    	int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
//		    	int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
//		        
//		        if (xi != x && yi != y) {
//				    for (int p = 0; p < cz.package_size; p++) {
//
//					    if (cz.freePackageCols.get(xi, yi, p) == 0) {
//		        			maximum = Math.max(maximum, cz.packageCols(xi, yi, p));
//		        		}
//	        		}
//	    		}
//		    }
//		    
//	    	float own = 0.0f;
//		    for (int p = 0; p < cz.package_size; p++) {
//			    
//				if (cz.freePackageCols.get(x, y, p) == 0) {
//					own = Math.max(own, cz.packageCols(x, y, p));
//				}
//			}
//		    
//		    if (neighbor(x, y) == 0.0f || maximum > own) {
//		    	rememberCols.set(0, x, y);
//		    }
//	    }
	}
	
	public void remember(int x, int y) {
//		if (rememberCols.get(x, y) > 0.0f) {
//		    
//			for (int p = 0; p < cz.package_size; p++) {
//			    
//			    if (cz.freePackageCols.get(x, y, p) == 0) {
//				    
//			    	if (cz.packageCols(x, y, p) > 0) {
//			    		cz.packageCols(count, x, y, p);
//					}
//			    }
//		    }
//	    }
	}
	
	public void cleanup(int x, int y) {
		//free up
//		Mapping m = cz.in_zones[0];
//		
//	    for (int p = 0; p < cz.package_size; p++) {
//		    if (cz.freePackageCols.get(x, y, p) == 0) {
//			    for (int l = 0; l < m.ns_links; l++) {
//			        m.linksWeight.set(0, x, y, p, l);
//			    }
//		    }
//	    }
	}
	
	public void execute() {
		count++;
		super.execute();
	}
	
	int phaze = 0;
	
	public void gpuMethod(int x, int y) {

		switch (phaze) {
		case 0:
			rememberCols = new Matrix(sz.cols);
			rememberCols.fill(1);
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
	
	@Override
    protected void release() {
    }
}