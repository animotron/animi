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
package org.animotron.animi.simulator.figures;

import java.awt.*;
import java.util.Random;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class RotateAnime extends AbstractAnime {
	
	int width;
	int height;
	
	private boolean filled = false;

	public RotateAnime(int width, int height, boolean filled) {
        super(0, null);
        this.filled = filled;

        this.width = width;
        this.height = height;
    }
	
	double i = 0.0;
	Random rnd = new Random();

	public void drawImage(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.translate(
			(int)(width / 2.0),
			(int)(height / 2.0)
		);
		
		i += 0.01;
		g2d.rotate(Math.PI * rnd.nextDouble() * 2);
        
		g2d.setColor(Color.WHITE);
        
		Polygon polygon = new Polygon();
        polygon.addPoint(-width,0);
        polygon.addPoint(-width,height);
        polygon.addPoint(width,height);
        polygon.addPoint(width,0);
        polygon.addPoint(-width,0);

        if (filled)
        	g2d.fillPolygon(polygon);
        else
        	g2d.drawPolygon(polygon);
    }

	@Override
	public void reset() {
	}
}
