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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.animotron.animi.MultiCortex;

import static java.awt.BorderLayout.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Application extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 3243253015790558286L;

	private WebcamPanel camView = null;
	
	private Button btInit = null;
	private Button btStep = null;
	private Button btPause = null;
	private Button btResume = null;
	
	protected static MultiCortex cortexs = null;
	
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

        //setLayout(new GridLayout(2,2,2,2));

        JPanel tools = new JPanel();
        add(tools, NORTH);
        
        btInit = new Button("initialize");
        btInit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                btInit.setEnabled(false);
                
                camView.pause();

                //задание параметров зон коры и структуры связей
                //BrainInit
                //инициализация зон коры
                //CortexInit
                //Начальный сброс "хорошо - плохо"
                cortexs = new MultiCortex();

                btPause.setEnabled(true);
                
                camView.resume();
                btStep.setEnabled(true);
            }
        });
        tools.add(btInit);

        btStep = new Button("step");
        btStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	cortexs.active = true;
            }
        });
        btStep.setEnabled(false);
        tools.add(btStep);

        btPause = new Button("pause");
        btPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pause();
                btPause.setEnabled(false);
                btResume.setEnabled(true);
            }
        });
        btPause.setEnabled(false);
        tools.add(btPause);

        btResume = new Button("resume");
        btResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resume();
                btResume.setEnabled(false);
                btPause.setEnabled(true);
            }
        });
        btResume.setEnabled(false);
        tools.add(btResume);
        
        //constants control
        //минимальное соотношение средней контрасности переферии и центра сенсорного поля, 
        //необходимое для активации контрастность для темных элементов (0)
		addDoubleSlider("соот.  переферии и центра", "KContr1", tools);
		//контрастность для светлых элементов
		addDoubleSlider("контрастность для светлых", "KContr2", tools);
		addDoubleSlider("минимальная контрастность", "KContr3", tools);
		
		addIntSlider("Bright Level","Level_Bright",tools);

		camView = new WebcamPanel();
        add(camView, CENTER);

        setBounds(0, 0, 800, 600);
        setLocationByPlatform(true);

        pack();

		setVisible(true);

	}
	
	private void addDoubleSlider(String name, final String constName, JPanel tools) {
		final Field field;
		double value;
		try {
			field = MultiCortex.class.getDeclaredField(constName);
			value = field.getDouble(MultiCortex.class);
		} catch (Exception ex) {
			return;
		}
		
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		
        panel.add(new JLabel(name), c);

        final JLabel label = new JLabel();
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 200, (int)Math.round(value * 100));
        slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				try {
					field.setDouble(MultiCortex.class, source.getValue() / 100.0);
			        label.setText(String.valueOf(field.get(MultiCortex.class)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

        label.setText(String.valueOf(value));
        label.setLabelFor(slider);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
        panel.add(label, c);

        //Turn on labels at major tick marks.
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(slider, c);
		
        tools.add(panel);
	}

	private void addIntSlider(String name, final String constName, JPanel tools) {
		final Field field;
		int value;
		try {
			field = MultiCortex.class.getDeclaredField(constName);
			value = field.getInt(MultiCortex.class);
		} catch (Exception ex) {
			return;
		}
		
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		
        panel.add(new JLabel(name), c);

        final JLabel label = new JLabel();
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 255, value);
        slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				try {
					field.setInt(MultiCortex.class, source.getValue());
			        label.setText(String.valueOf(field.get(MultiCortex.class)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

        label.setText(String.valueOf(value));
        label.setLabelFor(slider);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
        panel.add(label, c);

        //Turn on labels at major tick marks.
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(slider, c);
		
        tools.add(panel);
	}

	private void resume() {
		cortexs.active = true; 
		if (camView != null)
			camView.resume();
	}

	private void pause() {
		cortexs.active = false; 
		if (camView != null)
			camView.pause();
	}

	private void stop() {
		if (camView != null)
			camView.stop();
		cortexs.active = false; 
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Application());
	}
}
