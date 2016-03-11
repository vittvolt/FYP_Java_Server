import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;


public class Main extends JPanel{
	
	static ByteArrayOutputStream bos = null;
 
	public void paint(Graphics g) {
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
	}
	
	 public static void main(String[] args){
	  ServerSocket serverSocket = null;
	  Socket socket = null;
	  DataInputStream dataInputStream = null;
	  DataOutputStream dataOutputStream = null;
	  int count = 0;
	  
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
	    //bos.close();
	    
	    //Show the image
	    JFrame frame = new JFrame();
	    frame.getContentPane().add(new Main());
	
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(500,500);
	    frame.setVisible(true);
	    
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