/*
 *  Copyright (C) 2012-2013 The Animo Project
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

import javolution.util.FastList;

import org.animotron.animi.RuntimeParam;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Neuron {
	
	@RuntimeParam(name="activity")
	public double[] activity = new double[] {0, 0, 0, 0, 0, 0, 0};
	
	public int x;
	
	public int y;

	/** Links of synapses connects cortex neurons with projecting nerve bundle **/
	/** incoming links **/
	public FastList<Link> s_links = new FastList<Link>();
	
	/** Axonal connections with nearest cortical columns **/
	/** outgoing links **/
	public FastList<Link> a_links = new FastList<Link>();
	
	public FastList<Link> s_inhibitoryLinks = new FastList<Link>();

	public Neuron(int x, int y) {
		this.x = x;
		this.y = y;
	}

	//called by Link only!
	protected void addSynapse(Link link, LinkType type) {
		if (type == LinkType.NORMAL)
			s_links.add(link);
		else
			s_inhibitoryLinks.add(link);
	}

	//called by Link only!
	protected void addAxon(Link link, LinkType type) {
		a_links.add(link);
	}
}
