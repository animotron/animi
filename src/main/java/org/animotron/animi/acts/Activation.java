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
	
	public Activation(CortexZoneComplex cz) {
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

	public void gpuMethod(int x, int y) {
		switch (stage) {
		case 0:
			
			positive.gpuMethod(x, y);
			negative.gpuMethod(x, y);
			
			MatrixProxy<Float> pack = cz.colNeurons.sub(x, y);
			
			WinnerGetsAll._(cz, pack, false);

			MatrixProxy<Float> postPack = cz.colPostNeurons.sub(x, y);
			for (int index = 0; index < pack.length(); index++) {
				if (pack.getByIndex(index) > 0.1) {
					postPack.setByIndex(pack.getByIndex(index), index);
				}
			}

			//zero just in case...
			cz.cols.set(0f, x, y);
			
			//set activity equal to winner activity 
			for (int i = 0; i < cz.package_size; i++) {
				if (pack.get(i) > 0) {
					cz.cols.set(pack.get(i), x, y);
					break;
				}
			}

			break;

		case 1:
			cz.coLearnFactor.set(
				ActivationHebbian.activity(cz.colNeurons, cz.colWeights.sub(x, y)),
				x, y
			);
			
			break;

		default:
			break;
		}
	}
	
	public boolean isDone() {
		if (stage == 1) {
			WinnerGetsAll._(cz, cz.coLearnFactor, false);
		}
		stage++;
		return stage >= 2;
	}

	@Override
    protected void release() {
		positive.release();
		negative.release();
    }
}