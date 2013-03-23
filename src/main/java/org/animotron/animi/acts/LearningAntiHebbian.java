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
import org.animotron.matrix.Matrix;
import org.animotron.matrix.MatrixMapped;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningAntiHebbian extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.01f;
	
	@RuntimeParam(name = "minWeight")
	public float minWeight = 0;//10^-11;

	private float factor;
	
	public LearningAntiHebbian(LayerWLearning cz) {
		super(cz);
		
		factor = ny;
//		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private static float adjust(
			final Matrix<Float> in, 
			final Matrix<Float> weights, 
			final float activity, 
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < weights.length(); index++) {
			final float inActivity = (1 - in.getByIndex(index));
    		final float q = weights.getByIndex(index) + inActivity * activity * factor;
    		
    		weights.setByIndex(q, index);
    		
    		sumQ2 += q * q;
		}
	    
	    return sumQ2;
	}
	
	private static void normalization(
			final Matrix<Float> posWeights, 
			final Matrix<Float> weights, 
			final float sumQ2, 
			final float minWeight) {
		
		float norm = (float) Math.sqrt(sumQ2);
		for (int index = 0; index < weights.length(); index++) {
	    	
	    	final float q = weights.getByIndex(index) / norm;
	    	
//	    	if (q >= minWeight) {
//		    	final float pos = posWeights.getByIndex(index);
//	    		if (pos > q) {
//	    			weights.setByIndex(minWeight, index);
//	    			
//	    		} else {
//	    			posWeights.setByIndex(minWeight, index);
	    			weights.setByIndex(q, index);
//	    		}
//	    	} else {
//	    		weights.setByIndex(minWeight, index);
//	    	}
	    }
	}

	public static void learn(
			final Matrix<Float> in, 
			final Matrix<Float> posWeights, 
			final Matrix<Float> weights, 
			final float activity, 
			final float factor, 
			final float minWeight) {
		
		if (activity > 0) {
			final float sumQ2 = adjust(in, weights, activity, factor);
			
			normalization(posWeights, weights, sumQ2, minWeight);
		}
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		final Mapping m = cz.in_zones[0];
		
//		for (int p = 0; p < cz.depth; p++) {
			
			if (cz.cols.get(x, y, z) <= 0) {
//				continue;
				return;
			}

			learn(
					new MatrixMapped<Float>(m.frZone().cols, m.senapses().sub(x, y, z)), 
					m.senapseWeight().sub(x, y, z), 
					m.lateralWeight().sub(x, y, z), 
					m.toZone().cols.get(x, y, z),
					factor,
					minWeight
				);
//		}
	}
	
	@Override
    protected void release() {
    }
}
