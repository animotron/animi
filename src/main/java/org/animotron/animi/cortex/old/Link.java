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
package org.animotron.animi.cortex.old;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Link {
	
	//Dendrite terminal
	public Neuron synapse;
	
	//Axon terminal
	public Neuron axon;

	public double[] w = new double[] {0, 0, 0};
	
	public double stability = 0;

	protected Link(Neuron synapse, Neuron axon, LinkType type) {
		this.synapse = synapse;
		synapse.addAxon(this, type);

		this.axon = axon;
		axon.addSynapse(this, type);
	}
	
	protected Link(Neuron synapse, Neuron axon, double w, LinkType type) {
		this(synapse, axon, type);
		
		this.w[0] = w/(double)3;
		this.w[1] = w/(double)3;
		this.w[2] = w/(double)3;
	}

	public void addStability(double x) {
//		System.out.println(x);
		stability += Math.abs(x);
	}
}
