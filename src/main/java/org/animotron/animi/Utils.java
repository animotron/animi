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
package org.animotron.animi;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Utils {

    private final static double LUM_RED = 0.299;
    private final static double LUM_GREEN = 0.587;
    private final static double LUM_BLUE = 0.114;

    public static int calcGrey(final BufferedImage img, final int x, final int y) {
        int value = img.getRGB(x, y);

        int r = get_red(value);
        int g = get_green(value);
        int b = get_blue(value);
        
//        return r+g+b;
//        return (r+g+b) /3;
        return (int) Math.round(r * LUM_RED + g * LUM_GREEN + b * LUM_BLUE);
    }

    public static int create_rgb(int alpha, int r, int g, int b) {
        int rgb = (alpha << 24) + (r << 16) + (g << 8) + b;
        return rgb;
    }

    public static int get_alpha(int rgb) {
        return (rgb >> 24) & 0xFF;
        // return rgb & 0xFF000000;
    }

    public static int get_red(int rgb) {
        return (rgb >> 16) & 0xFF;
        // return rgb & 0x00FF0000;
    }

    public static int get_green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int get_blue(int rgb) {
        return rgb & 0xFF;
    }
    

}
