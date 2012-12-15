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
package org.animotron.animi.simulator.figures;

import java.awt.*;
import java.awt.geom.Point2D;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class RectAnime extends AbstractAnime {
	
	private int d;

    public RectAnime(int d, int maxX, int maxY) {
        super(0, null);

        this.d = d;
        this.maxX = maxX;
        this.maxY = maxY;
        
        reset();
    }
    
    public void reset() {
        int D = 2 * rnd.nextInt(d);
        
        int X = rnd.nextInt(maxX);
        int Y = rnd.nextInt(maxY);

        this.p = new Point2D[] {
	        new Point(X, Y),
	        new Point(X - D, Y - D),
	        new Point(X + D, Y - D),
	        new Point(X + D, Y + D),
	        new Point(X - D, Y + D)
        };
    }

	public RectAnime(int a, double dt, int[][] anime) {
        super(dt, anime);
        this.p = new Point2D[] {
                new Point(anime[0][0], anime[0][1]),
                new Point(anime[0][0] - a / 2, anime[0][1] - a / 2),
                new Point(anime[0][0] + a / 2, anime[0][1] - a / 2),
                new Point(anime[0][0] + a / 2, anime[0][1] + a / 2),
                new Point(anime[0][0] - a / 2, anime[0][1] + a / 2)
        };

    }
	
	public void drawImage(Graphics g) {
        g.setColor(Color.WHITE);
        Polygon polygon = new Polygon();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) {
                polygon.addPoint((int) Math.round(p[i].getX()), (int) Math.round(p[i].getY()));
            }
        }
        g.drawPolygon(polygon);
    }

}
