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
    int RSensPol = 8;
    //Радиус центра сенсорного поля
    int RCSensPol = 4;

    //Кол-во элементов в центре и переферии сенсорного поля
    int NSensCentr = 0;
    int NSensPeref = 0;
    
    int sensPoLength = 0;
    int[][] SQ;
    
    public OnOffMatrix() {
        sensPoLength = 2 * RSensPol - 2;
        
        SQ = new int[sensPoLength][sensPoLength];

        int RPol2 = RSensPol * RSensPol;
        int RCen2 = RCSensPol * RCSensPol;

        int R2;
        int dx, dy;

        //Разметка квадратного массива двумя кругами (центром и переферией сенсорного поля)
        for (int ix = 0; ix < sensPoLength; ix++) {
        	for (int iy = 0; iy < sensPoLength; iy++) {

                dx = RSensPol - ix;
                dy = RSensPol - iy;
                R2 = dx * dx + dy * dy;

                if (R2 > RPol2)
                    SQ[ix][iy] = 0;
                else {
                    if (R2 > RCen2) {
                        SQ[ix][iy] = 1;
                        NSensPeref++;
                    } else {
                        SQ[ix][iy] = 2;
                        NSensCentr++;
                    }
                }
        	}
        }
	}
    
    public int sensPoLength() {
    	return sensPoLength;
    }

	public int getType(int i, int j) {
		return SQ[i][j];
	}

	public int NSensCentr() {
		return NSensCentr;
	}

	public int NSensPeref() {
		return NSensPeref;
	}

	public int RSensPol() {
		return RSensPol;
	}
}
