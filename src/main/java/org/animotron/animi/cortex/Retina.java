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

import org.animotron.animi.simulator.Stimulator;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: macula lutea
public class Retina {

	//Сетчатка
//  public static final int WIDTH = 192;
//	public static final int HEIGHT = 144;
//	public static final int WIDTH = 160;
//	public static final int HEIGHT = 120;
	public static final int WIDTH = 30;
	public static final int HEIGHT = 30;
	
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
	public void setNextLayer(LayerSimple sz) {
		NL = sz;
		
		width = sz.width();
		height = sz.height();

		initialize();
	}
	
    // создание связей сенсорных полей
	private void initialize() {
        retinaTask = new RetinaZone(this, (LayerSimple)NL);
    }
	
    BufferedImage image = null;
	
	public void process(Stimulator stimulator) {
		
        retinaTask.setInput(image = stimulator.getNextImage());

    	try {
            NL.mc.addTask(retinaTask);
        
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
