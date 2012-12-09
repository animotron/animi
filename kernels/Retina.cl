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

__kernel void computeRetina(
    __global float* output,
    int outputSizeX,

    int offsetX, int offsetY,

    float XScale, float YScale,

    __global int* input,
    int inputSizeX, int inputSizeY
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    output[(y * outputSizeX) + y] = 0;

    barrier(CLK_LOCAL_MEM_FENCE);

    int pos = ((y + offsetY) * outputSizeX) + x + offsetX;
    
    if (pos < (inputSizeX * inputSizeY))
    {
	    int rgb = input[(inputSizeX * (int)(y * YScale)) + (int)(x * XScale)];
	    int R = (rgb >> 16) & 0xFF;
	    int G = (rgb >> 8 ) & 0xFF;
	    int B = (rgb      ) & 0xFF;
	    
	    //calculate gray
	    float value = (R + G + B) / 3;
	    
	    //normalize
	    output[pos] = value / 255.0f;
    }
    
//    barrier(CLK_LOCAL_MEM_FENCE);
}
