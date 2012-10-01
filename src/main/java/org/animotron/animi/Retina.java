/*
 *  Copyright (C) 2012 The Animo Project
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
package org.animotron.animi;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: macula lutea
public class Retina {
	
	/* x */
	private int width;
	/* y */
	private int height;
	
	//Числовое представление сетчатки. Черно-белая картинка.
	private int preprocessed[][];
	
//	private int receptiveField[][] = null;
	private OnOffReceptiveField[][] sensorField;
	
	private OnOffMatrix onOff = new OnOffMatrix();
	
	public Retina(int width, int height) {
		this.width = width;
		this.height = height;
		
		preprocessed = new int[width][height];
	}
	
	private Layer NL = null;
	public void setNextLayer(Layer layer) {
		NL = layer;
		initialize();
	}
	
    // создание связей сенсорных полей
	private void initialize() {
		sensorField = new OnOffReceptiveField[NL.width()][NL.height()];

        double XScale = (width - onOff.regionSize()) / (double)NL.width();
        double YScale = (height - onOff.regionSize()) / (double)NL.height();

        int X, Y;
        int NC, NP;

        boolean top = true;
        boolean fl = true;
        
        for (int ix = 0; ix < NL.width(); ix++) {
        	fl = top = !top;
        	for (int iy = 0; iy < NL.height(); iy++) {

        		OnOffReceptiveField mSensPol = new OnOffReceptiveField();
                sensorField[ix][iy] = mSensPol;
                mSensPol.centr = new int[onOff.numInCenter()][2];
        		mSensPol.peref = new int[onOff.numInPeref()][2];

                // распределение он и офф полей
                if (fl) {
                	mSensPol.type = 3;
                } else {
                	mSensPol.type = 3;
                }
                fl = !fl;

                X = (int)Math.round( ix * XScale + onOff.radius() + 1 );
                Y = (int)Math.round( iy * YScale + onOff.radius() + 1 );

                NC = 0;
                NP = 0;

                for (int i = 0; i < onOff.regionSize(); i++) {
                    for (int j = 0; j < onOff.regionSize(); j++) {

                    	switch (onOff.getType(i, j)) {

                        case 0:
                            break;
                        case 1:

                            mSensPol.peref[NP][0] = X - onOff.radius() + i;
                            mSensPol.peref[NP][1] = Y - onOff.radius() + j;

                            NP = NP + 1;
                            break;
                        case 2:

                        	mSensPol.centr[NC][0] = X - onOff.radius() + i;
                        	mSensPol.centr[NC][1] = Y - onOff.radius() + j;

                            NC = NC + 1;
                            break;
						default:
							break;
                		}
                    }
                }
        	}
        }
    }
	
	//constants
    //минимальное соотношение средней контрасности переферии и центра сенсорного поля, необходимое для активации
    //контрастность для темных элементов (0)
    public static double KContr1 = 1.45;
    //контрастность для светлых элементов (Level_Bright)
    public static double KContr2 = 1.15;
	//минимальная контрастность
    public static double KContr3 = 1.15;
	//(0..255)
    public static int Level_Bright = 100;
    public static int Lelel_min = 10;// * N_Col;

    public void process(BufferedImage image) {

    	//preprocessing
    	for (int x = 0; x < width; x++)
        	for (int y = 0; y < height; y++)
        		preprocessed[x][y] = Utils.calcGrey(image, x, y);
        
    	double SP, SC, SA;
        double K_cont;
        for (int ix = 0; ix < NL.width(); ix++) {
        	for (int iy = 0; iy < NL.height(); iy++) {

        		OnOffReceptiveField mSensPol = sensorField[ix][iy];

                NL.set(ix,iy,false);

                SC = 0;
                for (int j = 0; j < onOff.numInCenter(); j++) {

                    SC += preprocessed[mSensPol.centr[j][0]][mSensPol.centr[j][1]];
                }

                SP = 0;
                for (int j = 0; j < onOff.numInPeref(); j++) {

                    SP += preprocessed[mSensPol.peref[j][0]][mSensPol.peref[j][1]];
                }

                SA = ((SP + SC) / (onOff.numInCenter() + onOff.numInPeref()));

                K_cont = KContr1 + SA * (KContr2 - KContr1) / Level_Bright;

                if (K_cont < KContr3) K_cont = KContr3;

                SC = SC / onOff.numInCenter();
                SP = SP / onOff.numInPeref();

                if (SA > Lelel_min) {
                	switch (mSensPol.type) {
                    case 1:

                        if (SC / SP > K_cont)
                        	NL.set(ix,iy,true);

						break;

                    case 2:

                        if (SP / SC > K_cont)
                        	NL.set(ix,iy,true);

                        break;

                    case 3:

                        if (SC / SP > K_cont || SP / SC > K_cont) 
                        	NL.set(ix,iy,true);

                        break;
					default:
						break;
					}
                }
        	}
        }
    }
    
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

    public BufferedImage getImage() {
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int c = preprocessed[x][y];
                image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
    }

}