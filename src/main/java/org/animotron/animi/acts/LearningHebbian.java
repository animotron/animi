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
 * Delta rule. http://en.wikipedia.org/wiki/Delta_rule
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningHebbian extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.1f / 5.0f;
	
	private float factor;
	
	public LearningHebbian(CortexZoneComplex cz) {
		super(cz);
		
		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private static float adjust(final Matrix<Float> in, final Matrix<Float> weights, final float activity, final float factor) {
		float sumQ2 = 0.0f;
		for (int index = 0; index < weights.length(); index++) {
    		final float q = weights.getByIndex(index) + in.getByIndex(index) * activity * factor;
    		
    		weights.setByIndex(q, index);
    		
    		sumQ2 += q * q;
		}
	    
	    return sumQ2;
	}

	private static void normalization(final Matrix<Float> weights, final float sumQ2) {
		float norm = (float) Math.sqrt(sumQ2);
		for (int index = 0; index < weights.length(); index++) {
	    	
	    	final float q = weights.getByIndex(index) / norm;
	    	
	    	weights.setByIndex(q, index);
	    }
	}

	public static void learn(final Matrix<Float> in, final Matrix<Float> weights, final float activity, final float factor) {
		if (activity > 0) {
			final float sumQ2 = adjust(in, weights, activity, factor);
			
			normalization(weights, sumQ2);
		}
	}

//	private float adjust(final Mapping m, final int x, final int y, final int p) {
//		final float toCheck = 
//			adjust(
//				new MatrixMapped<Float>(m.frZone.cols, m.linksSenapse.sub(x, y)), 
//				m.linksWeight.sub(x, y, p), 
//				m.toZone.cols.get(x, y), 
//				factor
//			);
//
//		float sum = 0;
//		
//		float sumQ2 = 0.0f;
//	    for(int l = 0; l < m.ns_links; l++) {
//	    	int xi = m.linksSenapse.get(x, y, l, 0);
//	    	int yi = m.linksSenapse.get(x, y, l, 1);
//	        
//	    	if (xi >= 0 && xi < m.frZone.width && yi >= 0 && yi < m.frZone.height) {
//	    		
//	    		sum += m.frZone.cols.get(xi, yi);
//	    		
//	    		final float q = m.linksWeight.get(x, y, p, l) + m.frZone.cols.get(xi, yi) * m.toZone.cols.get(x, y) * factor;
//	    		
//	    		m.linksWeight.set(q, x, y, p, l);
//	    		
//	    		sumQ2 += q * q;
//	        }
//	    }
//	    
//	    if (sum == 0) {
//	    	System.out.println("?!");
//	    }
//	    if (toCheck != sumQ2) {
//	    	System.out.println("WRONG sumQ2!!!");
//	    }
//	    return sumQ2;
//	}
//	
//	private void normalization(final Mapping m, final int x, final int y, final int p, final float sumQ2) {
//		float norm = (float) Math.sqrt(sumQ2);
//	    for(int l = 0; l < m.ns_links; l++) {
//	    	
//	    	final float q = m.linksWeight.get(x, y, p, l) / norm;
//	    	
//	    	m.linksWeight.set(q, x, y, p, l);
//	    }
//	}

	public void gpuMethod(int x, int y) {
		
		final Mapping m = cz.in_zones[0];
		
		for (int p = 0; p < cz.package_size; p++) {
		
			if (cz.colNeurons.get(x, y, p) <= 0) {
				continue;
			}
			
			learn(
				new MatrixMapped<Float>(m.frZone.cols, m.linksSenapse.sub(x, y)), 
				m.linksWeight.sub(x, y, p), 
				m.toZone.cols.get(x, y),
				factor
			);

//			final float sumQ2 = adjust(m, x, y, p);
//			
////			if (sumQ2 == 0 || Float.isInfinite(sumQ2) || Float.isNaN(sumQ2)) {
////				adjust(m, x, y);
////			}
//			
//			normalization(m, x, y, p, sumQ2);
		}
	}

	@Override
    protected void release() {
    }
}
