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
import java.util.Random;

import static animi.tuning.Codes.*;
import static java.lang.Math.*;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class RotateAnime extends AbstractAnime {
	
	boolean isRandom = false;
	
	int width;
	int height;
	
	private boolean filled = false;

	private Polygon polygon;
	
	int[][] tremor;

	public RotateAnime(int width, int height, float worldStep, boolean filled) {
        super(0, null);
        this.filled = filled;

        this.width = width;
        this.height = height;

    	polygon = new Polygon();
        polygon.addPoint(-width,0);
        polygon.addPoint(-width,height);
        polygon.addPoint(width,height);
        polygon.addPoint(width,0);
        polygon.addPoint(-width,0);
        
//        double twoPI = PI; //2.0 * PI;
//        double delta = twoPI / (double)Codes.SHIFTS;
        
        tremor = new int[SHIFTS][2];
        
        int i = 0;
        int half = SHIFTS / 4;
        for (int delta = -half; delta < half; delta++) {
        	tremor[i] = 
    			new int[] {
        			(int) (1.0 * worldStep * delta), 
        			(int) (1.0 * worldStep * delta)
        		};
        	i++;
        }
        for (int delta = half; delta > -half; delta--) {
        	tremor[i] = 
    			new int[] {
        			(int) (1.0 * worldStep * delta), 
        			(int) (1.0 * worldStep * delta)
        		};
        	i++;
        }
	}
	
	Random rnd = new Random();
	
	double angelStep = PI / (double)CODES;
	double alfa = nextAngel();
	int code = -1;
	
	int step = 0;
	int start = 0;
	
	private double nextAngel() {
		if (isRandom) {
			return angelStep * (code = rnd.nextInt(CODES));
		
		} else {
			code++;
			if (code >= CODES)
				code = 0;

			final double angel = angelStep * code;
			
			return angel;
		}
	}
	
	public int getCode() {
		return code;
	}

	public void drawImage(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		//bring to center
		g2d.translate(
			(int)(width / 2.0),
			(int)(height / 2.0)
		);
		
		//rotate on angel alfa
		g2d.rotate(alfa);
		
		int[] pos = tremor[step];
		//shift by tremor
		g2d.translate(
			pos[0],
			pos[1]
		);
        
		g2d.setColor(Color.WHITE);
        if (filled)
        	g2d.fillPolygon(polygon);
        else
        	g2d.drawPolygon(polygon);
    }
	
	@Override
	public boolean step() {
		step++;
		if (step >= tremor.length) {
			step = 0;
		} else {
		}
		return start == step;
	}

	@Override
	public void reset() {
//		start = step = rnd.nextInt(tremor.length);
		alfa = nextAngel();
	}
}
