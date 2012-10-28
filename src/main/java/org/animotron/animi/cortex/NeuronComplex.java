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

import java.util.HashMap;
import java.util.Map;

import org.animotron.animi.RuntimeParam;

/**
 * Complex neuron
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class NeuronComplex {
	
	@RuntimeParam(name="active")
	public double active = 0;
	
	@RuntimeParam(name="backProjection")
	public double backProjection = 0;

	@RuntimeParam(name="minus")
	public double minus = 0;
	
	//	/** Number of active neurons **/
//	@RuntimeParam(name="Number of active neurons")
//    public double sum;

	/** Links of synapses connects cortex neurons with neurons of cortical columns **/
//	@RuntimeParam(name="")
    public Link3d[] s_links;
    
	public void clean() {
		active = 0;
	}
}