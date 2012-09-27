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
package org.animotron.animi.gui;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.animotron.animi.MultiCortex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Application extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 3243253015790558286L;

	private WebcamPanel camView = null;
	
	private Button btInit = null;
	private Button btPause = null;
	private Button btResume = null;
	
	private MultiCortex cortexs = null;
	
	private Application() {}
	
	@Override
	public void run() {
		setTitle("Animi");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosed(WindowEvent e) {
        		stop();
        	}

        	@Override
        	public void windowDeiconified(WindowEvent e) {
        		resume();
        	}

			@Override
        	public void windowIconified(WindowEvent e) {
        		pause();
        	}
        });

        setLayout(new GridLayout(2,2,2,2));
        
        btInit = new Button("initialize");
        btInit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        
				btInit.disable();

		        //задание параметров зон коры и структуры связей
		        //BrainInit
		        //инициализация зон коры
		        //CortexInit
		        //Начальный сброс "хорошо - плохо"
		        cortexs = new MultiCortex();

//		        FillMSensPol() ' создание связей сенсорных полей
//
//		        SetOnOFF() ' распределение он и офф полей

		        btPause.setEnabled(true);
			}
		});
        add(btInit);

        btPause = new Button("pause");
        btPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pause();
				btPause.setEnabled(false);
				btResume.setEnabled(true);
			}
		});
        add(btPause);

        btResume = new Button("resume");
        btResume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resume();
				btResume.setEnabled(false);
				btPause.setEnabled(true);
			}
        });
        add(btResume);

        camView = new WebcamPanel();
        add(camView);
        
        setBounds(0, 0, 800, 600);

		pack();
		setVisible(true);

	}

	private void resume() {
		if (camView != null)
			camView.resume();
	}

	private void pause() {
		if (camView != null)
			camView.pause();
	}

	private void stop() {
		if (camView != null)
			camView.stop();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Application());
	}
}
