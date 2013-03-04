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
public class Inhibitory extends Task {

	@RuntimeParam(name = "k")
	public float k = 0.3f;
	
	@RuntimeParam(name = "minDelta")
	public float minDelta = 0.01f;
	
	public Inhibitory(CortexZoneComplex cz) {
		super(cz);
	}

	private float maxDelta = 0;
	private float preDelta = 0;
	private MatrixFloat cols = null;
	
	@Override
	public void prepare() {
		cols = new MatrixFloat(cz.cols);
	}

	@Override
	public void gpuMethod(int x, int y) {
		
		float delta = 0;
		if (cz.isSingleReceptionField()) {
			for (int xi = 0; xi < cz.width; xi++) {
				for (int yi = 0; yi < cz.height; yi++) {
					
					if (xi != x && yi != y) {
						delta += cz.inhibitory_w * cols.get(xi, yi);
					}
				}
			}
		} else {
			for (int l = 0; l < cz.inhibitory_number_of_links; l++) {
				final int xi = cz.inhibitoryLinksSenapse(x, y, l, 0);
				final int yi = cz.inhibitoryLinksSenapse(x, y, l, 1);
				
				if (xi != x && yi != y) {
					delta += cz.inhibitory_w * cols.get(xi, yi);
				}
			}
		}
		
		float activity = cz.cols.get(x, y);
		activity -= delta * k;
		if (activity < 0) {
			activity = 0;
		}
		
		cz.cols.set(activity, x, y);
		
		synchronized (this) {
			maxDelta = Math.max(maxDelta, delta);
		}
	}
	
	@Override
	public boolean isDone() {
		if (preDelta == 0) {
			preDelta = maxDelta + 1;
		}
		if (preDelta <= maxDelta) {
			System.out.println("maxDelta increased or equal");
			return true;
		}
		return maxDelta > minDelta;
	}
	
	@Override
	protected void release() {
	}
}
