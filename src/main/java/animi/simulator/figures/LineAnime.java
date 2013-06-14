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
package animi.simulator.figures;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class LineAnime extends AbstractAnime {

    public LineAnime(int a, double dt, int[][] anime) {
        super(dt, anime);
        this.p = new Point2D[] {
                new Point(anime[0][0], anime[0][1]),
                new Point(anime[0][0] - a / 2, anime[0][1] - a / 2),
                new Point(anime[0][0] + a / 2, anime[0][1] + a / 2)
        };
    }

    protected LineAnime(double dt, int[][] anime) {
        super(dt, anime);
    }

    public void reset() {}

    public void drawImage(Graphics g) {
        g.setColor(Color.WHITE);

        g.drawLine(
                (int) Math.round(p[1].getX()), (int) Math.round(p[1].getY()),
                (int) Math.round(p[2].getX()), (int) Math.round(p[2].getY())
        );
		
	}

}
