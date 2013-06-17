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
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningAsItis extends Task {
	
	public LearningAsItis(LayerWLearning cz) {
		super(cz);
	}

	public static void learn(
			final Floats in, 
			final Floats posW) {
		
		for (int index = 0; index < posW.length(); index++) {
			if (Float.isNaN(in.getByIndex(index))) {
				System.out.println("!");
			}
			posW.setByIndex(in.getByIndex(index), index);
		}
	}
	
	@Override
	public boolean prepare() {
		return true;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		
		if (cz.toLearning.get(x,y,z) <= 0f) return;
		
		final Mapping m = cz.in_zones[0];
		
		if (m.senapsesCode().get(x,y,z) >= 0f)
			return;

		m.senapsesCode().set((float)cz.app.getStimulator().getCode(), x,y,z);
		
		Floats in = new FloatsMapped(m.frZone().axons, m._senapses().sub(x, y, z));
		Floats posW = m.senapseWeight().sub(x, y, z);
		
		LearningAsItis.learn(
			in, 
			posW
		);
	}

	@Override
    protected void release() {
    }
}