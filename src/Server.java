import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;


public class Server {
	final static String apikey = "c98bbe63-3889-4600-a59a-075bf7261704";
	
	private static void waitForData(){
		ServerSocket server = null;
		try{
			server = new ServerSocket(8000);
		} catch (IOException e){
			e.printStackTrace();
			System.out.println("dun goofed");
			System.exit(-1);
		}
		Socket client = null;
		try{
			System.out.println("waiting on accept");
			client = server.accept();
			System.out.println("Accept has happened");
		} catch (IOException e){
			System.out.println("failed to accept");
			System.exit(1);
		}
		PrintWriter out = null;
		BufferedReader in = null;
//		BufferedImage img = null;
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
//			img = ImageIO.read(client.getInputStream());
		} catch (IOException e){
			System.out.println("PrintWriter failed");
			System.exit(1);
		}
		int inLine = -1;
		try{
			while((inLine = in.read()) != -1){
				System.out.print(inLine);
			}
		} catch (IOException e){
			System.out.println("Fuck");
		}
/*		File f = new File("img.jpg");
		try{
			ImageIO.write(img, "jpg", f);
		} catch (IOException e){
			System.out.println("img write failed");
			System.exit(1);
		}
*/
		
//		} catch(IOException e){
	//		System.out.println("readlIne failed");
//			System.exit(1); 
	//	}		
		out.close();
		
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static JSONObject applyOCR() throws IOException{
String s = "https://api.havenondemand.com/1/api/sync/ocrdocument/v1";
		
		String file = "img.jpg";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		JSONObject text = null;
		try {
		    HttpPost httppost = new HttpPost(s);
		
		    File f = new File(file);
		    FileBody fileBody = new FileBody(f);
		    StringBody apikeyStringBody = new StringBody(apikey, ContentType.TEXT_PLAIN);
		
		    HttpEntity reqEntity = MultipartEntityBuilder.create()
			    .addPart("file", fileBody)
			    .addPart("apikey", apikeyStringBody)
			    .build();
		
		    httppost.setEntity(reqEntity);
		
		    CloseableHttpResponse response = null;
		    
		    try {
		    	response = httpclient.execute(httppost);
			
		    	System.out.println(response.getStatusLine());
		    	HttpEntity entity = response.getEntity();
			
		    	if (entity != null) {
		    		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		    		StringBuilder sb = new StringBuilder();
		    		String b = null;
		    		while((b = br.readLine()) != null){
		    			sb.append(b);
		    		}
		    		try {
						text = new JSONObject(sb.toString());
					} catch (JSONException e) {
						System.out.println("We done goofed");
						e.printStackTrace();
					}
		    	}
			
		    }catch(ClientProtocolException cpe){
		    	cpe.printStackTrace();
		    }catch(IOException ioe){
		    	ioe.printStackTrace();
		    } finally {
		    	try {
		    		response.close();
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	}
		    }
		} finally {
		    	httpclient.close();
		}
//		System.out.println(text);
		return text;
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		waitForData();
		JSONObject j = applyOCR();
		
	}

}
