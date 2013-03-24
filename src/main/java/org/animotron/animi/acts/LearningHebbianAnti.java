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

import org.animotron.matrix.Matrix;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningHebbianAnti {
	
	private LearningHebbianAnti() {}

	private static float adjust(
			final Matrix<Float> in, 
			final Matrix<Float> posW, 
			final Matrix<Float> negW, 
			final float activity,
			final float avg,
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < negW.length(); index++) {
			
			if (posW.getByIndex(index) > 0f)
				continue;

			final float inActivity = avg - in.getByIndex(index);
    		
			final float q = negW.getByIndex(index) + inActivity * activity * factor;
    		
    		if (q > 0f) {
				negW.setByIndex(q, index);
	    		
	    		sumQ2 += q * q;
    		} else {
				negW.setByIndex(0f, index);
    		}
		}
	    
	    return sumQ2;
	}
	
	private static void normalization(
			final Matrix<Float> posWeights, 
			final Matrix<Float> weights, 
			final float sumQ2) {
		
		float norm = (float) Math.sqrt(sumQ2);
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
		
		final float sumQ2 = adjust(in, posW, negW, activity, avg, factor);
		
		if (sumQ2 > 0f) {
			normalization(posW, negW, sumQ2);
		}
	}
}
