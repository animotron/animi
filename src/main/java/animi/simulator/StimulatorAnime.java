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
package animi.simulator;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import animi.Params;
import animi.cortex.IRetina;
import animi.gui.Application;
import animi.simulator.figures.Figure;
import animi.simulator.figures.RotateAnime;
import animi.tuning.Codes;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class StimulatorAnime extends AbstractStimulator {
	
    @Params
    public Figure[] figures;
    
    Random rnd = new Random();
    
    public StimulatorAnime(Application application) {
    	super(application);
	}

	public void init() {
		IRetina retina = app.contr.getRetina();
		
		int width = retina.worldWidth();
		int height = retina.worldHeight();
		
        img = new BufferedImage(
        		width, 
        		height,
        		BufferedImage.TYPE_INT_RGB);
		
        figures = new Figure[1];
        figures[0] = new RotateAnime(img.getWidth(), img.getHeight(), retina.worldStep(), true);
	}
	
	int count = 0;
	int changeAt = Codes.SHIFTIMES - 1;//3;
	boolean resetStage = true;
	Figure active = null;
	
	public boolean isReset() {
		return resetStage;
	}

	public void step() {
		if (active == null || active.step()) {
			
			if (active == null || count >= changeAt) {
				do {
					active = figures[rnd.nextInt(figures.length)];
					//XXX: protection?
				} while (!active.isActive());
				
				active.reset();
				
				resetStage = true;
				count = 0;
			} else {
				resetStage = true;
				count++;
			}
		} else {
			resetStage = false;
		}

        Graphics g = img.getGraphics();
        g.clearRect(0, 0, img.getWidth(), img.getHeight());
        
		active.drawImage(g);
	}

	public BufferedImage getNextImage() {
		step();
		
		return getImage();
	}
	
	public int getCode() {
		return active.getCode();
	}
}
