import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.*;
import org.opencv.core.MatOfPoint;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.awt.image.DataBufferByte;


public class Main {
		
	//Color blob detection variables
	private static Mat mFrame;
    private static boolean              mIsColorSelected = false;
    private static Scalar mBlobColorRgba;
    private static Scalar               mBlobColorHsv;
    private static ColorBlobDetector    mDetector;
    private static Mat                  mSpectrum;
    private static Size SPECTRUM_SIZE;
    private static Scalar               CONTOUR_COLOR;
    static int rows = 0;
	static int cols = 0;
	
	static int command1 = 11, command2 = 22, parameter1 = 1, parameter2 = 2;
	
	static ByteArrayOutputStream bos = null;
	
	public static JFrame frame = new JFrame();
	public static JLabel lbl = new JLabel();
	
	public static void show_Img(Image img){
		ImageIcon icon = new ImageIcon(img);
		lbl.setIcon(icon);
  	  	frame.setSize(img.getWidth(null) + 100, img.getHeight(null) + 100);
  	  	frame.add(lbl);
  	  	frame.revalidate();
  	  	frame.repaint();
	}
	
	public static Image Mat_to_BufferedImage(Mat m){
		
		int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
		
		return image;
	}
	
	//The bytes must be from image files (not Android bitmap object)
	public static Mat byteArray_to_Mat(byte [] bytes){
		Mat m = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
		//Mat m = new Mat(w, h, CvType.CV_8UC3);
		//m.put(0, 0, bytes);
		return m;
	}
	
