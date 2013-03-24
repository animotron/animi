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
import org.animotron.matrix.*;

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
	public float ny = 1f;
	
	private float factor;
	
	public LearningHebbian(LayerWLearning cz) {
		super(cz);
		
		factor = ny;
//		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private static float adjust(
			final Matrix<Float> in, 
			final Matrix<Float> posW, 
			final Matrix<Float> negW, 
			final float activity, 
			final float avg,
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < posW.length(); index++) {
    		
			if (negW.getByIndex(index) > 0f)
				continue;

			final float X = in.getByIndex(index) - avg;
			
			final float q = posW.getByIndex(index) + X * activity * factor;
			if (q > 0f) {
	    		posW.setByIndex(q, index);
			
	    		sumQ2 += q * q;
			} else {
	    		posW.setByIndex(0f, index);
			}
    		
		}
	    
	    return sumQ2;
	}

	private static void normalization(
			final Matrix<Float> weights, 
			final float sumQ2) {
		
		final float norm = (float) Math.sqrt(sumQ2);
		for (int index = 0; index < weights.length(); index++) {
	    	
	    	final float q = weights.getByIndex(index) / norm;
	    	
    		weights.setByIndex(q, index);
	    }
	}

	public static void learn(
			final Matrix<Float> in, 
			final Matrix<Float> posW, 
			final Matrix<Float> negW, 
			final float activity,
			final float avg,
			final float factor) {
		
		if (activity > 0) {
			final float sumQ2 = adjust(in, posW, negW, activity, avg, factor);
			
			if (sumQ2 > 0f)
				normalization(posW, sumQ2);
		}
	}
	
	private float avg = 0f;

	public void prepare() {
		avg = 0f;
		
		final Mapping m = cz.in_zones[0];

		final MatrixFloat neurons = m.frZone().axons;
		
		float sum = 0f;
		for (int index = 0; index < neurons.length(); index++) {
			sum += neurons.getByIndex(index);
		}
		
		avg = sum / (float)neurons.length();
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		final Mapping m = cz.in_zones[0];
		
		final float act = m.toZone().neurons.get(x, y, z);
		
		if (act <= 0) {
			return;
		}

		Matrix<Float> in = new MatrixMapped<Float>(m.frZone().neurons, m.senapses().sub(x, y, z));
		Matrix<Float> posW = m.senapseWeight().sub(x, y, z);
		Matrix<Float> negW = m.inhibitoryWeight().sub(x, y, z);
		
		LearningHebbian.learn(
			in, 
			posW,
			negW,
			act,
			avg,
			factor
		);

		LearningHebbianAnti.learn(
			in, 
			posW, 
			negW, 
			act,
			avg,
			factor
		);
	}

	@Override
    protected void release() {
    }
}
