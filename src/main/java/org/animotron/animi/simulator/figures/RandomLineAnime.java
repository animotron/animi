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

import org.animotron.animi.RuntimeParam;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class RandomLineAnime implements Figure {

    private int[][] p = new int[4][4];

    @RuntimeParam(name = "active")
    public boolean active = true;

    public RandomLineAnime(int x1, int y1, int x2, int y2) {

        p[0][2] = p[2][2] = x1;
        p[0][3] = p[2][3] = x2;
        p[1][2] = p[3][2] = y1;
        p[1][3] = p[3][3] = y2;

        for (int[] i : p) {
            init(i);
        }
    }
    
    private int random(int min, int max) {
        return (int) Math.round((max - min) * Math.random() + min);
    }

    private void init(int[] p) {
        p[0] = random(p[2], p[3]);
        d(p);
    }
    
    public void reset() {}

    private void d(int[] p) {
        p[1] = random(p[2] - p[0], p[3] - p[0]);
    }

    public void drawImage(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawLine(p[0][0], p[1][0], p[2][0], p[3][0]);
	}

    @Override
    public void step() {
        for (int[] i : p) {
            step(i);
        }
    }

    private void step(int[] p) {
        if (p[1] == 0) {
            d(p);
        } else {
            if (p[1] > 0) {
                p[0] ++;
                p[1] --;
            } else {
                p[0] --;
                p[1] ++;
            }
        }
    }

	@Override
	public boolean isActive() {
		return active;
	}

}
