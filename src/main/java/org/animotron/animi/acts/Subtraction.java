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
public class Subtraction implements Act<CortexZoneComplex> {

	public Subtraction() {}

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	
    	NeuronComplex cn = layer.col[x][y];
    	if (cn.active > 0) {
    		
    		for (int j = 0; j < cn.s_links.length; j++) {
    			Link3d scn = cn.s_links[j];
    			
    			NeuronSimple sn = layer.s[scn.x][scn.y][scn.z];
    			
    			for (int i = 0; i < sn.n1; i++) {
    				Link2dZone ssn = sn.s_links[i];
    				
        			double q = 0;
    	    		for (int k = 0; k < cn.s_links.length; k++) {
    	    			q += ssn.w * cn.s_links[k].w;
    	    		}
    				
    				NeuronComplex col = ssn.zone.col[ssn.x][ssn.y];
    				
//    				cn.putQ(col, q);
    				
    				double delta = cn.active / q;
    				
    				col.backProjection += delta;
    				col.minus -= delta;
    			}
    		}
    	}
    }
}
