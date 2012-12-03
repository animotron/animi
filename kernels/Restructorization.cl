/*
 *  Copyright (C) 2012 The Animo Project
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
 
 /*
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */

__kernel void computeRestructorization(
    __global float* cols,
    int sizeX,

    __global float* linksWeight,
    __global int*   linksSenapse,
    int linksNumber,
    
    float ny,
    __global float* nys,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float activity = cols[mul24(y, sizeX)+x];
    if (activity > 0)
    {
	    int XSize = mul24(linksNumber, 2);
	    int offset = mul24(y, mul24(sizeX, XSize)) + mul24(x, XSize);
	    
		int linksOffset = mul24(y, mul24(sizeX, linksNumber)) + mul24(x, linksNumber);
		
		float factor = 1;
		if (nys[mul24(y, sizeX)+x] != ny)
		{
			factor = nys[mul24(y, sizeX)+x];
			nys[mul24(y, sizeX)+x] = ny;
		} else {
			factor = activity * nys[mul24(y, sizeX)+x];
		}
	
		float w;
		float sumQ2 = 0;
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	int xi = linksSenapse[offset + mul24(l, 2)  ];
	    	int yi = linksSenapse[offset + mul24(l, 2)+1];
	    	
	    	w = linksWeight[linksOffset + l];
	    	
	    	w += input[mul24(yi, inputSizeX)+xi] * factor;
	
			sumQ2 += w * w;
			
			linksWeight[linksOffset + l] = w;
		}
	
		float norm = sqrt(sumQ2);
	    for(int l = 0; l < linksNumber; l++)
	    {
			linksWeight[linksOffset + l] = linksWeight[linksOffset + l] / norm;
		}
	}
}