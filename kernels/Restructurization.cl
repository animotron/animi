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

__kernel void computeRestructurization(
    __global float* cols,
    int sizeX,

    __global float* package,
    int numberOfPackages,

    __global float* linksWeight,
    __global int*   linksSenapse,
    int linksNumber,
    
    __global float* rememberCols,
    __global float* freeCols,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float ny = 0.1f;
    
    int pos = (y * sizeX)+x;
    
    //current activity
    float activity = cols[pos];
    
    if (activity > 0.0f) //cycle activity?
    {
	    int XSize = (linksNumber * 2);
	    int offset = (y * sizeX * XSize) + (x * XSize);
	    
	    int offsetPackagers = linksNumber * numberOfPackages;
		
	    for (int p = 0; p < numberOfPackages; p++)
    	{
			int lOffset = (y * sizeX * offsetPackagers) + (x * offsetPackagers) + (p * linksNumber);

    		if (package[(y * sizeX * numberOfPackages) + (x * numberOfPackages) + p] > 0.0f)
	    	{
    			float sumWp = 0;
    			float sumWn = 0;
    			float w = 0;
			    
			    for(int l = 0; l < linksNumber; l++)
			    {
			    	int xi = linksSenapse[offset + (l * 2)    ];
			    	int yi = linksSenapse[offset + (l * 2) + 1];
			    	
			    	if (input[ ( yi * inputSizeX ) + xi ] > 0.0f)
			    	{
			    		w =   activity * linksWeight[lOffset + l] * ny;
		    		}
		    		else
		    		{
			    		w = - activity * linksWeight[lOffset + l] * ny;
		    		}

			    	w += linksWeight[lOffset + l];
					linksWeight[lOffset + l] = w;
					
					if (w > 0.0f)
					{
						sumWp += w;
					}
					else
					{
						sumWn += w;
					}
				}
			
			    for(int l = 0; l < linksNumber; l++)
			    {
			    	if (linksWeight[lOffset + l] > 0.0f)
			    	{
						linksWeight[lOffset + l] = linksWeight[lOffset + l] / sumWp;
		    		}
		    		else
		    		{
			    		linksWeight[lOffset + l] = linksWeight[lOffset + l] / -sumWn;
					}
				}
    		}
    	}
    }
}