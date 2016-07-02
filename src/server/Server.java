package server;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

public class Server extends Thread{

	private Socket s;
	private BufferedReader in;
	private DataOutputStream out;

	public Server(Socket client) {
		s = client;
		try {
			out = new DataOutputStream(s.getOutputStream());
			in = new BufferedReader(new InputStreamReader (s.getInputStream()));
		} catch (IOException e) {}
	}

	public void run() {
		try {			
			String inputData = in.readLine();
			String[] input = inputData.split(" "); //method + query
			if (!input[0].equals("GET")) {
				respond("<b>Error 404. Resource not found.</b>", false);
			}
			else{
				if (!input[1].equals("/")) {
					String file = URLDecoder.decode(input[1].replaceFirst("/", ""),"UTF-8");
					if (new File(file).isFile()){
						respond(file, true);
					}
					else {
						respond("<b>Error 404. Resource not found.</b>", false);
						System.out.println("ol");
					}
				} else {
					String output = "";
					output += "<b>HTTP Server:</b><BR>";
					while (in.ready()){
					    output += inputData + "<BR>";
					    inputData = in.readLine();
					}
					respond(output, true);
				}
			}
		} catch (Exception e){}
	}

	public void respond(String response, boolean ok) throws IOException {		
		if (ok)
			out.writeBytes("HTTP/1.1 200 OK\r\n");
		else
			out.writeBytes("HTTP/1.1 404 Not Found\r\n");
		out.writeBytes("Server: Java HTTPServer");				
		String contentType = "Content-Type: text/html\r\n";
		String contentLength = "Content-Length: ";
		if (new File(response).isFile()){
			contentLength += Integer.toString((new FileInputStream(response)).available()) + "\r\n";
			if (!response.endsWith(".htm") && !response.endsWith(".html")){
				if(response.endsWith(".png")){
					contentType = "Content-Type: image/png\r\n";
				}else if(response.endsWith(".jpg")){
					contentType = "Content-Type: image/jpg\r\n";
				}else if(response.endsWith(".gif")){
					contentType = "Content-Type: image/gif\r\n";
				}else if(response.endsWith(".swf")){
					contentType = "Content-Type: application/x-shockwave-flash\r\n";
				}else if(response.endsWith("mp4")){
					contentType = "Content-Type: video/mp4 \r\n";
				}
			}
		}
		else {
			response = "<html><head></head><body>" + response + "</body></html>";
			contentLength += response.length() + "\r\n";
			contentType = "Content-Type: text/html\r\n";
		}		
		out.writeBytes(contentType);
		out.writeBytes(contentLength);
		out.writeBytes("\r\n");
		if (new File(response).isFile()){
			FileInputStream in = new FileInputStream(response);
			byte[] buffer = new byte[1];
			while ((in.read(buffer)) != -1 ) {
				out.write(buffer);
			}
			in.close();
		}
		else out.writeBytes(response);
		out.writeBytes("Connection: close\r\n");
		out.close();
	}

	public static void main (String args[]){
		try{
			ServerSocket ss = new ServerSocket (2222, 10, InetAddress.getByName("127.0.0.1"));
			System.out.println("Server started at 127.0.0.1:2222. Access 127.0.0.1:2222/oldal.html");
			while(true) {
				(new Server(ss.accept())).start();
			}
		}catch(IOException e){
			System.out.println("Connection error!");
		}
	}
}
