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
package animi.tuning;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Random;

import animi.Imageable;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Codes implements Imageable {
	
	public final static int CODES = 18;
	public final static int SHIFTS = 16;
	public final static int SHIFTIMES = 1;
	
	public final static Color[] GRADIENT_RAINBOW = Gradient.createMultiGradient(
			new Color[] { 
				Color.red,
				Color.orange, 
				Color.yellow, 
				Color.green,
				Color.blue, 
				new Color(181, 32, 255),
				Color.red
			}, 
			CODES
		);

	BufferedImage img;
	
	public Codes() {
		img = new BufferedImage(20, (CODES+1)*10, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		for (int i = 0; i < CODES; i++) {
			g.setColor(GRADIENT_RAINBOW[i]);
			g.fillRect(0, i*10, 10, i*10+10);
		}
		g.setColor(GRADIENT_RAINBOW[0]);
		g.fillRect(0, CODES*10, 10, CODES*10+10);
		
		Polygon p = new Polygon();
		p.addPoint(-4,-1);
		p.addPoint(4 ,-1);
		p.addPoint(4 , 1);
		p.addPoint(-4, 1);
		p.addPoint(-4, -1);
		
		g.translate(15, 5);

		for (int i = 0; i < CODES; i++) {
			g.setColor(GRADIENT_RAINBOW[i]);
			
			double alpha = Math.PI * i / (float)CODES;
			g.rotate(-alpha);
			
			g.fillPolygon(p);

			g.rotate(alpha);

			g.translate(0, 10);
		}
		g.setColor(GRADIENT_RAINBOW[0]);
		g.fillPolygon(p);
	}

	@Override
	public String getImageName() {
		return "Encoding description";
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.Imageable#refreshImage()
	 */
	@Override
	public void refreshImage() {
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.Imageable#getImage()
	 */
	@Override
	public BufferedImage getImage() {
		return img;
	}

	@Override
	public Object whatAt(Point point) {
		return null;
	}

	@Override
	public void focusGained(Point point) {}

	@Override
	public void focusLost(Point point) {}

	@Override
	public void closed(Point point) {}
	
	private static Random rnd = new Random();
	public static float distance(float mem, float in) {
		if (mem < 0f) {
			return 0.01f * rnd.nextFloat();
		}
		
		float d = Math.abs(in - mem);
		if (d >= CODES / 2f) {
			d = CODES - d;
		}
		
		if (d == 0f) {
			return 1f;
			
		} else if (d > 0f && d <= 1f) {
			return .8f;

		} else if (d > 1f && d <= 2f) {
			return .2f;
		}
		
		return 0f;
	}
}