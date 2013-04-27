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
package org.animotron.animi.tuning;

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.acts.WinnerGetsAll;
import org.animotron.animi.cortex.*;
import org.animotron.matrix.Matrix;
import org.animotron.matrix.MatrixFloat;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class InhibitoryLearningMatrix extends Task {

	@RuntimeParam(name = "k")
	public float k = 0.3f;
	
	@RuntimeParam(name = "minDelta")
	public float minDelta = 0.01f;
	
	public InhibitoryLearningMatrix(LayerWLearning cz) {
		super(cz);
	}

//	private float w = 0f;
//	private float maxDelta = 0f;
//	private Matrix<Float> matrix = null;
	
	@Override
	public void prepare() {
//		matrix = cz.learning.copy();
//		w = 1 / matrix.length();
//		maxDelta = 0f;
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		if (x == 0 && y == 0 && z == 0) {
			cz.toLearning = (MatrixFloat) cz.learning.copy();
			_(cz.in_zones[0], cz.toLearning);
		}
//		
//		
//		float delta = 0;
//		for (int xi = 0; xi < cz.width; xi++) {
//			for (int yi = 0; yi < cz.height; yi++) {
//				for (int zi = 0; zi < cz.depth; zi++) {
//				
//					if (xi != x && yi != y && zi != z) {
//						delta += w * matrix.get(xi, yi, zi);
//					}
//				}
//			}
//		}
//		
//		float activity = matrix.get(x, y, z);
//		activity -= delta * k;
//		if (activity < 0f) {
//			activity = 0f;
//			delta = 0f;
//		}
//		
//		cz.learning.set(activity, x, y, z);
//		
//		synchronized (this) {
//			maxDelta = Math.max(maxDelta, delta);
//		}
	}
	
	@Override
	public boolean isDone() {
		return true;
//		boolean isDone = maxDelta < minDelta;
//		if (!isDone) {
//			maxDelta = 0f;
//			matrix = cz.learning.copy();
//		}
//		
//		return isDone;
	}
	
	@Override
	protected void release() {
	}
	
	public static void _(final Mapping m, final Matrix<Float> source) {
		
		final Matrix<Float> cols = source.copy();
		
		int count = 0;
    	
    	//max is winner & winner gets all
    	int[] maxPos;
    	float max = 0;
    	
    	source.fill(0f);

    	maxPos = cols.max();
    	final float half = cols.get(maxPos) / 5f;

    	while (count <= 0) {
	    	maxPos = cols.max();
	        if (maxPos == null) {
	        	break;
	        }
	        
	    	max = cols.get(maxPos);
	    	if (half >= max)
	    		return;
	    	
	    	if (m.senapsesCode().get(maxPos[0],maxPos[1],maxPos[2]) < 0f) {
	        	source.set(max, maxPos);
	        	count++;
//        		return;
	        }// else {
//	        	cols.set(0f, maxPos);
//	        }
    		return;
    	}
	}
}
