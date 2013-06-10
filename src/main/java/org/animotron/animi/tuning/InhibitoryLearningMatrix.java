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

	@Override
	public void prepare() {
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		if (x == 0 && y == 0 && z == 0) {
			cz.toLearning = (MatrixFloat) cz.learning.copy();
			_(cz.in_zones[0], cz.toLearning);
		}
	}
	
	@Override
	public boolean isDone() {
		//cz.toLearning.debug("InhibitoryLearningMatrix");
		return true;
	}
	
	@Override
	protected void release() {
	}
	
	public void _(final Mapping m, final Matrix<Float> source) {
		
		final Matrix<Float> cols = source.copy();
		
    	//max is winner & winner gets all
    	int[] maxPos;
    	float max = 0;
    	
    	source.fill(0f);

        final int linksNumber = cz.inhibitory_number_of_links;

        while (true) {
	    	maxPos = cols.max();
	        if (maxPos == null) {
	        	break;
	        }
	        
	    	max = cols.get(maxPos);

    		for (int l = 0; l < linksNumber; l++) {
    			try {
	    	    	final int xi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 0);
	    	    	final int yi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 1);
	    	        
	    	    	for (int zi = 0; zi < cz.depth; zi++) {
	    	    		cols.set(0f, xi, yi, zi);
	    	    	}
    			} catch (Exception e) {
    				e.printStackTrace();

//    				final int xi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 0);
//	    	    	final int yi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 1);
    			}
    		}
	    	
        	source.set(max, maxPos);
        	cols.set(0f, maxPos);
    	}
	}
}
