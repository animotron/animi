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

import java.util.List;

import javolution.util.FastList;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Q {
	
	NeuronComplex iNeuron;
	NeuronComplex oNeuron;
	
	//** { in , out } **//
	protected List<Link[]> links = new FastList<Link[]>();
	
	public Q(Link in, Link out) {
		this.iNeuron = (NeuronComplex) in.synapse;
		this.oNeuron = (NeuronComplex) out.axon;

		add(in, out);
	}
	
	public void add(Link in, Link out) {
		assert iNeuron == in.synapse;
		assert oNeuron == out.axon;
		
		links.add(new Link[] {in, out});
	}
	
	public double activity() {
		double activity = 0;
		for (Link[] ls : links) {
			activity += ls[0].synapse.activity * ls[0].w * ls[1].w;
		}
		return activity;
	}
}
