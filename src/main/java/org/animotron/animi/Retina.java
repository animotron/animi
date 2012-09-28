/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
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
package org.animotron.animi;

/**
 * @author <a href="aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Retina {

    public static final int RETINA_WIDTH = 640;
    public static final int RETINA_HEIGHT = 480;

    public static final int COLOR_CHANNELS = 3;

    public static final int RSENS_FIELD = 3;
    public static final int RCSENS_FIELD = 8;

    class Field {
        int type;
        int[][] center;
        int[][]  peref;
    }

    double k_contr1 = 1.45;
    double k_contr2 = 1.15;
    double k_contr3 = 1.15;
    int level__bright = 100;
    int level_min = 10 * COLOR_CHANNELS;



}
