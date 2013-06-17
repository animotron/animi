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
package animi.tuning;

import animi.cortex.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CodeActivation extends Task {
	
	public CodeActivation(LayerWLearning cz) {
		super(cz);
	}
	
	@Override
	public boolean prepare() {
		return true;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		final Mapping m = cz.in_zones[0];
		
		final int[] pos = m.senapses().get(x,y,z,0);
		final float in = m.frZone().axons().get(pos[0], pos[1], pos[2]);
		final float mem = m.senapseWeight().get(x,y,z,0);
		
		cz.neurons.set(Codes.distance(mem, in), x,y,z);
	}
	
	public boolean isDone() {
		return true;
	}

	@Override
    protected void release() {
    }
}