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
public class Subtraction { //implements Act<CortexZoneComplex> {

	public Subtraction() {}

//    @Override
    public static NeuronComplex[][] process(CortexZoneComplex layer, final int x, final int y) {
    	
    	CortexZoneSimple zone = layer.in_zones[0].zone;
    	NeuronComplex[][] ms = new NeuronComplex[zone.width][zone.height];
    	for (int _x = 0; _x < zone.width; _x++) {
        	for (int _y = 0; _y < zone.height; _y++) {
        		ms[_x][_y] = new NeuronComplex(zone.col[_x][_y]);
        	}
    	}
    	
    	NeuronComplex cn = layer.col[x][y];
    	if (cn.active > 0) {

    		double Q = 0;
    		for (Link cnLink : cn.s_links) {
    			NeuronSimple sn = (NeuronSimple) cnLink.synapse;
    			
    			for (Link ssn : sn.s_links) {
    				
        			double q = 0;
    	    		for (Link link : cn.s_links) {
    	    			q += ssn.w * link.w;
    	    		}
    				
    				ms[ssn.synapse.x][ssn.synapse.y].q += q;
    				
    				Q += q;
    			}
    		}
			//XXX: store to reuse //cn.putQ(col, q);
			if (Q == 0) {
				System.out.println("WARNING q == 0");
				return ms;
			}
    				
    		for (Link cnLink : cn.s_links) {
    			NeuronSimple sn = (NeuronSimple) cnLink.synapse;
    			
    			for (Link ssn : sn.s_links) {
    				
    				NeuronComplex col = ms[ssn.synapse.x][ssn.synapse.y];
    				
					double delta = cn.active * col.q / Q;
    				
    				col.backProjection += delta;
    				col.minus -= delta;
    				if (col.minus < 0) col.minus = 0;
    			}
    		}
    	}
    	return ms;
    }
}
