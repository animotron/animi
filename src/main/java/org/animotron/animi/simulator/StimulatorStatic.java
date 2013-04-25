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
package org.animotron.animi.simulator;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.animotron.animi.Params;
import org.animotron.animi.cortex.IRetina;
import org.animotron.animi.gui.Application;
import org.animotron.animi.simulator.figures.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class StimulatorStatic extends AbstractStimulator {
	
    @Params
    public Figure[] figures;
    
    public StimulatorStatic(Application application) {
    	super(application);
	}

	public void init() {
		final IRetina retina = app.cortexs.retina;
		
        figures = new Figure[] {
//        	new OvalAnime(15, mc.retina.width(), mc.retina.height()),
        	new RectAnime(15, retina.width(), retina.height(), true)
//        	,
//        	new Triangle(15, mc.retina.width(), mc.retina.height())
        };
        
        //prepare first image
        getNextImage();
	}
    
	public void reset() {
		for (Figure figure : figures) {
			figure.reset();
        };

        //prepare first image
        getNextImage();
	}

	@Override
	public BufferedImage getNextImage() {
		final IRetina retina = app.cortexs.retina;

		img = new BufferedImage(retina.worldWidth(), retina.worldHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();

        for (Figure i : figures) {
    		i.drawImage(g);
        }
        return img;
	}

	@Override
	public boolean isReset() {
		// TODO Auto-generated method stub
		return false;
	}
}
