/*
 *  Copyright (C) 2011 The Animo Project
 *  http://animotron.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.animotron.animi;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Ferenc Kovacs
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class NounPlusNounTest extends ATest {

	/*
	 * 1. Problems: noun plus noun may refer to a single object e.g. and be in a productive relation (not to be separated) 
	 * or in an additive relation where noun1 is an object and noun2 is a property that object has, but not a quality of that object, 
	 * so they are separable example?
	 * 
	 *  driver seat / a specific seat on the front left hand side inside a car on the continent - productive relation , a term
	 *  back seat / a generic seat anywhere this spatial/orientational relation exists between a seat and its relative location
	 *  
	 *  2. yes is the answer 
	 * 
	 */
	@Test
	public void test_01() throws Exception {

		testAnimi("driver\n", "driver");
		testAnimi("back\n", "back");
		testAnimi("seat\n", "seat");
		
		testAnimi("driver seat\n", "driver seat");
		testAnimi("back seat\n", "back seat");
		
		//XXX: to be continue
	}
	
}