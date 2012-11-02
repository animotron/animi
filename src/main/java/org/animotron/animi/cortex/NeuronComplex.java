/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
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

import java.util.Map;

import javolution.util.FastMap;

import org.animotron.animi.RuntimeParam;

/**
 * Complex neuron
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class NeuronComplex extends Neuron {
	
	@RuntimeParam(name="backProjection")
	public double backProjection = 0;

	@RuntimeParam(name="minus")
	public double minus = 0;

	public double q = 0;

	public Map<NeuronComplex, Q> Qs = new FastMap<NeuronComplex, Q>();
	
	public NeuronComplex(int x, int y) {
		super(x,y);
	}

	public NeuronComplex(NeuronComplex cn) {
		super(cn.x, cn.y);
		activity = cn.activity;

		s_links = cn.s_links;
		a_links = cn.a_links;

		backProjection = cn.backProjection;
		minus = cn.minus;
		q = cn.q;
	}

	public void init() {
		//collect Q
		for (Link cnLink : s_links) {
			for (Link snLink : cnLink.synapse.s_links) {
				NeuronComplex key = (NeuronComplex) snLink.synapse;
				Q q = Qs.get(key);
				if (q == null) {
					q = new Q(snLink, cnLink);
					Qs.put(key, q);
				} else {
					q.add(snLink, cnLink);
				}
			}
		}
	}
}