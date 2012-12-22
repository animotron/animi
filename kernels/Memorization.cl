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

__kernel void computeMemorization(
    int sizeX,

    __global float* package,
    __global int* packageFree,
    int numberOfPackages,

    __global float* rememberCols,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * sizeX) + x;
    int packagePos = 0;
    
	if (rememberCols[pos] > 0.0f)
    {
	    for (int p = 0; p < numberOfPackages; p++)
	    {
			packagePos = 
				(numberOfPackages * sizeX * y) + 
				(numberOfPackages * x) + 
				p;
		    
		    if (packageFree[packagePos] >= 1.0f)
		    {
			    if (package[packagePos] > 0.0f)
			    {
					packageFree[packagePos] = 0.0f;
				}
		    }
	    }
    }
	
//	barrier(CLK_LOCAL_MEM_FENCE);
}