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

import java.util.Random;

import org.animotron.animi.cortex.*;
import org.animotron.matrix.*;

/**
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningTest extends LearningHebbian {
	
	public float speed = 0.75f;
	
	public int eachCount = 500;

	private Random rnd = new Random();
	
	public LearningTest(LayerWLearning cz) {
		super(cz);
	}

	private int count = 0;

	public void prepare() {
		count++;
	}

	public void gpuMethod(final int x, final int y, final int z) {
		super.gpuMethod(x, y, z);
		
		if (true)
			return;
		
		if (count % eachCount == 0) {
			System.out.println("===== ["+x+","+y+","+z+"]");
			
			final Mapping m = cz.in_zones[0];
			
			Matrix<Float> posW = m.senapseWeight().sub(x, y, z);
			
			MatrixProxy<Integer[]> sen = m.senapses().sub(x, y, z);
			
			final Mapping _m = ((LayerWLearning)m.frZone()).in_zones[0];

			for (int index = 0; index < posW.length(); index++) {
				if (posW.getByIndex(index) > 0.15f) {
					Integer[] pos = sen.getByIndex(index);

					System.out.print("["+pos[0]+","+pos[1]+","+pos[2]+"] "+posW.getByIndex(index));

					int dx = pos[0] - x;
					int dy = pos[1] - y;
//					int dz = pos[2] - z;
					
					if ((dx >= -1 && dx <= 1) && (dy >= -1 && dy <= 1)) {
						System.out.println(" !");
						continue;
					}
					System.out.println(" .");
					
//					posW.setByIndex(0f, index);
					
					Matrix<Float> _posW = _m.senapseWeight().sub(pos[0], pos[1], pos[2]);
//					Matrix<Float> _negW = _m.inhibitoryWeight().sub(pos[0], pos[1], pos[2]);
					
					float posAvg = 0f;
//					float negAvg = 0f;
					for (int i = 0; i < _posW.length(); i++) {
						posAvg += _posW.getByIndex(i);
//						negAvg += _negW.getByIndex(i);
						
					}
					posAvg = posAvg / (float)_posW.length();
//					negAvg = negAvg / (float)_negW.length();
					
					float posSumQ2 = 0;
//					float negSumQ2 = 0;
					for (int i = 0; i < _posW.length(); i++) {
						
//						final float w = rnd.nextFloat();
//						
//						_posW.setByIndex(w, i);
//						_negW.setByIndex(0f, i);
						
//						posSumQ2 = w * w;
						
						final float pW = _posW.getByIndex(i);
//						final float nW = _negW.getByIndex(i);
//						if (nW > 0f) {
//							final float neW = nW + (negAvg - nW) * speed;
//							
//							if (neW > 0f) {
//								_negW.setByIndex(neW, i);
//								
//								negSumQ2 += neW * neW;
//							} else {
//								_negW.setByIndex(0f, i);
//							}
//						} else {
							final float neW = pW + (posAvg - pW) * speed;
							
							if (neW > 0f) {
								_posW.setByIndex(neW, i);
								
								posSumQ2 += neW * neW;
							} else {
								_posW.setByIndex(0f, i);
							}
//						}
					}
					
					Mth.normalization(_posW, posSumQ2);
//					Mth.normalization(_negW, negSumQ2);
				}
			}
		}
	}
}