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
package animi.acts;


import animi.cortex.*;
import animi.matrix.*;

/**
 * Self-organizing feature map.
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningSOMLateral {
	
	public static void learn(
			final MappingSOM m,
			final Matrix<Integer[]> lateralSenapse, 
			final Matrix<Float> lateralWeight, 
			final float act,
			final float factor) {
		
		float sumQ2 = 0f;
		for (int index = 0; index < lateralWeight.length(); index++) {
			Integer[] xyz = lateralSenapse.getByIndex(index);
			
			final int xi = xyz[0];
			final int yi = xyz[1];
			final int zi = xyz[2];
			
			final float q = 
					lateralWeight.getByIndex(index) 
					+ m.toZone().neurons.get(xi, yi, zi) * act * factor;
		
			lateralWeight.setByIndex(q, index);
			
			sumQ2 = q * q;
		}

		Mth.normalization(lateralWeight, sumQ2);
	}
}
