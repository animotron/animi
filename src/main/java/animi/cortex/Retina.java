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
package animi.cortex;

import animi.simulator.Stimulator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: macula lutea
public class Retina implements IRetina {

	//Сетчатка
//  public static final int WIDTH = 192;
//	public static final int HEIGHT = 144;
//	public static final int WIDTH = 160;
//	public static final int HEIGHT = 120;
	public static final int WIDTH = 120;
	public static final int HEIGHT = 120;
	
	/* x */
	private int width;
	/* y */
	private int height;
	
	RetinaZone retinaTask = null;
	
	public Retina() {
		this(WIDTH, HEIGHT);
	}

	public Retina(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	private LayerSimple NL = null;
	@Override
	public void setNextLayer(LayerSimple sz) {
		NL = sz;
		
		width = sz.width();
		height = sz.height();
	}
	
	List<LayerWLearning> resetLayers = new ArrayList<LayerWLearning>();
	@Override
	public void addResetLayer(LayerWLearning layer) {
		resetLayers.add(layer);
	}
	
	@Override
	public void init(Stimulator stimulator) {
        retinaTask = new RetinaZone(this, (LayerSimple)NL);
        
        for (int i = 0; i < 5; i++) {
        	retinaTask.setInput(image = stimulator.getNextImage());
        }
	}
	
    BufferedImage image = null;
	
	public void process(Stimulator stimulator) {
		
        retinaTask.setInput(image = stimulator.getNextImage());

		if (stimulator.isReset()) {
			for (LayerWLearning layer : resetLayers) {
				layer.reset();
			}
		}
		
    	try {
            NL.app.addTask(retinaTask);
        
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
//    	NL.mc.finish();
        NL.refreshImage();
    }
    
	public int worldSafeZone() {
		return worldStep() * OnOffMatrix.radius;
	}

	public int worldStep() {
		return (OnOffMatrix.centeRadius * 2) - 1;
	}

	public int worldWidth() {
		return (width  * worldStep()) + (worldSafeZone() * 2);
	}
	
	public int worldHeight() {
		return (height * worldStep()) + (worldSafeZone() * 2);
	}

	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

    public BufferedImage getImage() {
        return image;
    }
}
