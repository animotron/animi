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

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class InhibitoryTest extends Task {

	@RuntimeParam(name = "radius")
	int radius = 1;

	@RuntimeParam(name = "factor")
	float factor = 0.2f;

	public InhibitoryTest(LayerWLearning cz) {
		super(cz);
	}

	@Override
	public void prepare() {
		cz.neighbors.fill(0f);
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {

		int fx = x - radius;
		if (fx < 0) fx = 0;
		
		int tx = x + radius;
		if (tx >= cz.width) tx = cz.width - 1;

		int fy = y - radius;
		if (fy < 0) fy = 0;
		
		int ty = y + radius;
		if (ty >= cz.height) ty = cz.height - 1;
		
		int fz = z - radius;
		if (fz < 0) fz = 0;
		
		int tz = z + radius;
		if (tz >= cz.depth) tz = cz.depth - 1;

		float act = 0f;
		for (int xi = fx; xi <= tx; xi++) {
			for (int yi = fy; yi <= ty; yi++) {
				for (int zi = fz; zi <= tz; zi++) {
					act += cz.neurons.get(xi,yi,zi);
				}
			}
		}
		cz.neighbors.set(act, x,y,z);
		
		cz.neurons.set(cz.neurons.get(x,y,z) + (act * factor), x,y,z);
	}
	
	@Override
	public boolean isDone() {
		WinnerGetsAll._(cz, cz.neurons, !cz.isSingleReceptionField());
		
		return true;
	}
	
	@Override
	protected void release() {
	}
}
