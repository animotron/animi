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

import animi.RuntimeParam;
import animi.cortex.*;
import animi.matrix.Floats;
import animi.matrix.FloatsMapped;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class ActivationHebbianAnti extends Task {
	
	@RuntimeParam(name = "k")
	public float k = 100f;

	public ActivationHebbianAnti(LayerWLearning cz) {
		super(cz);
	}

	public static float activity(final Floats in, final Floats weights) {
		float sum = 0.0f;

		for (int i = 0; i < weights.length(); i++) {
    		sum += in.getByIndex(i) * weights.getByIndex(i);
	    }
	    
	    return sum;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		final Mapping m = cz.in_zones[0];
		
		final float activity = 
				cz.neurons.get(x, y, z) - 
				k * activity(
					new FloatsMapped(m.frZone().axons, m._senapses().sub(x, y, z)), 
					m.inhibitoryWeight().sub(x, y, z)
				);
	
		cz.neurons.set(activity < 0 ? 0 : activity, x, y, z);
	}

	@Override
    protected void release() {
    }
}
