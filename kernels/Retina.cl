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
    
    output[mul24(y, outputSizeX)+y] = 0;

    barrier(CLK_GLOBAL_MEM_FENCE);

    int pos = mul24(y+offsetY, outputSizeX)+x+offsetX;
    
    if (pos < mul24(inputSizeX, inputSizeY))
    {
	    int rgb = input[mul24(inputSizeX, mul24(y, YScale)) + mul24(x, XScale)];
	    int R = (rgb >> 16) & 0xFF;
	    int G = (rgb >> 8 ) & 0xFF;
	    int B = (rgb      ) & 0xFF;
	    
	    //calculate gray
	    float value = (R + G + B) / 3;
	    
	    //normalize
	    output[pos] = value / 255;
    }
    
    barrier(CLK_LOCAL_MEM_FENCE);
}
