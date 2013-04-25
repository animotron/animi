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
import java.util.Random;

import org.animotron.animi.Params;
import org.animotron.animi.cortex.IRetina;
import org.animotron.animi.gui.Application;
import org.animotron.animi.simulator.figures.Figure;
import org.animotron.animi.simulator.figures.RotateAnime;

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
		IRetina retina = app.cortexs.retina;
		
		int width = retina.worldWidth();
		int height = retina.worldHeight();
		
        img = new BufferedImage(
        		width, 
        		height,
        		BufferedImage.TYPE_INT_RGB);
		
        figures = new Figure[1];
        
        int f = 0;

//		int step = 56;
//        int offset = retina.worldStep() * step / 8;
//
//        int j = 0;
//        int[][] steps = new int[step][];
//
//        for (int k = 0; k < 2; k++) {
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {-retina.worldStep(), 0};
//	        }
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {+retina.worldStep(), 0};
//	        }
//        }
//        
//        Point first  = new Point((int)(width * 0 - offset), (int)(height * 0 - offset * 2));
//        Point second = new Point((int)(width * 1 + offset), (int)(height * 1 + offset * 0));
//        Point third  = new Point(second.x + width * 2, second.y);
//        Point fourth = new Point(first.x, first.y + height * 2);
//        		
//    	figures[f++] = new RectAnime(
//	        new Point2D[] {first, second, third, fourth, first},
//    	    true,
//    	    steps,
//			true
//    	);

//    	j = 0;
//        steps = new int[step][];
//
//        for (int k = 0; k < 2; k++) {
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {-retina.worldStep(), 0};
//	        }
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {+retina.worldStep(), 0};
//	        }
//        }
//        
//        first  = new Point((int)(width * 0 - offset), (int)(height * 1 + offset * 2));
//        second = new Point((int)(width * 1 + offset), (int)(height * 0 - offset * 0));
//        third  = new Point(second.x + width * 2, second.y);
//        fourth = new Point(first.x, first.y + height * 2);
//        		
//    	figures[f++] = new RectAnime(
//	        new Point2D[] {first, second, third, fourth, first},
//    	    true,
//    	    steps,
//			true
//    	);
//
//    	j = 0;
//    	steps = new int[step][];
//
//        for (int k = 0; k < 2; k++) {
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {-retina.worldStep(), 0};
//	        }
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {+retina.worldStep(), 0};
//	        }
//        }
//
//    	figures[f++] = new RectAnime(
//			(int)(img.getWidth() * 0.5) + retina.worldStep() * step / 8, (int)(img.getHeight() * -0.2),
//			(int)(img.getWidth() * 2), (int)(img.getHeight() *  2),
//    	    true,
//    	    null,//steps,
//			true
//    	);
//
//    	j = 0;
//        steps = new int[step][];
//        
//    	for (int k = 0; k < 2; k++) {
//	    	for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {0, -retina.worldStep()};
//	        }
//	        for (int i = 0; i < step/4; i++) {
//	        	steps[j++] = new int[] {0, +retina.worldStep()};
//	        }
//    	}
//
//        figures[f++] = new RectAnime(
//    			(int)(img.getWidth() * -0.5), (int)(img.getHeight() * 0.5) + retina.worldStep() * step / 8,
//    			(int)(img.getWidth() *  2), (int)(img.getHeight() * 2),
//	    	    true,
//	    	    steps,
//    			true
//	    	);

        figures[f++] = new RotateAnime(img.getWidth(), img.getHeight(), retina.worldStep(), true);
	}
	
	int count = 0;
	int changeAt = 3;
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
}
