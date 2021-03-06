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

import animi.Params;
import animi.cortex.*;
import animi.matrix.FloatsProxy;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Activation extends Task {
	
//	@Params
	private ActivationHebbian positive;
	@Params
	private ActivationHebbianAnti negative;
	
	public Activation(LayerWLearning cz) {
		super(cz);
		
		positive = new ActivationHebbian(cz);
		negative = new ActivationHebbianAnti(cz);
	}
	
	private int stage = 0;
	
	@Override
	public boolean prepare() {
		positive.prepare();
		negative.prepare();
		
		stage = 0;
		
		return true;
	}

	public void gpuMethod(final int x, final int y, final int z) {
//		final Mapping m = cz.in_zones[0];
		FloatsProxy pack;
		
		switch (stage) {
		case 0:
			positive.gpuMethod(x, y, z);
			
			break;
		case 1:
			negative.gpuMethod(x, y, z);

			break;
		case 2:
//			Mapping m = cz.in_zones[0];
//
//			final ILayer layer = m.toZone();
//			
//			float nAct = cz.neurons.get(x, y, z);
//			for (int dx = -1; dx <= 1; dx += 2) {
//				for (int dy = -1; dy <= 1; dy += 2) {
//					for (int dz = -1; dz <= 1; dz += 2) {
//						final int fx = x + dx;
//						final int fy = y + dy;
//						final int fz = z + dz;
//						
//						if (    (fx >= 0 && fx < layer.width()) && 
//								(fy >= 0 && fy < layer.height()) && 
//								(fz >= 0 && fz < layer.depth())
//							) {
//
//							nAct += m.toZone().axons.get(fx, fy, fz) * 0.2f;
//						}
//					}
//				}
//			}
//			
//			cz.neurons.set(nAct, x, y, z);
			
			break;
		case 3:
			if (z != 0)
				return;
			
			pack = cz.neurons.sub(x, y);
			
			if (pack.length() == 1) {
				return;
			}
			
//			pack.debug("pack before");
			WinnerGetsAll._(cz, pack, false);
//			pack.debug("pack after");

//			MatrixProxy<Float> postPack = cz.colPostNeurons.sub(x, y);
			for (int index = 0; index < pack.length(); index++) {
				final float value = pack.getByIndex(index);
				if (value > 0) {
					pack.setByIndex(value, index);
//					postPack.setByIndex(value, index);
				}
			}

			//zero just in case...
			cz.neurons.set(0f, x, y, z);
			
			//set activity equal to winner activity 
			for (int i = 0; i < pack.length(); i++) {
				if (pack.get(i) > 0) {
					cz.neurons.set(pack.get(i), x, y, i);
					break;
				}
			}

			break;

		default:
			break;
		}
	}
	
	public boolean isDone() {
		final Mapping m = cz.in_zones[0];
		
//		if (m instanceof MappingSOM) {
//			switch (stage) {
//			case 0:
//				cz.debugNeurons("after positive");
//				break;
//				
//			case 1:
//				cz.debugNeurons("after negative");
//				break;
//	
//			case 2:
//				cz.debugNeurons("after inhibitory");
//				break;
//			}
//		}
		
		stage++;
		if (stage == 1 && !m.haveInhibitoryWeight()) {
			stage++;
		}
		return stage >= 4;
	}

	@Override
    protected void release() {
		positive.release();
		negative.release();
    }
}