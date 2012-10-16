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

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class OvalAnime implements Figure {

    private int[][] anime;

    private int i = 0;
    private double x, y, dx, dy;
    private double l = 0;
    private int a;
    private int X, Y;

    public OvalAnime(int a, int[][] anime) {
        this.a = a;
        this.anime = anime;
    }
	
	public void drawImage(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawOval(X - a / 2, Y - a / 2, a, a);
	}

	public void step() {
        if (l <= 0) {
            x = X = anime[i][0];
            y = Y = anime[i][1];
            int j = Math.min(i + 1, anime.length - 1);
            dx = anime[j][0] - x;
            dy = anime[j][1] - y;
            l = Math.sqrt(dx * dx + dy * dy);
            dx /= l; dy /= l;
            i = j == anime.length - 1 ? 0 : j;
        } else {
            x += dx; y += dy;
            X = (int) Math.round(x);
            Y = (int) Math.round(y);
            l--;
        }
    }
}