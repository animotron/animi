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
public class LinkQ {
	
	public NeuronComplex synapse;
	public NeuronComplex axon;
	
	public double fX = 1;
	public double fY = 1;
	
	public int delay = 0;
	
	public LinkQ(NeuronComplex synapse, NeuronComplex axon, double q, double fX, double fY, double speed) {
		this.synapse = synapse;
		this.axon = axon;
		
		synapse.a_Qs.add(this);
		axon.Qs.put(synapse, this);
		
		this.q = q;
//		this.q[0] = q / (double)3;
//		this.q[1] = q / (double)3;
//		this.q[2] = q / (double)3;
		
		this.fX = fX;
		this.fY = fY;
		
		double lX = synapse.x - (axon.x * fX);
		double lY = synapse.y - (axon.y * fX);
		
		double length = Math.sqrt(lX*lX + lY*lY);
		
		delay = (int)Math.round(length / speed);
	}
	
	public double q = 0;
}
