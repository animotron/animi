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

__kernel void computeInhibitory(
    __global float* output,
    int sizeX,

    __global int* linksSenapse,
    int linksNumber,

    __global float* package,
    __global int* packageFree,
    int numberOfPackages,

    __global float* rememberCols,
    __global float* cycleCols,
    __global float* input
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * sizeX) + x;
    
    rememberCols[pos] = 1;

	barrier(CLK_LOCAL_MEM_FENCE);

	//set 0 in inhibitory zone of the active column
	if (cycleCols[pos] > 0)
	{
	    int XSize = (linksNumber * 2);
    	int offset = (y * sizeX * XSize) + (x * XSize);
    
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	int xi = linksSenapse[offset + (l * 2)    ];
	    	int yi = linksSenapse[offset + (l * 2) + 1];
	        
        	rememberCols[(yi * sizeX) + xi] = 0.0f;
	    }
    	rememberCols[(y * sizeX) + x] = 0.0f;
    }
	
//	barrier(CLK_LOCAL_MEM_FENCE);
}
