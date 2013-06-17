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
package animi.charts;

import static animi.matrix.MatrixDelay.noAttenuation;
import static animi.matrix.MatrixDelay.oneStepAttenuation;
import animi.acts.*;
import animi.cortex.*;
import animi.gui.Application;
import animi.gui.Controller;
import animi.tuning.LearningMatrix;

import static animi.tuning.Codes.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class SimilarityChart extends Controller {
	
	public ChartFrame panel;
	
    public IRetina retina;
    LayerSimple z_in;
    LayerWLearning layer;
	
    int completeTurn;

    public SimilarityChart(Application app) {
    	super(app);
    	
    	panel = new ChartFrame();
    	
    	completeTurn = SHIFTIMES * SHIFTS * CODES;

    	z_in = new LayerSimple("Зрительный нерв", app, 60, 60, 1,
			oneStepAttenuation
        );
    	z_in.isZeroAvgAxons = false;

    	layer = new LayerWLearning("similarity", app, SHIFTIMES * SHIFTS, CODES, 1,
            new Mapping[]{
                new MappingHebbian(z_in, 150, 1, true, false)
            },
            ActivationMethodOfTwo.class,//ActivationMemory.class,
            null,
            LearningMatrix.class,
            null,
            LearningAsItis.class,//LearningMemory.class,
            noAttenuation
        );
    	layer.singleReceptionField = true;

    	zones = new LayerSimple[]{z_in, layer};
        
        setRetina(null);
    }

	@Override
	public void setRetina(IRetina retina) {
		if (retina == null)
			this.retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
		else
			this.retina = retina;
		
        this.retina.setNextLayer(z_in);
        this.retina.addResetLayer(layer);
	}

	@Override
	public IRetina getRetina() {
		return retina;
	}
	
	@Override
	public void process() {
    	retina.process(app.getStimulator());
    	
		for (LayerSimple zone : zones) {
			zone.process();
		}
		
		if (app.contr.count > completeTurn) {
			//final int stimulCode = app.getStimulator().getCode();
			
			for (int code = 0; code < CODES; code++) {
				
				for (int shift = 0; shift < SHIFTS; shift++) {
					final float act = layer.axons.get(shift, code, 0);
					
					panel.values[code][shift] = act;
				}
			}
			layer.axons.fill(0f);
			panel.updateSurface();
			System.out.println("measure!");
		}
	}
}