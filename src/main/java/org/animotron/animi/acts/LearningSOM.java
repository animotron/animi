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
 * Self-organizing feature map.
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningSOM extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.01f;
	
	private float factor;
	
	public LearningSOM(LayerWLearning cz) {
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

	public static void learn(
			final MappingSOM m,
			final Matrix<Integer> lateralSenapse, 
			final Matrix<Float> lateralWeight, 
			final float factor,
			final int z) {
		
		for (int index = 0; index < lateralWeight.length(); index++) {
			
			final int xi = lateralSenapse.getByIndex(index*2 + 0);
			final int yi = lateralSenapse.getByIndex(index*2 + 0);
		
			final Matrix<Float> weights = m.senapseWeight().sub(xi, yi, z);
			
			final float sumQ2 = adjust(
					new MatrixMapped<Float>(m.frZone().cols, m.senapses().sub(xi, yi)), 
					m.senapseWeight().sub(xi, yi, z), 
					m.toZone().cols.get(xi, yi, z),
					factor
			);
			
			normalization(weights, sumQ2);
		}
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		final MappingSOM m = (MappingSOM) cz.in_zones[0];
		
//		for (int p = 0; p < cz.depth; p++) {
		
			if (cz.cols.get(x, y, z) <= 0) {
//				continue;
				return;
			}
			
			learn(
				m,
				m.lateralSenapse().sub(x, y, z),
				m.lateralWeight().sub(x, y, z),
				factor,
				z
			);
//		}
	}

	@Override
    protected void release() {
    }
}
