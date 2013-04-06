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

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Inhibitory extends Task {

	@RuntimeParam(name = "k")
	public float k = 0.3f;
	
	@RuntimeParam(name = "minDelta")
	public float minDelta = 0.01f;
	
	public Inhibitory(LayerWLearning cz) {
		super(cz);
	}

	private float maxDelta = 0f;
	private Matrix<Float> cols = null;
	
	@Override
	public void prepare() {
		cols = cz.neurons.copy();
		maxDelta = 0f;
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		
		float delta = 0;
		if (cz.isSingleReceptionField()) {
			for (int xi = 0; xi < cz.width; xi++) {
				for (int yi = 0; yi < cz.height; yi++) {
					for (int zi = 0; zi < cz.depth; zi++) {
					
						if (xi != x && yi != y && zi != z) {
							delta += cz.inhibitory_w * cols.get(xi, yi, zi);
						}
					}
				}
			}
		} else {
			for (int l = 0; l < cz.inhibitory_number_of_links; l++) {
				final int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
				final int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
				final int zi = cz.inhibitoryLinksSenapse(x, y, l, 2);
				
				if (xi != x && yi != y && zi != z) {
					delta += cz.inhibitory_w * cols.get(xi, yi, zi);
				}
			}
		}
		
		float activity = cz.neurons.get(x, y, z);
		activity -= delta * k;
		if (activity < 0f) {
			activity = 0f;
			delta = 0f;
		}
		
		cz.neurons.set(activity, x, y, z);
		
		synchronized (this) {
			maxDelta = Math.max(maxDelta, delta);
		}
	}
	
	@Override
	public boolean isDone() {
		boolean isDone = maxDelta < minDelta;
		if (!isDone) {
			maxDelta = 0f;
			cols = cz.neurons.copy();
		}
		
		return isDone;
	}
	
	@Override
	protected void release() {
	}
}
