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

    __global float* package,
    int numberOfPackages,

    __global float* linksWeight,
    __global int*   linksSenapse,
    int linksNumber,

    __global float* cycleCols,
    __global float* freeCols,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * outputSizeX) + x;
    
    int XSize = linksNumber * 2;
    int offset = (y * outputSizeX * XSize) + (x * XSize);

    int linksOffset = (y * outputSizeX * linksNumber * numberOfPackages) + (x * linksNumber * numberOfPackages);

	freeCols[pos] = 0.0f;

    float maximum = 0.0f;
    float sum = 0.0f;
    for (int p = 0; p < numberOfPackages; p++)
    {
    
		sum = 0.0f;
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	int xi = linksSenapse[offset + (l * 2)  ];
	    	int yi = linksSenapse[offset + (l * 2)+1];
	        
	        sum += input[(yi * inputSizeX) + xi] * linksWeight[linksOffset + (p * linksNumber) + l];
	    }
	    
	    if (sum < 0.5f) 
	    {
	    	sum = 0.0f;
    	}
    	else if (sum > 1.0f)
    	{
    		sum = 1.0f;
		}
	    
	    if (package[(y * outputSizeX * numberOfPackages) + (x * numberOfPackages) + p] < 0.0f)
    	{
    		if (freeCols[pos] < sum)
    		{
    			freeCols[pos] = sum;
			}
    	}
    	else
    	{
		    package[(y * outputSizeX * numberOfPackages) + (x * numberOfPackages) + p] = sum;
		    maximum = max(sum, maximum);
    	}
    }
    
    output[pos] = maximum;
    
    //activity during cycle
    if (cycleCols[pos] < maximum)
    {
    	cycleCols[pos] = maximum;
	}
}
