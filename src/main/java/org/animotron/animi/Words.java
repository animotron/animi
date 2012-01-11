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
public class Words implements KernelEventHandler {
	
	private static Words _ = null;
	
	public static Words words() {
		if (_ == null) {
			_ = new Words();
		}
		return _;
	}

    protected static final String NAME = "name";
	
	private static final String INDEX_NAME = "words";

	private static RelationshipIndex INDEX;

	private Words() {
		IndexManager indexManager = AnimoGraph.getDb().index();
		
		AnimoGraph.getDb().registerKernelEventHandler(this);

		INDEX = indexManager.forRelationships(INDEX_NAME);
		
	}
	
	public void add(Relationship the, String word) {
		INDEX.add(the, INDEX_NAME, word);
	}

	public IndexHits<Relationship> search(String word) {
		return INDEX.get(INDEX_NAME, word);
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