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
package org.animotron.animi.cortex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class AttentionZone extends CortexZoneComplex {
	
	public AttentionZone(String name, MultiCortex mc, int width, int height, Mapping[] in_zones) {
		super(name, mc, width, height, in_zones);
	}
	
    public void process() {
    	if (!isActive())
    		return;
    	
    	cycleActivation();
    	
    	//inhibitory
    	int X = -1, Y = -1;
    	double max = Double.MIN_VALUE;
    	
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
            	NeuronComplex cn = col[x][y];
            	if (max < cn.activity) {
            		max = cn.activity;
            		X = x;
            		Y = y;
            	}
            }
        }
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
            	NeuronComplex cn = col[x][y];
            	if (!(X == x && Y == y)) {
            		cn.activity = 0;
            		cn.posActivity = 0;
            		cn.backProjection = 0;
            	}
            }
        }

        X = (int)(X * in_zones[0].zone.width / (double) width());
		Y = (int)(Y * in_zones[0].zone.height / (double) height());
		
		mc.retina.shift(X, Y);
    }

}
