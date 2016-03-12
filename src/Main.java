import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.utils.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.awt.image.DataBufferByte;


public class Main{
	
	static int w = 0;

	static int h = 0;
	
	static ByteArrayOutputStream bos = null;
	
	public static JFrame frame = new JFrame();
	public static JLabel lbl = new JLabel();
	
	public static void show_Img(Image img){
		ImageIcon icon = new ImageIcon(img);
		lbl.setIcon(icon);
  	  	frame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
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
 
	//For use in JPanel, need the main class to exten JPanel
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
		 
	  frame.setVisible(true);
	  frame.setLayout(new FlowLayout());
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	 
		 
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
	    bos = new ByteArrayOutputStream(); 
	    
	    while ((i = dataInputStream.read()) > -1)
	    	bos.write(i);
	    
	    bos.flush();
	    
	    //Show the image
	    byte[] b = bos.toByteArray();
	    //Mat mat_img = byteArray_to_Mat(b);  //For processing images from saved files on Android
	    //Image img = Mat_to_BufferedImage(mat_img);
	    System.out.println("Frame received!! " + String.valueOf(b.length));
	    
	    BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
	    byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
	    Mat mat_img = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3); //Height/width reversed
	    mat_img.put(0, 0, pixels);
	    
	    Image img2 = Mat_to_BufferedImage(mat_img);
	    
	    show_Img(img2);
	    
	    
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
}