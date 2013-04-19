/*
 *  Copyright (C) 2012-2013 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
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
package org.animotron.animi.gui;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JInternalFrame;

import org.animotron.animi.cortex.Layer;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import static org.animotron.animi.gui.Application.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class Cube extends JInternalFrame implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener {

	Layer layer;
	
	//eye position for gluLookAt()
	float eyeX = 0f;
	float eyeY = 0f;
	float eyeZ = 20f;
	
	float degreeX = 0.0f;
	float degreeY = 0.0f;
	
	private int mX = 0;
	private int mY = 0;
	
	GLCanvas canvas;
	GLUT glut = new GLUT();
	
	public Cube(Layer layer) {
		super("Cube", true, // resizable
				true,		 // closable
				false,		 // maximizable
				true);		 // iconifiable
		
		this.layer = layer;

		canvas = new GLCanvas(caps);

		canvas.addGLEventListener(this);

		getContentPane().add(canvas, BorderLayout.CENTER);

		FPSAnimator animator = new FPSAnimator(canvas, 60);
		animator.add(canvas);
		animator.start();
		
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);

		setLocation(10, 10);
		setSize(width, height);

		setVisible(true);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		update();
		render(drawable);
	}

	private void update() {
	}

	int height = 600;
	int width = 600;
	float zNear=1f, zFar=1000f;
	
	public void setProjectionMatrix (GL2 gl, int width, int height) { 
		
		gl.glViewport(0,0,width,height);
//		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective(50.0f, width/(float)height, zNear, zFar);
		//camera position & projection
		glu.gluLookAt(eyeX, eyeY, eyeZ, 0,0,0, 0,1,0);
	}	

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		setProjectionMatrix(gl, width, height);
		
  		gl.glRotatef(degreeX, 0.0f, 1.0f, 0.0f);
  		gl.glRotatef(degreeY, 0.0f, 0.0f, 1.0f);

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
//		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glEnable(GL.GL_BLEND);              //activate blending mode
	    gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);  //define blending factors

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glPushMatrix();

//		gl.glRotatef(30.0f, 1.0f, 0.0f, 0.0f);
//		gl.glRotatef(35.0f, 0.0f, 1.0f, 0.0f);

		gl.glColor3f(0xff, 0xff, 0xff);

		float cs = 0.4f;
		float gs = 1.1f;
		int xs = layer.width();
		for (int lz = 0; lz < layer.depth(); lz++) {
			for (int ly = 0; ly < layer.height(); ly++) {
				for (int lx = 0; lx < layer.width(); lx++) {

					float act = layer.axons().get(lx, ly, lz);
					
					float R = act > 0.1f ? 1f : 0.1f;
					float alpha = act > 0.1f ? 1f : 0.1f;
					
					gl.glColor4f(R, 0f, 0f, alpha);

					gl.glTranslatef(gs, 0.0f, 0.0f);
					glut.glutSolidCube(cs);
				}
				gl.glTranslatef(-gs * xs, gs, 0.0f);
			}
			gl.glTranslatef(0f, -gs * xs, gs);
		}
		gl.glPopMatrix();

		// GL2 gl = drawable.getGL().getGL2();
		//
		// gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//
		// // draw a triangle filling the window
		// gl.glBegin(GL.GL_TRIANGLES);
		// gl.glColor3f(1, 0, 0);
		// gl.glVertex2d(-c, -c);
		// gl.glColor3f(0, 1, 0);
		// gl.glVertex2d(0, c);
		// gl.glColor3f(0, 0, 1);
		// gl.glVertex2d(s, -s);
		// gl.glEnd();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// drawable.getGL().setSwapInterval(1);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	//Mouse events
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		mX = e.getX();
		mY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
//		if (e.getModifiers() == Event.META_MASK) {
			degreeX = degreeX + (e.getX() - mX);
			degreeY = degreeY - (e.getY() - mY);
			
			canvas.display();
			
			mX = e.getX();
			mY = e.getY();
//		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		eyeZ = eyeZ + e.getWheelRotation();

		canvas.display();
	}
}
