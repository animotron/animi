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

import org.animotron.graph.AnimoGraph;
import org.animotron.graph.GraphOperation;
import org.animotron.statement.operator.THE;
import org.neo4j.graphdb.Node;
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

    protected static Node NAME = null;
	
	private static final String INDEX_NAME = "words";

	private static RelationshipIndex INDEX;

	private Words() {
		IndexManager indexManager = AnimoGraph.getDb().index();
		
		AnimoGraph.getDb().registerKernelEventHandler(this);

		INDEX = indexManager.forRelationships(INDEX_NAME);
		
		NAME = AnimoGraph.execute(new GraphOperation<Node>() {
			@Override
			public Node execute() throws Exception {
				return THE._("name");
			}
		});
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