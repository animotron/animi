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
public class Learning extends Task {
	
	@Params
	private LearningHebbian positive;
	@Params
	private LearningAntiHebbian negative;
	
	public Learning(CortexZoneComplex cz) {
		super(cz);
		
		positive = new LearningHebbian(cz);
		negative = new LearningAntiHebbian(cz);
	}

	public void prepare() {
		positive.prepare();
		negative.prepare();
	}

	public void gpuMethod(int x, int y) {
		if (cz.coLearnFactor.get(x, y) <= 0 && cz.neighborLearning.get(x, y) > 0) {
			return;
		}

		cz.neighborLearning.fill(1f);
		cz.neighborLearning.set(0f, x, y);
		
		positive.gpuMethod(x, y);
		negative.gpuMethod(x, y);

		LearningHebbian.learn(
				cz.colPostNeurons, 
				cz.colWeights.sub(x, y), 
				cz.coLearnFactor.get(x, y), 
				0.01f,
				0.00001f);
	}

	@Override
    protected void release() {
    }
}