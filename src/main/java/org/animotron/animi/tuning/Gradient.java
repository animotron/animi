package org.animotron.animi.tuning;

import java.awt.Color;

/**
 * 
 * <p>
 * There are a number of defined gradient types (look at the static fields), but
 * you can create any gradient you like by using either of the following
 * functions:
 * <ul>
 * <li>public static Color[] createMultiGradient(Color[] colors, int numSteps)</li>
 * <li>public static Color[] createGradient(Color one, Color two, int numSteps)</li>
 * </ul>
 * You can then assign an arbitrary Color[] object to the HeatMap as follows:
 * 
 * <pre>
 * myHeatMap.updateGradient(Gradient.createMultiGradient(new Color[] { Color.red,
 *     Color.white, Color.blue }, 256));
 * </pre>
 * 
 * </p>
 * 
 * <hr />
 * <p>
 * <strong>Copyright:</strong> Copyright (c) 2007, 2008
 * </p>
 * 
 * <p>
 * HeatMap is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * </p>
 * 
 * <p>
 * HeatMap is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </p>
 * 
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * HeatMap; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 * </p>
 * 
 * @author Matthew Beckler (matthew@mbeckler.org)
 * @author Josh Hayes-Sheen (grey@grevian.org), Converted to use BufferedImage.
 * @author J. Keller (jpaulkeller@gmail.com), Added transparency (alpha)
 *         support, data ordering bug fix.
 * @version 1.6
 */
public class Gradient {

	public static Color[] createGradient(final Color one, final Color two, final int numSteps) {
		int r1 = one.getRed();
		int g1 = one.getGreen();
		int b1 = one.getBlue();
		int a1 = one.getAlpha();

		int r2 = two.getRed();
		int g2 = two.getGreen();
		int b2 = two.getBlue();
		int a2 = two.getAlpha();

		int newR = 0;
		int newG = 0;
		int newB = 0;
		int newA = 0;

		Color[] gradient = new Color[numSteps];
		double iNorm;
		for (int i = 0; i < numSteps; i++) {
			iNorm = i / (double) numSteps; // a normalized [0:1] variable
			newR = (int) (r1 + iNorm * (r2 - r1));
			newG = (int) (g1 + iNorm * (g2 - g1));
			newB = (int) (b1 + iNorm * (b2 - b1));
			newA = (int) (a1 + iNorm * (a2 - a1));
			gradient[i] = new Color(newR, newG, newB, newA);
		}

		return gradient;
	}

	public static Color[] createMultiGradient(Color[] colors, int numSteps) {
		// we assume a linear gradient, with equal spacing between colors
		// The final gradient will be made up of n 'sections', where n =
		// colors.length - 1
		int numSections = colors.length - 1;
		int gradientIndex = 0; // points to the next open spot in the final
		// gradient
		Color[] gradient = new Color[numSteps];
		Color[] temp;

		if (numSections <= 0) {
			throw new IllegalArgumentException(
					"You must pass in at least 2 colors in the array!");
		}

		for (int section = 0; section < numSections; section++) {
			// we divide the gradient into (n - 1) sections, and do a regular
			// gradient for each
			temp = createGradient(colors[section], colors[section + 1],
					numSteps / numSections);
			for (int i = 0; i < temp.length; i++) {
				// copy the sub-gradient into the overall gradient
				gradient[gradientIndex++] = temp[i];
			}
		}

		if (gradientIndex < numSteps) {
			// The rounding didn't work out in our favor, and there is at least
			// one unfilled slot in the gradient[] array.
			// We can just copy the final color there
			for (/* nothing to initialize */; gradientIndex < numSteps; gradientIndex++) {
				gradient[gradientIndex] = colors[colors.length - 1];
			}
		}

		return gradient;
	}
}
