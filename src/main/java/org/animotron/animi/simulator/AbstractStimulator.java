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
package org.animotron.animi.simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.animotron.animi.gui.Application;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public abstract class AbstractStimulator implements Stimulator {
	
    protected BufferedImage img = null;
    
    Application app;

    public AbstractStimulator(Application application) {
    	app = application;
    	init();
	}

	public void reset() {
		init();
	}
	@Override
	public String getImageName() {
		return this.getClass().getSimpleName();
	}

	public BufferedImage getImage() {
        return img;
	}
	
	public BufferedImage getUserImage() {
		BufferedImage image = getImage();
		
        //workaround
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.drawImage(getImage(), 0, 0, img.getWidth(), img.getHeight(), null);

        drawUImage(g);
        
    	return img;
	}

	protected void drawUImage(Graphics g) {
		int textY = g.getFontMetrics(g.getFont()).getHeight();

        g.setColor(Color.WHITE);

        g.drawString(app.fps + " fps; "+app.cortexs.count+" cycles;", 0, textY);
	}

	@Override
	public Object whatAt(Point point) {
		return null;
	}
	
	@Override
	public void focusGained(Point point) {
	}

	@Override
	public void focusLost(Point point) {
	}
	
	@Override
	public void closed(Point point) {
	}

	@Override
	public void refreshImage() {
	}
}
