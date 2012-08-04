/*
 *  Copyright (C) 2011-2012 The Animo Project
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
package org.animotron.animi;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Ferenc Kovacs
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class QATest extends ATest {

	@Test
	public void test() {
		
		String human = _("human");

		_("Ann");
		_("Ferenc Kovacs", human);
		_("Evgeny Gazdovsky", human);
		_("Dmitriy Shabanov", human);
		
		//object, properties, relations
		
		//Definition: one for machine, one for user
		
		question("What is the name of the system?");
		question("What is the purpose of the system?");
		question("What is the form of the system's description?");
		question("Who are the participants?");
		question("How does the look like for this passage?");
		question("How do we interpret the code in semantic terms?");
	}
	
	private String question(String message) {
		return "";
	}

}
