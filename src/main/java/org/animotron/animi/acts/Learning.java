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
	
	public Learning(LayerWLearning cz) {
		super(cz);
		
		positive = new LearningHebbian(cz);
		negative = new LearningAntiHebbian(cz);
	}

	public void prepare() {
		positive.prepare();
		negative.prepare();
	}

	public void gpuMethod(final int x, final int y, final int z) {
		if (cz.cols.get(x, y, z) <= 0) { // && cz.neighborLearning.get(x, y) > 0) {
			return;
		}

//		cz.neighborLearning.fill(1f);
//		cz.neighborLearning.set(0f, x, y);
//		
		positive.gpuMethod(x, y, z);
		negative.gpuMethod(x, y, z);

		//learning for post activity
//		LearningHebbian.learn(
//				cz.colPostNeurons, 
//				cz.colWeights.sub(x, y), 
//				cz.colCorrelation.get(x, y), 
//				0.1f / 5.0f,
//				10^-11);
	}

	public boolean isDone() {
//		float w = 0f;
//		
//		final int each = 5000;
//		if (((cz.count + 1) % each == 0) || (cz.count % each == 0)) {
//			final Mapping m = cz.in_zones[0];
//
//			for (int x = 0; x < cz.width; x++) {
//				for (int y = 0; y < cz.height; y++) {
//					for (int xi = 0; xi < cz.width; xi++) {
//						for (int yi = 0; yi < cz.height; yi++) {
//							for (int p = 0; p < cz.package_size; p++) {
//								w = cz.colWeights.get(x, y, xi, yi, p);
//								if (
//										(x == xi && y == yi && w < 0.001)  || 
//										(x != xi && y != yi && w > 0.05)) {
//				
//									if ((cz.count + 1) % each == 0) {
//										System.out.println("["+x+","+y+"] ["+xi+","+yi+","+p+"] "+w);
//										continue;
//									}
//									MatrixProxy<Float> weight = m.linksWeight.sub(xi, yi, p);
//									for (int index = 0; index < weight.length(); index++) {
//										weight.setByIndex(m.getInitWeight(), index);
//									}
//				
//									weight = m.inhibitoryWeight.sub(xi, yi, p);
//									for (int index = 0; index < weight.length(); index++) {
//										weight.setByIndex(m.getInitWeight(), index);
//									}
//									
//									
//									for (int xj = 0; xj < cz.width; xj++) {
//										for (int yj = 0; yj < cz.height; yj++) {
//											cz.colWeights.set(cz.getWeight(), xj, yj, xi, yi, p);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}

		return true;
	}

	@Override
    protected void release() {
    }
}