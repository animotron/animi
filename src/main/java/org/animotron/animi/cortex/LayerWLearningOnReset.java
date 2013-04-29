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
package org.animotron.animi.cortex;

import org.animotron.animi.gui.Application;
import org.animotron.matrix.MatrixDelay;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class LayerWLearningOnReset extends LayerWLearning {
	
	public LayerWLearningOnReset(
			String name, Application app, 
			int width, int height, int depth, 
			Mapping[] in_zones, 
			
			Class<? extends Task> classOfActivation,
			Class<? extends Task> classOfInhibitory,
			Class<? extends Task> classOfLearningMatrix,
			Class<? extends Task> classOfLearningMatrixInhibitory,
			Class<? extends Task> classOfLearning,
			
			MatrixDelay.Attenuation attenuation) {

		super(name, app, 
				
				width, height, depth, 
				
				in_zones, 
				
				classOfActivation, 
				classOfInhibitory, 
				classOfLearningMatrix, 
				classOfLearningMatrixInhibitory, 
				classOfLearning, 
				
				attenuation);
	}

    public void process() {
    	if (!isActive()) {
    		return;
    	}

		neurons.step();
    	
    	//Активация колонок (узнавание)
    	performTask(cnActivation);
    	performTask(cnInhibitory);

		axons.step(neurons);
    }

    public void reset() {
    	if (isLearning()) {

        	performTask(learningMatrix);
        	
        	performTask(learningMatrixInhibitory);

        	//Запоминание и переоценка параметров стабильности нейрона
        	performTask(cnLearning);
    	
    		count++;
    	}
		axons.fill(0f);
    }
}
