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

__kernel void computeActivation(
    __global float* output,
    int outputSizeX,

    __global float* linksWeight,
    __global int*   linksSenapse,
    int linksNumber,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int XSize = mul24(linksNumber, 2);
    int offset = mul24(y, mul24(outputSizeX, XSize)) + mul24(x, XSize);

    int linksOffset = mul24(y, mul24(outputSizeX, linksNumber)) + mul24(x, linksNumber);

    float sum = 0.0f;

    for(int l = 0; l < linksNumber; l++)
    {
    	int xi = linksSenapse[offset + mul24(l, 2)  ];
    	int yi = linksSenapse[offset + mul24(l, 2)+1];
        
        sum += input[mul24(yi, inputSizeX) + xi] * linksWeight[linksOffset + l];
    }

    output[mul24(y, outputSizeX)+x] = sum;
    
    barrier(CLK_LOCAL_MEM_FENCE);
}
