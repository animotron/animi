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
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class ActivationTest extends Task {
	
	public ActivationTest(LayerWLearning cz) {
		super(cz);
	}

	private float activity(final Matrix<Float> in, final Matrix<Float> weights) {
		float sum = 0.0f;

		for (int i = 0; i < weights.length(); i++) {
    		sum += in.getByIndex(i) * weights.getByIndex(i);
	    }
	    
	    return sum;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		final Mapping m = cz.in_zones[0];

		if (m.senapsesCode().get(x,y,z) >= 0) {
			final float activity = 
					activity(
						new MatrixMapped<Float>(m.frZone().axons, m._senapses().sub(x, y, z)), 
						m.senapseWeight().sub(x, y, z)
					);
			
			cz.neurons.set(activity, x, y, z);
		}
	}

	@Override
    protected void release() {
    }
}
