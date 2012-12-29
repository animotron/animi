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
float Neighbor(int x, int y, __global float* package, __global int* packageFree, int numberOfPackages, int sizeX)
{
	int packagePos = 0;
	
	float neighbor = 0.0f;
    for (int dx = -1; dx <= 1; dx++)
    {
    	if (dx == 0) continue;
    	
	    for (int dy = -1; dy <= 1; dy++)
	    {
	    	if (dy == 0) continue;
	    	
	    	int xi = x + dx;
	    	int yi = y + dy;

	    	for (int p = 0; p < numberOfPackages; p++)
		    {
				packagePos = (((sizeX * yi) + xi) * numberOfPackages) + p;
			    
			    if (packageFree[packagePos] > 0)
			    {
			    	neighbor = max(neighbor, package[packagePos]);
	    		}
			}
	    }
    }
    return neighbor;
}

__kernel void computeMemorization(
    __global float* output,
    int sizeX,

    __global float* package,
    __global int* packageFree,
    int numberOfPackages,

    __global float* rememberCols,

    __global int* linksSenapse,
    int linksNumber,

    __global float* linksWeight,
    int wLinksNumber,

    __global float* input,
    int inputSizeX,
    
    int cycle,

    float K_POROG_ACTIVATION_FINAL,
    float K_POROG_ACT_PAKETA
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * sizeX) + x;
    int packagePos = 0;
    
    rememberCols[pos] = 1.0f;

	barrier(CLK_LOCAL_MEM_FENCE);
	
	//set 0 in inhibitory zone of the active column
	if (output[pos] > K_POROG_ACTIVATION_FINAL)
	{
    	int offset = pos * (linksNumber * 2);
    
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	int xi = linksSenapse[offset + (l * 2)    ];
	    	int yi = linksSenapse[offset + (l * 2) + 1];
	        
	        if (xi != x && yi != y)
	        {
        		rememberCols[(yi * sizeX) + xi] = 0.0f;
    		}
	    }
    }
    else if (output[pos] > 0.0f)
    {
    	rememberCols[pos] = 0.0f;
    }
	
	barrier(CLK_LOCAL_MEM_FENCE);

	//all packages should be active 
	int pN = 0;
	int pP = 0;

	if (rememberCols[pos] > 0.0f)
	{
	    for (int p = 0; p < numberOfPackages; p++)
	    {
			packagePos = (pos * numberOfPackages) + p;
		    if (packageFree[packagePos] > 0)
		    {
		    	pN++;
		    	if (package[packagePos] > K_POROG_ACT_PAKETA)
		    	{
		    		pP++;
		    	}
		    }
	    }
	    
	    if (pN != 0)
	    {
		    if (pN == pP)
		    {
			    for (int p = 0; p < numberOfPackages; p++)
			    {
					packagePos = (((sizeX * y) + x) * numberOfPackages) + p;
				    
				    if (packageFree[packagePos] == 0)
				    {
					    if (package[packagePos] > 0.0f)
					    {
							packageFree[packagePos] = cycle;
						}
				    }
			    }
		    }
		   	rememberCols[pos] = 0.0f;
	   	}
    }

	barrier(CLK_LOCAL_MEM_FENCE);
	
	//neighborhood...
	if (rememberCols[pos] > 0.0f)
    {
    	int offset = pos * linksNumber * 2;
    
    	float maximum = 0.0f;
    	
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	int xi = linksSenapse[offset + (l * 2)    ];
	    	int yi = linksSenapse[offset + (l * 2) + 1];
	        
	        if (xi != x && yi != y)
	        {
			    for (int p = 0; p < numberOfPackages; p++)
			    {
					packagePos = (((sizeX * yi) + xi) * numberOfPackages) + p;
				    
				    if (packageFree[packagePos] == 0)
				    {
	        			maximum = max(maximum, package[packagePos]);
	        		}
        		}
    		}
	    }
	    
    	float own = 0.0f;
	    for (int p = 0; p < numberOfPackages; p++)
	    {
			packagePos = pos * numberOfPackages + p;
		    
			if (packageFree[packagePos] == 0)
			{
				own = max(own, package[packagePos]);
			}
		}
	    
	    float neighbor = Neighbor(x, y, package, packageFree, numberOfPackages, sizeX);

	    if (neighbor == 0.0f || maximum > own)
	    {
	    	rememberCols[pos] = 0.0f;
	    }
    }

	barrier(CLK_LOCAL_MEM_FENCE);

	if (rememberCols[pos] > 0.0f)
    {
	    for (int p = 0; p < numberOfPackages; p++)
	    {
			packagePos = (((sizeX * y) + x) * numberOfPackages) + p;
		    
		    if (packageFree[packagePos] == 0)
		    {
			    if (package[packagePos] > 0)
			    {
					packageFree[packagePos] = cycle;
				}
		    }
	    }
    }
	
	barrier(CLK_LOCAL_MEM_FENCE);
	
	//free up
    int linksOffset = ((y * sizeX) + x) * numberOfPackages;
	int wOffset = 0;
    
    for (int p = 0; p < numberOfPackages; p++)
    {
		packagePos = (pos * numberOfPackages) + p;
	    if (packageFree[packagePos] == 0)
	    {
	    	wOffset = (linksOffset + p) * wLinksNumber;
		    for (int l = 0; l < wLinksNumber; l++)
		    {
		        linksWeight[wOffset + l] = 0;
		    }
	    }
    }
}