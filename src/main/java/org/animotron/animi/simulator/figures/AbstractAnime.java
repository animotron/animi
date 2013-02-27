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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.animotron.animi.RuntimeParam;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public abstract class AbstractAnime implements Figure {

    protected int maxX = 0, maxY = 0;

    protected int[][] anime;

    private int i = 0;
    private double dx, dy, dt;
    protected double l = 0;
    private AffineTransform at;
    Point2D[] p;
    
    @RuntimeParam(name = "active")
    public boolean active = true;

    public AbstractAnime(double dt, int[][] anime) {
        this.dt = dt;
        this.anime = anime;
    }
	
	public void step() {
		if (anime == null)
			return;
		
        if (l < 0) {
            int j = Math.min(i + 1, anime.length - 1);
            dx = anime[j][0];
            dy = anime[j][1];
            i = j == anime.length - 1 ? 0 : j;
//        } else if (l < 0) {
//                int j = Math.min(i + 1, anime.length - 1);
//                dx = anime[j][0] - anime[i][0];
//                dy = anime[j][1] - anime[i][1];
//                i = j == anime.length - 1 ? 0 : j;
        } else if (l == 0) {
            int j = Math.min(i + 1, anime.length - 1);
            dx = anime[j][0] - anime[i][0];
            dy = anime[j][1] - anime[i][1];
            l = Math.sqrt(dx * dx + dy * dy);
            dx /= l; dy /= l;
            i = j == anime.length - 1 ? 0 : j;
        } else {
            l--;
            if (l < 0) l = 0;
        }
        at = new AffineTransform();
        at.rotate(dt, p[0].getX(), p[0].getY());
        at.translate(dx, dy);
        for (int i = 0; i < p.length; i++) {
            Point2D q = at.transform(p[i], null);
            p[i] = q;
        }
    }
	
	@Override
	public boolean isActive() {
		return active;
	}
}
