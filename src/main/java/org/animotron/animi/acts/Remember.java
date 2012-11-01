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
package org.animotron.animi.acts;

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;

/**
 * Запоминание
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Remember implements Act<CortexZoneComplex> {
	
	//порог запоминания
	@RuntimeParam(name="mRecLevel")
	public double mRecLevel = 0.8;

    public Remember () {}
    
    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	
    	NeuronComplex cn = layer.col[x][y];

    	NeuronSimple _sn_ = null;
    	
    	//есть ли свободные и есть ли добро на запоминание (интервал запоминания)
    	boolean found = false;
    	for (Link cnLink : cn.s_links) {
    		_sn_ = (NeuronSimple) cnLink.synapse;
    		if (!_sn_.occupy) {
    			found = true;
    			break;
    		}
    	}
    	if (!found) return;
    	
    	//суммируем минусовку с реципторного слоя колоник
    	NeuronSimple sn = null;
    	Link maxLink = null;
    	double maxActive = 0;
    	
		NeuronComplex[][] ms = Subtraction.process(layer, x, y);

    	for (Link cnLink : cn.s_links) {
    		NeuronSimple _sn = (NeuronSimple) cnLink.synapse;
    		
        	double snActive = 0;
    		for (Link snLink : _sn.s_links) {
    			
    			NeuronComplex in = ms[snLink.synapse.x][snLink.synapse.y];
    			
    			snActive += in.minus;
    		}
    		
    		if (snActive > maxActive) {
    			maxActive = snActive;
    			sn = _sn;
    			maxLink = cnLink;
    		}
    	}
    	
		//поверка по порогу
//    	if (activeF > 0 && (active < mRecLevel && sn != null))
    	if (sn == null || maxActive / sn.s_links.size() < mRecLevel) {
			return;
    	}
    	
//    	System.out.println(maxActive / sn.s_links.size());
		
		//перебираем свободные простые нейроны комплексного нейрона
		//сумма сигнала синепсов простых неровнов с минусовки
		//находим максимальный простой нейрон и им запоминаем (от минусовки)
    	
    	//вес синапса ставим по остаточному всечению
    	sn.active = 0;
		for (Link snLink : sn.s_links) {
			
			NeuronComplex in = ms[snLink.synapse.x][snLink.synapse.y];
			snLink.w = in.minus;
			
    		sn.active += in.active * snLink.w;

			//занулить минусовку простого нейрона
//			in.minus = 0;
		}
    	sn.occupy = true;
    	maxLink.addStability( sn.active );
    	
    	if (cn.active == 0)
    		cn.active = sn.active;
    	
    	//присвоить веса сложного нейрона таким образом, чтобы 
    	
    	//текущая активность / на сумму активности (комплекстные нейроны)
//		double active = 0;
//		for (Link cnL : sn.a_links) {
//			
//			active += cnL.axon.active;
//		}
//    	
//		for (Link cnL : sn.a_links) {
//			
//			//UNDERSTAND: перераспределять ли веса?
//			if (active != 0) {
//				cnL.w = cnL.axon.active / active;
//			} else {
//				cnL.w = 1;
//			}
//			cnL.stability = Math.abs( cnL.w );
//		}
    	
    	Restructorization.normalization(cn, sn);
    }
}
