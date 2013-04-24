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
public class MassSuicide extends LearningHebbian {
	
	public int eachCount = 500;
	
	Matrix<Float> harakiri;

	final Mapping m;

	public MassSuicide(LayerWLearning cz) {
		super(cz);

		LayerSimple layer = cz.in_zones[0].frZone();
		harakiri = new MatrixFloat(layer.width, layer.height, layer.depth);

		m = cz.in_zones[0];
	}

	private int count = 0;

	public void prepare() {
		count++;
		
		harakiri.fill(Float.MAX_VALUE);
	}

	public void gpuMethod(final int x, final int y, final int z) {
		super.gpuMethod(x, y, z);
		
		if (count % eachCount == 0) {
			System.out.println("===== ["+x+","+y+","+z+"]");
			
			Matrix<Float> posW = m.senapseWeight().sub(x, y, z);
			
			MatrixProxy<Integer[]> sen = m.senapses().sub(x, y, z);
			
			for (int index = 0; index < posW.length(); index++) {
				if (posW.getByIndex(index) > 0.15f) {
					Integer[] pos = sen.getByIndex(index);

//					System.out.print("["+pos[0]+","+pos[1]+","+pos[2]+"] "+posW.getByIndex(index));

					int dx = pos[0] - x;
					int dy = pos[1] - y;
//					int dz = pos[2] - z;
					
					final float length = (float)Math.sqrt(dx*dx + dy*dy);
					final float value = harakiri.get(pos[0], pos[1], pos[2]);
					if (value > length) {
						harakiri.set(length, pos[0], pos[1], pos[2]);
					}
				}
			}
		}
	}
	
	Random rnd = new Random();
	
	public boolean isDone() {
		if (count % eachCount == 0) {
			LayerWLearning layer = (LayerWLearning) m.frZone();
			
			final Mapping m = layer.in_zones[0];
			
	
			for (int x = 0; x < layer.width; x++) {
				for (int y = 0; y < layer.height; y++) {
					for (int z = 0; z < layer.depth; z++) {
						
						if (harakiri.get(x,y,z) > 3f) {
							Matrix<Float> posW = m.senapseWeight().sub(x,y,z);
							float sumQ2 = 0f;
							
							for (int index = 0; index < posW.length(); index++) {
								final float w = rnd.nextFloat();
								
								posW.setByIndex(w, index);
	
								sumQ2 += w * w;
							}
							
							Mth.normalization(posW, sumQ2);
						}
					}
				}
			}
		}

		return true;
	}
}