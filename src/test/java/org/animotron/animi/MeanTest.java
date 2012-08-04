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

import java.util.Arrays;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.AREV;
import org.animotron.statement.operator.DEF;
import org.animotron.statement.operator.REF;
import org.animotron.statement.operator.Utils;
import org.animotron.statement.value.VALUE;
import org.animotron.utils.MessageDigester;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import static org.animotron.expression.AnimoExpression.__;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.traversal.Evaluation.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author Eduard Khachukaev
 *
 */
public class MeanTest extends ATest {

	private String _(String name, String ... types) {
		
		String uuid = uuid();
		
		StringBuilder sb = new StringBuilder();
		sb.append("def ").append(MessageDigester.uuid().toString());
		
		for (int i = 0; i < types.length; i++) {
			sb.append(" (").append(types[i]).append(")");
		}
		
		sb.append(" '").append(name).append("'.");
		
//		System.out.println(sb.toString());
		
		__(sb.toString());
		
		return uuid;
	}
	
	private static TraversalDescription td = 
	Traversal.description().
		depthFirst().
		uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).
        evaluator(new org.neo4j.graphdb.traversal.Evaluator(){
			@Override
			public Evaluation evaluate(Path path) {

				if (path.length() == 0)
					return EXCLUDE_AND_CONTINUE;
				
				Relationship r = path.lastRelationship();
				if (!r.getStartNode().equals(path.endNode()))
					return EXCLUDE_AND_PRUNE;
				
				if (path.length() == 1)
					if (r.isType(VALUE._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 2)
					if (r.isType(AREV._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 3)
					if (r.isType(REF._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 4)
					if (r.isType(AN._))
						return INCLUDE_AND_PRUNE;

				return EXCLUDE_AND_PRUNE;
			}
        });
	
	private static TraversalDescription tdSensorToMental = 
	Traversal.description().
		depthFirst().
		uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).
        evaluator(new org.neo4j.graphdb.traversal.Evaluator(){
			@Override
			public Evaluation evaluate(Path path) {

				if (path.length() == 0)
					return EXCLUDE_AND_CONTINUE;
				
				Relationship r = path.lastRelationship();
				if (!r.getStartNode().equals(path.endNode()))
					return EXCLUDE_AND_PRUNE;
				
				if (path.length() == 1)
					if (r.isType(AREV._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 2)
					if (r.isType(REF._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 3)
					if (r.isType(AN._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 4)
					if (r.isType(VALUE._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 5)
					if (r.isType(AREV._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 6)
					if (r.isType(REF._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 7)
					if (r.isType(AN._))
						return INCLUDE_AND_PRUNE;

				return EXCLUDE_AND_PRUNE;
			}
        });

	private static TraversalDescription tdMental = 
	Traversal.description().
		depthFirst().
		uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).
        evaluator(new org.neo4j.graphdb.traversal.Evaluator(){
			@Override
			public Evaluation evaluate(Path path) {

				if (path.length() == 0)
					return EXCLUDE_AND_CONTINUE;
				
				Relationship r = path.lastRelationship();
				if (!r.getStartNode().equals(path.endNode()))
					return EXCLUDE_AND_PRUNE;
				
				if (path.length() == 1)
					if (r.isType(AREV._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 2)
					if (r.isType(REF._))
						return EXCLUDE_AND_CONTINUE;

				if (path.length() == 3)
					if (r.isType(AN._))
						return INCLUDE_AND_PRUNE;

				return EXCLUDE_AND_PRUNE;
			}
        });

	private void sensorStep(Node n, Brain brain) {
		FastSet<Relationship> state = FastSet.newInstance();
		
		for (Path path : td.traverse(n)) {
			
			Relationship r = path.lastRelationship();
			Node end = r.getEndNode();
			
			if (!Utils.haveContext(end)) {
				state.add(r);
			} else
				for (Relationship rr : brain.sensorState) {
					if (rr.getStartNode().equals(r.getEndNode())) {
						state.add(r);
						break;
					}
				}
//			brain.detectedSensorObject(r);
			
			//check next levels
//			mentalStep(r.getStartNode(), brain);
		}
		brain.replaceSensorState(state);
	}
	
	private Brain parse(String sentence) {
		Node n = null;

		Brain brain = new Brain();

		for (int i = 0; i < sentence.length(); i++) {
			n = VALUE._.get(sentence.charAt(i));
			if (n == null) {
				System.out.println("noise point '"+sentence.charAt(i)+"'");
				
			} else
				sensorStep(n, brain);
		}
			
		System.out.println(brain.toString());

		return brain;
	}
	
	class MentalObject {
		FastList<Relationship> steps = new FastList<Relationship>();
		
		public MentalObject(Relationship r) {
			steps.add(r);
		}
		
		public String toString() {
			return Arrays.toString(steps.toArray());
		}
	}
	
	class Brain {
		FastSet<Relationship> sensorState = new FastSet<Relationship>();
		FastSet<Relationship> detectedSensorObjects = new FastSet<Relationship>();
		
		FastSet<MentalObject> mentalState = new FastSet<MentalObject>();
		
		public Brain() {
			sensorState = FastSet.newInstance();
			mentalState = FastSet.newInstance();
		}
		
		protected void replaceSensorState(FastSet<Relationship> state) {
			FastSet.recycle(sensorState);
			sensorState = state;
			
			checkSensorStateForCompleteObjects();
		}
		
		protected void replaceMentalState(FastSet<MentalObject> state) {
			FastSet.recycle(mentalState);
			mentalState = state;
		}
		
		private void detectedSensorObject(Relationship r) {
			detectedSensorObjects.add(r);
			
			for (Path path : tdSensorToMental.traverse(r.getStartNode())) {
//				System.out.println(path);
				
				boolean matched = false;
				Relationship last = path.lastRelationship();
				for (MentalObject m : mentalState) {
					if (m.steps.getFirst().getStartNode().equals(last.getEndNode())) {
//						System.out.println("match by first "+m.steps.getFirst());
						m.steps.addFirst(last);
						matched = true;
						continue;
					}
					if (m.steps.getLast().getEndNode().equals(last.getStartNode())) {
//						System.out.println("match by last "+m.steps.getLast());
						m.steps.addLast(last);
						matched = true;
						continue;
					}
				}
				if (!matched)
					mentalState.add(new MentalObject(last));
			}
		}
		
		private void checkSensorStateForCompleteObjects() {
			for (Relationship r : sensorState) {
		        Relationship ar = r.getStartNode().getSingleRelationship(AREV._, Direction.INCOMING);
		        if (ar != null)
		        	detectedSensorObject(r);
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			sb.append("detected sensor objects: [ ");
			for (Relationship r : detectedSensorObjects) {
				sb.append(r.getId()).append(" ");
			}
			sb.append("]\n");

			sb.append("sensor state: [ ");
			for (Relationship r : sensorState) {
				sb.append(r.getId()).append(" ");
			}
			sb.append("]\n");

			sb.append("mental state: [ ");
			for (MentalObject mo : mentalState) {
				sb.append(mo.toString()).append(" ");
			}
			sb.append("]");
			
			return sb.toString();
		}
	}

	@Test
	public void test_00() throws Throwable {
		String image = "Петя";
		
		_(image);
		
		sleep(1);
		
		Brain brain = parse(image);

		Node n = VALUE._.get(image);
		assertNotNull(n);
		
		FastSet<Relationship> state = brain.sensorState;
		assertEquals(1, state.size());
		
		Node start = state.valueOf(state.head().getNext()).getStartNode();
		for (Path path : td.traverse(start))
			assertEquals(n, path.lastRelationship().getStartNode());
	}

	@Test
	public void test_01() throws Throwable {
		
		String sentence = "Петя пошёл";
		
    	__(
			"def id1 'Петя'.",
			"def id2 'пошёл'.",
			"def id3 id1 id2 '"+sentence+"'."
		);

    	sleep(1);

    	Brain brain = parse(sentence);

		Node n = VALUE._.get(sentence);
		assertNotNull(n);
		
		FastSet<Relationship> state = brain.sensorState;
		assertEquals(1, state.size());
		
		Node start = state.valueOf(state.head().getNext()).getStartNode();
		for (Path path : td.traverse(start))
			assertEquals(n, path.lastRelationship().getStartNode());
	}

	@Test
	public void test_02() throws Throwable {
		
		String sentence = "Петя пошёл";
		
    	__(
			"def id1 'Петя'.",
			"def id2 'пошёл'.",
			"def id3 id1 id2."
		);
    	
    	sleep(1);

		Brain brain = parse(sentence);

		FastSet<MentalObject> state = brain.mentalState;
		assertEquals(1, state.size());
		
		MentalObject mo = state.valueOf(state.head().getNext());
		assertEquals(2, mo.steps.size());
	}

	@Test
	@Ignore
	public void test_10() throws Throwable {
		__(
			"def image.", "def action.", "def pointer.", "def question.",
			"def time.", 
			"def time-from (time) (from).", 
			"def time-to (time) (to).", 
			"def time-shift (?time-from) (?time-to).",
			
			"def place.",
			"def place-from (place) (from).",
			"def place-to (place) (to).",
			"def place-shift (?place-from) (?place-to)."
		);
		
		//ОБРАЗЫ (24): 
//		_("я", "image");
//		_("он", "image");
//		_("она",  "image");
//		_("они", "image");

		_("Петя", "image");
//		_("луна", "image");
//		_("мальчик", "image");
//		_("небе", "image");
		
		_("деревне", "image", "place");
		_("школу", "image", "place");
		
//		_("домой", "image");
//		_("деревни", "image");
//		_("деревней", "image");
//			
//		_("тёмная", "image");
//		_("сторона", "image");
//		_("поезд", "image");
//		_("реки", "image");
//
//		_("неделю", "image", "time");
		_("утром", "image", "time");
//		_("днём", "image", "time");
//		_("полудню", "image", "time");
//		_("вечером", "image", "time");
//		_("ночью", "image", "time");
//		_("сегодня", "image", "time");
		
		//АКЦИИ (48): 
		_("пошёл", "action", "?time", "?place-shift");
		
//		_("взошла", "action");
//		_("появилась", "action");
//		_("знают", "action");
//		_("пойдёт", "action");
//		_("пришёл", "action");
//		_("появилось", "action");
//		_("сделал", "action");
//		_("делал", "action");
//		_("был", "action");
//		_("была",  "action");
		_("шёл", "action");
//		_("пошла", "action");
//		_("пошло", "action");
//		_("пошли", "action");
//		_("пришла", "action");
//		_("пришло", "action");
//		_("пришли", "action");
//		_("шла", "action");
//		_("шло", "action");
//		_("шли", "action");
//		_("сделала", "action");
//		_("делала", "action");
//		_("сделало", "action");
//		_("делало", "action");
//		_("сделали", "action");
//		_("делали", "action");
//		_("появились", "action");
//		_("появился", "action");
//		_("произошло", "action");
//		_("побежал", "action");
//		_("побежала", "action");
//		_("побежало", "action");
//		_("побежали", "action");
//		_("прибежал", "action");
//		_("прибежала", "action");
//		_("прибежало", "action");
//		_("прибежали", "action");
//		_("бежал", "action");
//		_("бежала", "action");
//		_("бежало", "action");
//		_("бежали", "action");
//		_("было", "action");
//		_("были",  "action");
//		_("положил", "action");
//		_("положила", "action");
//		_("положило", "action");
//		_("положили", "action");
		
		//УКАЗАТЕЛИ (7): 
		_("по", "pointer", "place");
		_("в", "pointer", "place-to");
//		_("к", "pointer");
//		_("спустя", "pointer");
//		_("за", "pointer");
//		_("из", "pointer");
//		_("возле", "pointer");

		//ВОПРОСИТЕЛИ (10): 
		_("когда", "question", "time");
		
		_("где", "question", "place");
		_("куда", "question", "place-to");
		_("откуда", "question", "place-from");
		
//		_("кто", "question");
//		_("что", "question");
//		
//		_("какой", "question");
//		_("какая", "question");
//		_("какое", "question");
//		_("какие", "question");
		
		testAnimi("Петя пошёл в школу по деревне утром.\n", "");
		testAnimi("где Петя?\n","Петя в школе.");
//		testAnimi("Вечером он пришёл домой.", "");
	}
	
    @Test
	@Ignore
	public void test_20() throws Throwable {
    	__(
			"def id1 'Петя'.",
			"def id2 'развел'.",
			"def id3 'костер'.",
			"def id4 'в'.",
			"def id5 'лес'.",
			"def id6 (id4 id5) 'в лесу'.",
			"id1 id2 (id3) (id6)."
		);
	}

	
}
