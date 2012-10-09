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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class RectAnime extends Stimulator {

    private int[][] anime;

    private int i = 0;
    private double dx, dy, dt;
    private double l = 0;
    private AffineTransform at;
    Point2D[] p;
    private Polygon polygon = null;

//    private int a;

    public RectAnime(int width, int height, int a, double dt, int[][] anime) {
    	super(0);
    	
//        this.a = a;
        this.dt = dt;
        this.anime = anime;

        this.p = new Point2D[] {
                new Point(anime[0][0], anime[0][1]),
                new Point(anime[0][0] - a / 2, anime[0][1] - a / 2),
                new Point(anime[0][0] + a / 2, anime[0][1] - a / 2),
                new Point(anime[0][0] + a / 2, anime[0][1] + a / 2),
                new Point(anime[0][0] - a / 2, anime[0][1] + a / 2)
        };

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
	
	public void drawImage(Graphics g) {
        g.setColor(Color.WHITE);
		
        if (polygon != null)
			g.drawPolygon(polygon);
	}

	protected void step() {
        if (l <= 0) {
            int j = Math.min(i + 1, anime.length - 1);
            dx = anime[j][0] - anime[i][0];
            dy = anime[j][1] - anime[i][1];
            l = Math.sqrt(dx * dx + dy * dy);
            dx /= l; dy /= l;
            i = j == anime.length - 1 ? 0 : j;
        } else {
            l--;
        }
        
        at = new AffineTransform();
        at.rotate(dt, p[0].getX(), p[0].getY());
        at.translate(dx, dy);
        polygon = new Polygon();
        for (int i = 0; i < p.length; i++) {
            Point2D q = at.transform(p[i], null);
            if (i > 0) {
                polygon.addPoint((int) Math.round(q.getX()), (int) Math.round(q.getY()));
            }
            p[i] = q;
        }
    }
}