	public static Image byteArray_to_Image(byte [] bytes){
		BufferedImage img = null;
		try{
			img = ImageIO.read(new ByteArrayInputStream(bytes));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return img;
	}
 
	//For use in JPanel, need the main class to extend JPanel
	/*public void paint(Graphics g) {
	      Image img = createImg();
	      g.drawImage(img, 20,20,this);
	   }
	
	private Image createImg(){
		BufferedImage BImg = null;
		try{
			//BImg = ImageIO.read(new File("E:/socket/ball.jpg"));
			BImg = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		return BImg;
	} */
	
	 public static void main(String[] args){
	  System.loadLibrary(Core.NATIVE_LIBRARY_NAME);	 
	  
	  //Initialization
	  mFrame = new Mat();
	  mSpectrum = new Mat();
	  mDetector = new ColorBlobDetector();
	  mBlobColorHsv = new Scalar(255);
	  mBlobColorRgba = new Scalar(255);
	  SPECTRUM_SIZE = new Size(200, 64);
	  CONTOUR_COLOR = new Scalar(255,0,0,255);
	  
	  MouseAdapter MA = new MouseAdapter() {
		  @Override 
		    public void mousePressed(MouseEvent e) {
			  mIsColorSelected = false;
			  
			  int x = e.getX() - 50;
			  int y = e.getY() - 41;
			  System.out.println("Clicked!!!!!" + " " + x + " " + y);
			  if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return;

		        org.opencv.core.Rect touchedRect = new org.opencv.core.Rect();

		        touchedRect.x = (x>4) ? x-4 : 0;
		        touchedRect.y = (y>4) ? y-4 : 0;

		        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
		        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

		        Mat touchedRegionRgba = mFrame.submat(touchedRect);

		        Mat touchedRegionHsv = new Mat();
		        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

		        // Calculate average color of touched region
		        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		        int pointCount = touchedRect.width*touchedRect.height;
		        for (int i = 0; i < mBlobColorHsv.val.length; i++)
		            mBlobColorHsv.val[i] /= pointCount;

		        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
		        mDetector.resetStart();
		        mDetector.setHsvColor(mBlobColorHsv);

		        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

		        mIsColorSelected = true;
		    } 
	  };
	  
	  frame.setVisible(true);
	  frame.setLayout(new FlowLayout());
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	 
	  frame.addMouseListener(MA);
		 
	  ServerSocket serverSocket = null;
	  Socket socket = null;
	  DataInputStream dataInputStream = null;
	  DataOutputStream dataOutputStream = null;
	  
	  try {
	   serverSocket = new ServerSocket(8080);
	   System.out.println("Listening :8080");
	   System.out.println(serverSocket.getInetAddress());
	   System.out.println(serverSocket.getLocalSocketAddress());
	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	  
	  while(true){
	   try {
		int i = 0;
	    socket = serverSocket.accept();
	    
	    dataInputStream = new DataInputStream(socket.getInputStream());
	    //BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("E:/socket/ball.jpg"));
	    
	    //Read image frame size first
	    long size = dataInputStream.readLong();
	    System.out.println("Size: " + String.valueOf(size));
	    //Read parameters from client
	    parameter1 = dataInputStream.readInt();
	    parameter2 = dataInputStream.readInt();
	    System.out.println("par1 & par2: " + String.valueOf(parameter1) + " " + String.valueOf(parameter2));
	    
	    //Read image bytes
	    bos = new ByteArrayOutputStream(); 
	    int bytes_read = 0;
	    byte[] buffer = new byte[1024];
	    while (size > 0 && (bytes_read = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1){
	    	bos.write(buffer, 0, bytes_read);
	    	size = size - bytes_read;
	    }
	    bos.flush();
	    
	    //Process and show the image
	    byte[] b = bos.toByteArray();
	    //Mat mat_img = byteArray_to_Mat(b);  //For processing images from saved files on Android
	    System.out.println("Frame received!! " + String.valueOf(b.length));
	    
	    BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
	    byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
	    Mat mat_img = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3); //Height/width reversed
	    mat_img.put(0, 0, pixels);
	    
	    mat_img.convertTo(mat_img, -1, 2, 0);
	    
	    //Image processing
	    mat_img.copyTo(mFrame);
	    rows = mFrame.rows();
	    cols = mFrame.cols();
	    if (mIsColorSelected) {
            //Show the error-corrected color
            mBlobColorHsv = mDetector.get_new_hsvColor();
            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

            mDetector.process(mFrame);
            List<MatOfPoint> contours = mDetector.getContours();
            Imgproc.drawContours(mFrame, contours, -1, CONTOUR_COLOR,2);

            Mat colorLabel = mFrame.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mFrame.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }
	    
	    Image img2 = Mat_to_BufferedImage(mFrame);
	    show_Img(img2);
	    
	    //Send command to client
	    dataOutputStream = new DataOutputStream(socket.getOutputStream());
	    dataOutputStream.writeInt(command1);
	    dataOutputStream.writeInt(command2);
	   
	    
	    /*JFrame frame = new JFrame();
	    frame.getContentPane().add(new Main());
	
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(500,500);
	    frame.setVisible(true); */
	    
	    /*dataInputStream = new DataInputStream(socket.getInputStream());
	    System.out.println("ip: " + socket.getInetAddress());
	    System.out.println("message: " + dataInputStream.readUTF());    
	        
	    dataOutputStream = new DataOutputStream(socket.getOutputStream());
	    count++;
	    dataOutputStream.writeUTF("Hello Client !!!!!!" + String.valueOf(count));
	    dataOutputStream.flush();  */
	   } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	   finally{
	    if( socket!= null){
	     try {
	      socket.close();
	     } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     }
	    }
	    
	    if( dataInputStream!= null){
	     try {
	      dataInputStream.close();
	     } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     }
	    }
	    
	    if( dataOutputStream!= null){
	     try {
	      dataOutputStream.close();
	     } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     }
	    }
	    if (bos != null){
	    	try{
	    		bos.close();
	    	}
	    	catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }
	    if (socket != null){
	    	try{
	    		socket.close();
	    		socket = null;
	    	}
	    	catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
	   }
	  }
	 }

	 private static Scalar converScalarHsv2Rgba(Scalar hsvColor) {
	     Mat pointMatRgba = new Mat();
	     Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
	     Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

	     return new Scalar(pointMatRgba.get(0, 0));
	 }
}