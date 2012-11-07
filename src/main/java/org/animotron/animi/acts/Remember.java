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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;

/**
 * Запоминание
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Remember implements Act<CortexZoneComplex> {
	
	//порог запоминания
	@RuntimeParam(name="mRecLevel")
	public double mRecLevel = 0.1;

    public Remember () {}
    
    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	
    	NeuronComplex cn = layer.col[x][y];
    	
    	if (!cn.isOccupy()) {
    		
    		double sumA2 = 0;
    		double activity = 0;

    		for (LinkQ link : cn.Qs.values()) {
    			activity += link.synapse.activity;
    			
    			sumA2 += link.synapse.activity * link.synapse.activity;
    		}

    		System.out.println(activity / cn.Qs.values().size());
    		if ((activity / cn.Qs.values().size()) < mRecLevel)
    			return;
    		
    		double sumQ = 0;
    		for (LinkQ link : cn.Qs.values()) {
    			link.q = link.synapse.activity / sumA2;
    			sumQ += link.q;
    		}
    		cn.occupy = true;
    		cn.sumQ = sumQ;
    	}
//    	Restructorization.normalization(cn, sn);
    }
}
