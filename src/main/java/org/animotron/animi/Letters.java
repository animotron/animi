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

import org.animotron.graph.AnimoGraph;
import org.animotron.statement.operator.Utils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Letters implements KernelEventHandler {
	
	protected static Letters _ = new Letters();
	
    protected static final String LETTER = "letter";
    protected static final String STARTS = "starts";
    protected static final String DEFS = "DEF";
	private static final String INDEX_NAME = LETTER+"s";

	private static RelationshipIndex INDEX;

	private Letters() {
		IndexManager indexManager = AnimoGraph.getDb().index();
		
		AnimoGraph.getDb().registerKernelEventHandler(this);

		INDEX = indexManager.forRelationships(INDEX_NAME);
	}
	
	public static void add(Relationship value, char ch) {
		//System.out.println("adding '"+ch+"'");
		INDEX.add(value, LETTER, ch);
		
		for (Path path : Utils.td_THE.traverse(value.getStartNode())) {
			INDEX.add(path.lastRelationship(), DEFS, ch);
		}
		//IS topology here???
	}

	public static void addStarts(Relationship op, char ch) {
		//System.out.println("adding '"+ch+"'");
		INDEX.add(op, STARTS, ch);
	}

	public static IndexHits<Relationship> search(char ch) {
		//System.out.println("searching '"+ch+"'");
		return INDEX.get(LETTER, ch);
	}

	public static IndexHits<Relationship> starts(char ch) {
		//System.out.println("searching defs '"+ch+"'");
		return INDEX.get(STARTS, ch);
	}

	public static Node getDef(char ch) {
		IndexHits<Relationship> hits = INDEX.get(DEFS, ch);
		try {
			if (hits.hasNext()) {
				Relationship r = hits.next();
				
				if (hits.hasNext())
					throw new UnsupportedOperationException("more than one defs for letter '"+ch+"'");
				
				return r.getEndNode();
			}
		} finally {
			hits.close();
		}
		throw new UnsupportedOperationException("no letter '"+ch+"'");
	}

	@Override
	public void beforeShutdown() {
		_ = null;
	}

	@Override
	public void kernelPanic(ErrorState error) {
	}

	@Override
	public Object getResource() {
		return null;
	}

	@Override
	public ExecutionOrder orderComparedTo(KernelEventHandler other) {
		return null;
	}
}