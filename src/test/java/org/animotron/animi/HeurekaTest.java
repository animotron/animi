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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ferenc Kovacs
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class HeurekaTest extends ATest {

	/*
	 * If an object or topic has a property in a relation that connects an internal object to an external object 
	 * and they are found identical or the same or equivalent you have comment called heureka
	 */
	@Test
	public void test_01() throws Exception {
		
	}
	
	private String uuid() {
		return UUID.randomUUID().toString();
	}
	
	private void testAnimiParser(String msg, String expression) {
		Assert.fail("not implemented");
	}

	private void testAnimi(String msg, String expected) {
		Assert.fail("not implemented");
	}
}
