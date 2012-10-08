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
import java.awt.image.BufferedImage;

import org.animotron.animi.Imageable;
import org.animotron.animi.cortex.Retina;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Geometry implements Imageable {
	
	private BufferedImage image;

	public Geometry() {
		image = new BufferedImage(Retina.WIDTH, Retina.HEIGHT, BufferedImage.TYPE_INT_RGB);
	}
	
	public BufferedImage getImage(String imageID) {
		step();
		
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.drawRect(100, 100, 100, 100);
		g.fillRect(250, 100, 100, 100);

		g.drawLine(400, 100, 450, 200);
		
		g.drawOval(100, 250, 100, 100);
		g.fillOval(250, 250, 100, 100);

		return image;
	}
	
	private void step() {
	}
}
