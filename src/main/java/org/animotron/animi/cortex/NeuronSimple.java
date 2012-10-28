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

import org.animotron.animi.RuntimeParam;


/**
 * Simple neuron
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class NeuronSimple {
	
	@RuntimeParam(name="active")
	public double active = 0;

	@RuntimeParam(name="occupy")
	public boolean occupy = false;
	
	/** Links of synapses connects cortex neurons with projecting nerve bundle **/
//	@RuntimeParam(name="")
	/** incoming links **/
	public Link2dZone[] s_links;
	
	/** Axonal connections with nearest cortical columns **/
//	@RuntimeParam(name="")
	/** outgoing links **/
	public Link2d[] a_links;
	
	/** Counter for links of synapses **/
//	@RuntimeParam(name="")
	public int n1 = 0;
	
	/** Counter for axonal connections **/
//	@RuntimeParam(name="")
	public int n2 = 0;
}