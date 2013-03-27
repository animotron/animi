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

import org.animotron.animi.cortex.*;
import org.animotron.matrix.*;

/**
 * Self-organizing feature map.
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningSOMAnti {
	
	private static float adjust(
			final Matrix<Float> in, 
			final Matrix<Float> posW, 
			final Matrix<Float> negW, 
			final float avg, 
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < negW.length(); index++) {
			
			if (posW.getByIndex(index) > 0f) {
	    		
				negW.setByIndex(0f, index);
	    		continue;
			}

			final float inActivity = avg - in.getByIndex(index);
    		
			final float q = negW.getByIndex(index) + inActivity * factor;
    		
    		if (q > 0f) {
	    		negW.setByIndex(q, index);
	    		
	    		sumQ2 += q * q;
    		} else {
	    		negW.setByIndex(0f, index);
    		}
		}
	    
	    return sumQ2;
	}

	public static void learn(
			final MappingSOM m,
			final Matrix<Integer[]> lateralSenapse, 
			final Matrix<Float> lateralWeight, 
			final float avg,
			final float factor) {
		
		for (int index = 0; index < lateralWeight.length(); index++) {
			Integer[] xyz = lateralSenapse.getByIndex(index);
			
			final int xi = xyz[0];
			final int yi = xyz[1];
			final int zi = xyz[2];
		
			final Matrix<Float> posW = m.senapseWeight().sub(xi, yi, zi);
			final Matrix<Float> negW = m.inhibitoryWeight().sub(xi, yi, zi);
			
			final float sumQ2 = adjust(
					new MatrixMapped<Float>(m.frZone().axons, m._senapses().sub(xi, yi, zi)), 
					posW,
					negW, 
					avg,
					factor * lateralWeight.getByIndex(index)
			);
			
//			System.out.println("["+xi+","+yi+","+zi+"] "+lateralWeight.getByIndex(index));
			
			Mth.normalization(negW, sumQ2);
		}
	}
}
