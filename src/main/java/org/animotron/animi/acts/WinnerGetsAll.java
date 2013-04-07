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

import org.animotron.animi.cortex.*;
import org.animotron.matrix.Matrix;

/**
 * Winner gets all
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class WinnerGetsAll extends Task {

	public WinnerGetsAll(LayerWLearning cz) {
		super(cz);
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		if (x == 0 && y == 0 && z == 0) {
			_(cz, cz.neurons, !cz.isSingleReceptionField());
		}
	}
	
	public static void _(LayerWLearning cz, Matrix<Float> source, boolean inhibitory) {
		
		Matrix<Float> cols = source.copy();
    	
    	//max is winner & winner gets all
    	int[] maxPos;
    	float max = 0;

        final int linksNumber = cz.inhibitory_number_of_links;
    	
    	while (true) {
	    	maxPos = cols.max();
	        if (maxPos == null) {
	        	break;
	        }
	        
	    	max = cols.get(maxPos);
	    	
//	    	System.out.println(maxPos);
	    	
	        if (!inhibitory) {
	        	source.fill(0f);

	        	source.set(max, maxPos);
//        		sz.rememberCols[maxPos] = max;
        		return;
	        }
	        
    		for (int l = 0; l < linksNumber; l++) {
    			try {
	    	    	final int xi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 0);
	    	    	final int yi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 1);
	    	        
	    	    	for (int zi = 0; zi < cz.depth; zi++) {
	    	    		cols.set(0f, xi, yi, zi);
	    	    	}
    			} catch (Exception e) {
    				e.printStackTrace();

    				final int xi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 0);
	    	    	final int yi = cz.inhibitoryLinksSenapse(maxPos[0], maxPos[1], l, 1);
    			}
    		}
    		cols.set(0f, maxPos);
    	}
	}
	
	@Override
	protected void release() {
	}
}