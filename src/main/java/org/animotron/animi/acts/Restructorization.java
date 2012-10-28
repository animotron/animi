/*
 *  Copyright (C) 2012 The Animo Project
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

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Restructorization implements Act<CortexZoneComplex> {

	public Restructorization() {}

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	
    	for (int z = 0; z < layer.deep; z++) {
    		NeuronSimple sn = layer.s[x][y][z];
    		if (sn.active > 0) {
    			for (int i = 0; i < sn.n2; i++) {
    				Link2d link = sn.a_links[i];
    				
    				if (link.w > 0) {
	    				NeuronComplex cn = layer.col[link.x][link.y];
	    				
	    				double sum = 0, delta = 0;

        		    	for (int j = 0; j < cn.s_links.length; j++) {
	        				Link3d l = cn.s_links[j];
	        				
	        				if (l.x == x && l.y == y && l.z == z)
	    	    				delta = cn.active * sn.active / l.stability;
	        				else
	        					sum += l.stability;
	        			}
        		    	
        		    	for (int j = 0; j < cn.s_links.length; j++) {
	        				Link3d l = cn.s_links[j];
	        				
	        				if (l.x == x && l.y == y && l.z == z)
	    	    				l.w += delta;
	        				else {
	        					l.w -= delta * l.stability / sum;
	        					if (l.w < 0) l.w = 0;
	        				}
	        			}
    				}
    			}
    		}
    	}
    }
}