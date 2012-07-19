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

import static org.animotron.expression.AnimoExpression.__;

import org.animotron.graph.AnimoGraph;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Labels implements KernelEventHandler {
	
	protected static Labels _ = new Labels();
	
    protected static final String NAME = "label";
	private static final String INDEX_NAME = NAME+"s";

	private static RelationshipIndex INDEX;

	private Labels() {
		IndexManager indexManager = AnimoGraph.getDb().index();
		
		AnimoGraph.getDb().registerKernelEventHandler(this);

		INDEX = indexManager.forRelationships(INDEX_NAME);
		
		if (!hasLetters()) {
			Transaction tx = AnimoGraph.beginTx();
			try {
				createLetters();
				
				tx.success();
			} finally {
				AnimoGraph.finishTx(tx);
			}
		}
	}
	
	public static void add(Relationship the, String word) {
//		System.out.println("adding '"+word+"'");
		INDEX.add(the, INDEX_NAME, word);
	}

	public static IndexHits<Relationship> search(String word) {
//		System.out.println("searching '"+word+"'");
		return INDEX.get(INDEX_NAME, word);
	}
	
	private boolean hasLetters() {
		IndexHits<Relationship> hits = Letters.search('a');
		try {
			return hits.hasNext();
		} finally {
			hits.close();
		}
	}

	private void createLetters() {
		IndexHits<Relationship> hits = Letters.search('a');
		try {
			if (hits.hasNext()) return;
		} finally {
			hits.close();
		}
		
		StringBuilder sb = null;
		
		for (char ch = 'a'; ch <= 'z'; ch++) {
			sb = new StringBuilder();
			sb.append("def ").append(ch).append(" letter \"").append(ch).append("\"");
			__(sb.toString());
		}

		for (char ch = 'A'; ch <= 'Z'; ch++) {
			sb = new StringBuilder();
			sb.append("def ").append(ch).append(" letter \"").append(ch).append("\"");
			__(sb.toString());
		}
		
		int shift = 'A' - 'a';

		for (char ch = 'A'; ch <= 'Z'; ch++) {
			sb = new StringBuilder();
			char low = (char)(ch - shift);
			sb.append("def ").append(ch).append(low).append(" (").append(ch).append(") (").append(low).append(")");
			__(sb.toString());
		}
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