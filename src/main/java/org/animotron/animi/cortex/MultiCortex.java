/*
 *  Copyright (C) 2012-2013 The Animo Project
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
package org.animotron.animi.cortex;

import static org.jocl.CL.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javolution.util.FastMap;

import org.animotron.animi.Params;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.acts.*;
import org.animotron.animi.cortex.old.LinkQ;
import org.animotron.animi.cortex.old.NeuronComplex;
import org.animotron.animi.gui.Application;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class MultiCortex implements Runnable {

    private static final boolean BENCHMARK = true;

    public static final int STOP = 0;
    public static final int PAUSE = 1;
    public static final int STEP = 2;
    public static final int RUN = 3;

    public static int MODE = STOP;

    private final Application app;
    
    @RuntimeParam(name = "frequency")
	public int frequency = 0; // Hz

    private long frame = 0;
    private long t0 = System.currentTimeMillis();

    private boolean run = true;
    
    public long count = 0;
    
    public Retina retina;

    public CortexZoneSimple z_in;
    
    public CortexZoneComplex z_attention;
    public CortexZoneComplex z_motoric;
    
    public CortexZoneComplex z_1st;
    public CortexZoneComplex z_goriz1;
    public CortexZoneComplex z_2nd;
    public CortexZoneComplex z_goriz2;
    public CortexZoneComplex z_3rd;
//  @Params
//  public CortexZoneSimple z_good;
//  @Params
//  public CortexZoneSimple z_bad;
    
    @Params
    public CortexZoneSimple [] zones;

    private MultiCortex(Application app, CortexZoneSimple [] zones) {
    	this.app = app;
    	this.zones = zones;

        retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
        retina.setNextLayer(zones[0]);
    }

    public MultiCortex(Application app) {
    	this.app = app;
    	
    	preInitCL();
    	
        z_in = new CortexZoneSimple("Зрительный нерв", this);
        
        z_1st = new CortexZoneComplex("1й", this, 120, 120, //160, 120,
            new Mapping[]{
                new Mapping(z_in, 100, 1, false) //7x7 (50)
            }
        );
//        z_1st.tremor = new int[] {
//			0, 0,
//			1, 1,
//			1,-1,
//			2, 0,
//			3, 1,
//			3,-1,
//			4, 0,
//		};



//        z_1st.speed = Integer.MAX_VALUE;
//        z_in.nextLayers(new CortexZoneSimple[] {z_1st});

//        z_attention = new AttentionZone("attention", this, 20, 20,
//            new Mapping[]{
//                new Mapping(z_1st, 80, 1, false)
//            }
//        );
//        z_attention.inhibitory_links = 0;
//        z_attention.speed = Integer.MAX_VALUE;
//
//        z_motoric = new AttentionZone("motoric", this, 2, 2,
//            new Mapping[]{
//                new Mapping(z_attention, 0, 0, false)
//            }
//        );
//        z_motoric.inhibitory_links = 0;
//        z_motoric.speed = Integer.MAX_VALUE;

//        z_goriz1 = new CortexZoneComplex("1st G", this, 20, 20,
//            new Mapping[]{
//                new Mapping(z_1st, 400, 2, false) //20x20 (400)
//            }
//        );
//        z_goriz1.inhibitory_links = 0;
//        z_goriz1.disper = 1;
//
//        z_1st.nextLayers(new CortexZoneSimple[] {z_goriz1});
//
//        z_2nd = new CortexZoneComplex("2nd", this, 50, 50,
//            new Mapping[]{
//                new Mapping(z_1st, 400, 2, false), //20x20
////                new Mapping(z_goriz1, 300, 2, true)
//            }
//        );
//        z_2nd.speed = 4;
//        z_1st.nextLayers(new CortexZoneSimple[] {z_2nd});
//        z_goriz1.nextLayers(new CortexZoneSimple[] {z_2nd});

//        z_3rd = new CortexZoneComplex("3rd", this, 32, 32,
//            new Mapping[]{
//                new Mapping(z_2nd, 300, 5)
//            }
//        );

//        zones = new CortexZoneSimple[]{z_in, z_1st, z_goriz1, z_2nd};
        zones = new CortexZoneSimple[]{z_in, z_1st};
        
        retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
        retina.setNextLayer(z_in);
    }
    
    public Map<String, cl_platform_id> platforms = new FastMap<String, cl_platform_id>();

    /** 
     * The OpenCL context.
     */
    public cl_context context;

    /**
     * The number of OpenCL devices that may be used.
     */
    private int numDevices;

    /**
     * The OpenCL command queues.
     */
    private cl_command_queue commandQueues[];

    /**
     * The OpenCL kernels which will actually compute.
     */
    private Map<Class<? extends Task>, cl_kernel> kernels[];

    /**
     * The {@link TaskProcessor}s which will execute the tasks for computing kernels.
     */
    private TaskProcessor taskProcessors[];

    /**
     * The queue of {@link Task}s that are about to be executed by the {@link TaskProcessor}.
     */
    public BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<Task>(1024, true);
    
    public void addTask(Task task) throws InterruptedException {
    	if (kernels != null && kernels.length > 0) {
    		taskQueue.put(task);
    	} else {
    		task.execute();
    	}
    }

    /**
     * Initialize OpenCL
     */
    private void preInitCL() {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id _platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(_platforms.length, _platforms, null);
        
        for (cl_platform_id plat : _platforms) {
        	platforms.put(getPlatformInfoString(plat, CL.CL_PLATFORM_NAME), plat);
        }
//            int numDevices[] = new int[1];
//            clGetDeviceIDs(plat, CL_DEVICE_TYPE_ALL, 0, null, numDevices);
//            
//            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
//            clGetDeviceIDs(plat, CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);
//
//            long dType = getLong(devicesArray[0], CL_DEVICE_TYPE);
//	        if( (dType & CL_DEVICE_TYPE_GPU) != 0)
//	        	platform = plat;
//        }
    }

    /**
     * Initialize OpenCL
     */
    private void initCL(final cl_platform_id platform, final long deviceType) {
		System.out.println("Using plaform "+getPlatformInfoString(platform, CL.CL_PLATFORM_NAME));

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        numDevices = numDevicesArray[0];
        
        // Obtain the device IDs 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        
        for (int i=0; i < numDevices; i++) {
            System.out.println("Device " + i + ": " + getDeviceInfoString(devices[i], CL.CL_DEVICE_NAME));
        }

        // Create a context for the selected devices
        context = clCreateContext(
            contextProperties, numDevices, devices, 
            null, null, null);
        
        // Read the kernel files and set up the OpenCL program
        String[] sources = new String[] {
    		readFile("kernels/Retina.cl"),
    		readFile("kernels/CNActivation.cl"),
    		readFile("kernels/Inhibitory.cl"),
    		readFile("kernels/WinnerGetsAll.cl"),
    		readFile("kernels/Memorization.cl"),
    		readFile("kernels/Restructurization.cl")
		};
        cl_program program = clCreateProgramWithSource(context, sources.length, sources, null, null);
        clBuildProgram(program, 0, null, "-cl-mad-enable", null, null);

        // Create a the command-queues and kernels
        commandQueues = new cl_command_queue[numDevices];
        kernels = new Map[numDevices];
        long properties = 0;
        if (BENCHMARK) {
            properties |= CL_QUEUE_PROFILING_ENABLE;
        }
        for (int i=0; i < numDevices; i++) {
            commandQueues[i] = clCreateCommandQueue(context, devices[i], properties, null);
            kernels[i] = new FastMap<Class<? extends Task>, cl_kernel>();
//            kernels[i].put(Retina.RetinaTask.class, clCreateKernel(program, "computeRetina", null));
            kernels[i].put(CNActivation.class, clCreateKernel(program, "computeActivation", null));
            kernels[i].put(Inhibitory.class, clCreateKernel(program, "computeInhibitory", null));
            kernels[i].put(WinnerGetsAll.class, clCreateKernel(program, "computeWinnerGetsAll", null));
            kernels[i].put(Memorization.class, clCreateKernel(program, "computeMemorization", null));
            kernels[i].put(Restructurization.class, clCreateKernel(program, "computeRestructurization", null));
        }
        
        // Start the task processors
        taskProcessors = new TaskProcessor[numDevices];
        for (int i=0; i<numDevices; i++)
        {
            taskProcessors[i] = new TaskProcessor(kernels[i], commandQueues[i]);
            Thread thread = new Thread(taskProcessors[i], "taskProcessorThread"+i);
            thread.setDaemon(true);
            thread.start();
        }

        /* Get Device specific Information */
        int status[] = new int[1];

        int maxWorkGroupSize[] = new int[1];
        int maxDimensions[] = new int[1];
        long maxWorkItemSizes[] = new long[1];
        long totalLocalMemory[] = new long[1];

        status[0] = clGetDeviceInfo(
            devices[0], CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t,
            Pointer.to(maxWorkGroupSize), null);

        status[0] = clGetDeviceInfo(
            devices[0], CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, Sizeof.cl_uint,
            Pointer.to(maxDimensions), null);
        
        maxWorkItemSizes = new long[maxDimensions[0]];
        status[0] = clGetDeviceInfo(
            devices[0], CL_DEVICE_MAX_WORK_ITEM_SIZES, Sizeof.size_t * maxDimensions[0],
                Pointer.to(maxWorkItemSizes), null);

        status[0] = clGetDeviceInfo(
            devices[0], CL_DEVICE_LOCAL_MEM_SIZE, Sizeof.cl_ulong,
            Pointer.to(totalLocalMemory), null);

        System.out.println("Max allowed work-items in a group = "+Arrays.toString(maxWorkGroupSize));
        System.out.println("Max group dimensions allowed = "+Arrays.toString(maxDimensions));
        System.out.println("Max work-items sizes in each dimensions = "+Arrays.toString(maxWorkItemSizes));
        System.out.println("Max local memory allowed = "+Arrays.toString(totalLocalMemory));
        
        // Initialize the BufferedImage and the OpenCL memory
//        initImage(
//            DEFAULT_SIZE_X, DEFAULT_SIZE_Y, 
//            DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
    }
    
    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getLong(cl_device_id device, int paramName)
    {
        return getLongs(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static long[] getLongs(cl_device_id device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }

    private static String getPlatformInfoString(cl_platform_id platform, int paramName) {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }    

    private static String getDeviceInfoString(cl_device_id device, int paramName) {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

    private static String readFile(String fileName) {
    	BufferedReader br = null;
    	try {
            br = new BufferedReader( new InputStreamReader( new FileInputStream(fileName) ) );
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (br != null) {
        		try {
					br.close();
				} catch (IOException e) {}
        	}
        }
        System.exit(1);
        return null;
    }

    /**
     * Flush all pending tasks and finish all running tasks
     */
    public void flush() {
        taskQueue.drainTo(new ArrayList<Task>());
        finish();
    }

    /**
     * Flush all pending tasks and finish all running tasks
     */
    public void finish() {
//        taskQueue.drainTo(new ArrayList<Task>());
        for (int i=0; i<numDevices; i++) {
            clFinish(commandQueues[i]);
            taskProcessors[i].finish();
        }
    }
    
    public void init(final cl_platform_id platform, final long deviceType) {
    	if (deviceType != CL_DEVICE_TYPE_DEFAULT) {
	        // Initialize OpenCL 
	        initCL(platform, deviceType);
    	}

    	// Flush all pending tasks
        flush();

		for (CortexZoneSimple zone : zones) {
			zone.init();
		}
		
        //первоначальные центры кристализации
//		Random rd = new Random();
//    	int x, y, pos, lx, ly;
//
//    	Mapping m = z_1st.in_zones[0];
//
//    	long n = Math.round(z_1st.cols.length * 0.005); // 0.5%
//        for (int i = 0; i < n; i++) {
//        	do {
//	        	x = rd.nextInt(z_1st.width);
//	        	y = rd.nextInt(z_1st.height);
//        	
//	        	//XXX: should be nothing at inhibitory area?
////				for (int i = 0; i < z_1st.number_of_inhibitory_links; i++) {
////				}
//	        	
//        	} while (z_1st.freePackageCols(x, y, 0) > 0);
//        	
//        	System.out.print("-");
//        	
//        	z_1st.freePackageCols(1, x, y, 0);
//
//        	int offset = ((z_1st.width * y) + x) * 2 * m.ns_links;
//        	
//        	int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
//        	int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
//        	
//            for (int l = 0; l < m.ns_links; l++) {
//            	lx = m.linksSenapse[offset + l*2 +0];
//            	ly = m.linksSenapse[offset + l*2 +1];
//            	
//            	minX = Math.min(minX, lx);
//            	minY = Math.min(minY, ly);
//
//            	maxX = Math.max(maxX, lx);
//            	maxY = Math.max(maxY, ly);
//            }
//            
//            int centerX = minX + (maxX - minX) / 2;
////            int centerY = minY + (maxY - minY) / 2;
//            
//            int wOffset = (((((z_1st.width * y) + x) * z_1st.package_size) + 0) * m.ns_links);
//            
//            float sumW = 0;
//            int count = 0;
//
//        	for (int l = 0; l < m.ns_links; l++) {
//            	lx = m.linksSenapse[offset + l*2 +0];
////            	ly = m.linksSenapse[offset + l*2 +1];
//            	
//            	int w = 0;
//            	
//            	//make vertical line
//            	if (lx >= centerX &&  lx <= centerX) { // && ly >= centerY - 1 &&  ly <= centerY + 1) {
//            		w = 1;
//            	}
//                m.linksWeight[wOffset + l] = w;
//
//				sumW += w;
//
//				if (w > 0.0f)
//				{
//					count++;
//				}
//        	}
//
//        	//normalization
//    	    for(int l = 0; l < m.ns_links; l++)
//    	    {
//    	    	if (m.linksWeight[wOffset + l] == 0.0f)
//    	    	{
//    	    		m.linksWeight[wOffset + l] = -1 / (float)(count * 0.5);
//        		}
//        		else
//        		{
//        			m.linksWeight[wOffset + l] = m.linksWeight[wOffset + l] / sumW;
//    			}
//    		}
//        }

    }
	
	@Override
	public void run() {
        while (run) {
			try {
				if (paused) {
					synchronized (this) {
						this.wait();
					}
				}
                long t = System.currentTimeMillis();
				
				process();
                
                if (frequency != 0) {
                    t = (1000 / frequency) - (System.currentTimeMillis() - t);

                    if (t > 0)
                    	Thread.sleep(t);
                    else
                    	//give some rest any way
                    	Thread.sleep(5);

                } else {
                	//give some rest any way
                	Thread.sleep(5);
                }

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
                frame++;
                long t = System.currentTimeMillis();
                long dt = t - t0;
                if (dt > 1000) {
                    app.fps = 1000 * frame / dt;
                    frame = 0;
                    t0 = t;
                }
            }
		}
	}
	
	public void process() {
        if (MODE >= STEP) {
        	retina.process(app.getStimulator());
        	
    		for (CortexZoneSimple zone : zones) {
    			zone.process();
    		}
    		count++;
    		
    		if (MODE == STEP) {
    			MODE = STOP;
        		app.count.setText(String.valueOf(count));
        		app.refresh();
    		
    		} else if (MODE == RUN && count % 10 == 0) {
        		app.count.setText(String.valueOf(count));
    			app.refresh();

    		}
        }
	}
	
	private Thread th = null;
	private volatile boolean paused = false;
	
	public synchronized void start() {
		if (th != null) {
			th.interrupt();
		}
		
		MODE = RUN;

		run = true;
		paused = false;
		
		th = new Thread(this);
		th.setDaemon(true);
		th.start();
	}

	public synchronized void stop() {
		MODE = (MODE == RUN) ? PAUSE : STOP;

		run = false;
		try {
			th.join();
		} catch (InterruptedException e) {
			th.interrupt();
		}
		paused = true;
		th = null;
	}

	public void resume() {
		paused = false;
		synchronized (this) {
			notifyAll();
		}
	}

    public void save(Writer out) throws IOException {
    	out.write("<cortex>");
		for (CortexZoneSimple zone : zones) {
			zone.save(out);
		}
    	out.write("</cortex>");
    }


	public static MultiCortex load(Application app, File file) throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			SAXPars saxp = new SAXPars(app);
	
			parser.parse(file, saxp);
			
			return saxp.mc;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static class SAXPars extends DefaultHandler { 
		
		Application app;
		
		MultiCortex mc = null;
		List<CortexZoneSimple> zones = new ArrayList<CortexZoneSimple>();
		
		CortexZoneSimple prevZone = null;
		CortexZoneSimple zone = null;
		
		double fX, fY = 0;
		
		NeuronComplex cn = null;
		
		List<Mapping> mappings = new ArrayList<Mapping>();
		
		public SAXPars(Application app) {
			this.app = app;
		}
		
		@Override
	    public void startElement (String uri, String localName, String qName, Attributes attrs) {
			if ("linkS".equals(qName)) {
				
				new LinkQ(
						prevZone.getCol(
							Integer.valueOf(attrs.getValue("sX")),
							Integer.valueOf(attrs.getValue("sY"))
						),
						cn, 
						Double.valueOf(attrs.getValue("w")),
						fX,
						fY,
						Double.valueOf(attrs.getValue("speed"))
					);

			} else if ("linkI".equals(qName)) {
				
//				new Link(
//						zone.getCol(
//							Integer.valueOf(attrs.getValue("sX")),
//							Integer.valueOf(attrs.getValue("sY"))
//						),
//						cn, 
//						Double.valueOf(attrs.getValue("w")),
//						LinkType.INHIBITORY
//					);
				
				
			} else if ("cn".equals(qName)) {
				cn = zone.getCol(
							Integer.valueOf(attrs.getValue("x")),
							Integer.valueOf(attrs.getValue("y"))
						);
			
			} else if ("mapping".equals(qName)) {
				mappings.add(
					new Mapping(
						prevZone,
						Integer.valueOf(attrs.getValue("number-of-synaptic-links")),
						Double.valueOf(attrs.getValue("synaptic-links-dispersion")),
						Boolean.valueOf(attrs.getValue("soft"))
					)
				);
				
			} else if ("zone".equals(qName)) {
				if ("complex".equals(attrs.getValue("type"))) {
					CortexZoneComplex cZone;
					zone = cZone = new CortexZoneComplex();

					cZone.count = Integer.valueOf(attrs.getValue("count"));
					
				} else {
					zone = new  CortexZoneSimple();
				}
				zone.id = attrs.getValue("id");
				zone.name = attrs.getValue("name");
				zone.width = Integer.valueOf(attrs.getValue("width"));
				zone.height = Integer.valueOf(attrs.getValue("height"));
				zone.active = Boolean.valueOf(attrs.getValue("active"));
				zone.learning = Boolean.valueOf(attrs.getValue("learning"));
				
				zone.init();

				if (prevZone != null) {
					fX = prevZone.width() / (double) zone.width();
					fY = prevZone.height() / (double) zone.height();
				}
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if ("zone".equals(qName)) {
				prevZone = zone;
				zones.add(zone);
				
				if (zone instanceof CortexZoneComplex) {
					((CortexZoneComplex) zone).in_zones = 
						mappings.toArray(new Mapping[mappings.size()]);
				}
				mappings.clear();
				
				zone = null;
			}
		}
		
	    public void endDocument() throws SAXException {
	    	mc = new MultiCortex(app, zones.toArray(new CortexZoneSimple[zones.size()]));
	    }
	}
	
    private class TaskProcessor implements Runnable {
        /**
         * The kernel which will be executed.
         */
        protected Map<Class<? extends Task>, cl_kernel> kernels;
        
        /**
         * The OpenCL command queue.
         */
        protected cl_command_queue commandQueue;

        /**
         * The list of tasks which are currently active.
         */
        private List<Task> activeTasks = Collections.synchronizedList(new ArrayList<Task>());
        
        /**
         * Creates a new TaskProcessor which will execute the
         * given kernel on the given command queue
         * 
         * @param kernel The kernel
         * @param commandQueue The command queue
         */
        public TaskProcessor(Map<Class<? extends Task>, cl_kernel> kernels, cl_command_queue commandQueue) {
            this.kernels = kernels;
            this.commandQueue = commandQueue;
        }
        
        @Override
        public void run() {
            while (true) {
                Task task = null;
                try {
                    task = taskQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                activeTasks.add(task);
                
                task.execute(kernels.get(task.getClass()), commandQueue);
                
                activeTasks.remove(task);
                synchronized (activeTasks) {
                    activeTasks.notifyAll();
                }
//                imageComponent.repaint();

                // Tasks occupy the graphics card -  
                // give it some time to breathe
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        /**
         * Wait until all tasks are finished.
         */
        public void finish() {
            synchronized (activeTasks) {
                while (!activeTasks.isEmpty()) {
                    try {
                        activeTasks.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
