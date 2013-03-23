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

import org.animotron.animi.Params;
import org.animotron.animi.cortex.*;
import org.animotron.matrix.MatrixProxy;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Activation extends Task {
	
//	@Params
	private ActivationHebbian positive;
	@Params
	private ActivationAntiHebbian negative;
	
	public Activation(LayerWLearning cz) {
		super(cz);
		
		positive = new ActivationHebbian(cz);
		negative = new ActivationAntiHebbian(cz);
	}
	
	private int stage = 0;
	
	@Override
	public void prepare() {
		positive.prepare();
		negative.prepare();
		
		stage = 0;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		switch (stage) {
		case 0:
			positive.gpuMethod(x, y, z);
			
			break;
		case 1:
			negative.gpuMethod(x, y, z);

			break;
		case 2:
			if (z != 0)
				return;
			
			MatrixProxy<Float> pack = cz.neurons.sub(x, y);
			
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

//		case 1:
//			cz.colCorrelation.set(
//				ActivationHebbian.activity(cz.colNeurons, cz.colWeights.sub(x, y)),
//				x, y
//			);
//			break;

		default:
			break;
		}
	}
	
	public boolean isDone() {
//		switch (stage) {
//		case 0:
//			cz.debug("after positive");
//			break;
//			
//		case 1:
//			cz.debug("after negative");
//			break;
//
//		case 2:
//			cz.debug("after inhibitory");
//			break;
//		}

		stage++;
		return stage >= 3;
	}

	@Override
    protected void release() {
		positive.release();
		negative.release();
    }
}