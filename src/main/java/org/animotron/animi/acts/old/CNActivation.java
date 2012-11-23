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
package org.animotron.animi.acts.old;

import org.animotron.animi.acts.Act;
import org.animotron.animi.cortex.*;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CNActivation implements Act<CortexZoneSimple> {

	public CNActivation() {}

    @Override
    public void process(CortexZoneSimple layer, final int x, final int y) {
    	NeuronComplex cn = layer.col[x][y];
    	
    	double activity = 0;
    	
    	for (int i = 0; i < 3; i++) {
	    	for (LinkQ q : cn.Qs.values()) {
	    		activity += q.synapse.activity[i] * q.q[i];
	    	}
    	}
    	
    	for (int i = 1; i < 3; i++) {
	    	cn.activity[i] = cn.activity[i-1];
	    	cn.posActivity[i] = cn.posActivity[i-1];
    	}

    	cn.activity[0] = activity;
    	cn.posActivity[0] = activity;
    }
}
