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
public class LinkQ {
	
	public NeuronComplex senapse;
	public NeuronComplex axon;
	
	//** { in , out } **//
	protected List<Link[]> links = new FastList<Link[]>();
	
	public LinkQ(Link in, Link out) {
		this.senapse = (NeuronComplex) in.synapse;
		this.axon = (NeuronComplex) out.axon;

		add(in, out);
	}
	
	public void add(Link in, Link out) {
		assert senapse == in.synapse;
		assert axon == out.axon;
		
		links.add(new Link[] {in, out});
	}
	
	double q = 0;
	public double q() {
//		for (Link[] ls : links) {
//			q += ls[0].w * ls[1].w;
//		}
		return q;
	}
}
