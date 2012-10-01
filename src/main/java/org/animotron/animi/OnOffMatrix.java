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

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class OnOffMatrix {
	
	//Параметры преобразования сетчатки в сигналы полей с он-центом и офф-центром

	//Радиус сенсорного поля
    int radius = 8;
    //Радиус центра сенсорного поля
    int centeRadius = 4;

    //Кол-во элементов в центре и переферии сенсорного поля
    int numInCenter = 0;
    int numInPeref = 0;
    
    int regionSize = 0;
    int[][] matrix;
    
    public OnOffMatrix() {
    	initialize();
    };
    
    private void initialize() {
        regionSize = 2 * radius - 2;
        
        matrix = new int[regionSize][regionSize];

        int radius2 = radius * radius;
        int centeRadius2 = centeRadius * centeRadius;

        int R2;
        int dx, dy;

        //Разметка квадратного массива двумя кругами (центром и переферией сенсорного поля)
        for (int ix = 0; ix < regionSize; ix++) {
        	for (int iy = 0; iy < regionSize; iy++) {

                dx = radius - ix;
                dy = radius - iy;
                R2 = dx * dx + dy * dy;

                if (R2 > radius2)
                    matrix[ix][iy] = 0;
                else {
                    if (R2 > centeRadius2) {
                        matrix[ix][iy] = 1;
                        numInPeref++;
                    } else {
                        matrix[ix][iy] = 2;
                        numInCenter++;
                    }
                }
        	}
        }
	}
    
    public int regionSize() {
    	return regionSize;
    }

	public int getType(int i, int j) {
		return matrix[i][j];
	}

	public int numInCenter() {
		return numInCenter;
	}

	public int numInPeref() {
		return numInPeref;
	}

	public int radius() {
		return radius;
	}
}
