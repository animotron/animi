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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningAntiHebbian extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.1f / 5.0f;
	
	private float factor;
	
	public LearningAntiHebbian(CortexZoneComplex cz) {
		super(cz);
		
		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private float adjust(final Mapping m, final int x, final int y, final int p) {
		float sum = 0;
		
		float sumQ2 = 0.0f;
	    for(int l = 0; l < m.ns_links; l++) {
	    	int xi = m.linksSenapse(x, y, l, 0);
	    	int yi = m.linksSenapse(x, y, l, 1);
	        
	    	if (xi >= 0 && xi < m.frZone.width && yi >= 0 && yi < m.frZone.height) {
	    		
	    		sum += m.frZone.cols.get(xi, yi);
	    		
	    		final float q = m.inhibitoryWeight.get(x, y, p, l) + (1 - m.frZone.cols.get(xi, yi)) * m.toZone.cols.get(x, y) * factor;
	    		
	    		m.inhibitoryWeight.set(q, x, y, p, l);
	    		
	    		sumQ2 += q * q;
	        }
	    }
	    
	    if (sum == 0) {
	    	System.out.println("?!");
	    }
	    return sumQ2;
	}
	
	private void normalization(final Mapping m, final int x, final int y, final int p, final float sumQ2) {
		float norm = (float) Math.sqrt(sumQ2);
	    for(int l = 0; l < m.ns_links; l++) {
	    	
	    	final float pos = m.linksWeight.get(x, y, p, l);
	    	final float neg = m.inhibitoryWeight.get(x, y, p, l) / norm;
	    	if (pos >= neg) {
	    		m.linksWeight.set(pos - neg, x, y, p, l);
		    	m.inhibitoryWeight.set(0, x, y, p, l);
	    	} else {
	    		m.linksWeight.set(0, x, y, p, l);
		    	m.inhibitoryWeight.set(neg - pos, x, y, p, l);
	    	}
	    }
	}

	public void gpuMethod(final int x, final int y) {
		
		if (cz.cols.get(x, y) <= 0) {
			return;
		}

		final Mapping m = cz.in_zones[0];
		
		for (int p = 0; p < cz.package_size; p++) {
		
			if (cz.packageCols.get(x, y, p) <= 0) {
				continue;
			}
			
			final float sumQ2 = adjust(m, x, y, p);
			
//			if (sumQ2 == 0 || Float.isInfinite(sumQ2) || Float.isNaN(sumQ2)) {
//				adjust(m, x, y);
//			}
			
			normalization(m, x, y, p, sumQ2);
		}
	}
	
	@Override
    protected void release() {
    }
}
