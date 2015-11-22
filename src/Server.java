import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.binary.Base64;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Server {
	final static String apikey = "c98bbe63-3889-4600-a59a-075bf7261704";
	public static String[] foods= {"yogurt","fruit", "bagel", "cream", "cheese", "strawberry", "salt", "sugar", 
		"rice", "milk", "ramen", "beef", "buns", "soy", "fruit", "gum", "potato", "chips", "apples", "soda", "soup"};
    public static String[] clothes = {"jean", "tshirt", "jacket", "hoodie", "hat", "leggings"};
    public static double clothesamount = 0.0;
    public static double foodamount = 0.0;
    public static double otheramount = 0.0;
    
    //public static String info = "GREAT AMERICAN BAGEL\n17801 International Blvd October 19. 2015\nSeaTac, WA 98158 2:10 PM\n(206) 433; 2717 Analisa\nAuthorization 019444\nReceipt crCR\nYogurt $2.49\nBagel Cream Cheese $4.34\nAsiago\nFresh Fruit 51.29\nSubtotal $8.12\nSales Tax $0.77\nTotal $8.89\nH Visa 4108 (Swipe) $8.89";
//    public static String info = "GREAT AMERICAN BAGEL\n17801 International Blvd October 21. 2015\nSeaTac, WA 98158 9:09 AM\n(206) 433 2717 Analisa\nAuthorization 021992\nReceipt EetW\nBagel Cream Cheese $4.34\nAsiago\nSoup $5.99\nYogurt Parfalt $3.99\nSubtotal $14.32\nSales Tax $1.36\nTotal $15.68\nVisa 4108 (Swine) $15.68";
    
    public static String getJSONString(JSONObject json) {
		JSONArray text_block = null;
		String text_string = null;
		
		try {
			text_block = (JSONArray) json.get("text_block");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			JSONObject text = (JSONObject) text_block.getJSONObject(0);
			text_string = text.getString("text");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return text_string;
	}
	
	
	public static boolean charIsNumber(char ch) {
		if (ch >= 48 && ch <= 57) {
			return true;
		}
		return false;
	}
	
	//48 to 57
	public static double getPrice(String line) {
		String result = "";
		int pd_index = line.indexOf(".");
		if (pd_index != -1) {
			if (pd_index > 0 && pd_index < line.length() - 2) {
				char ch_after1 = line.charAt(line.indexOf(".") + 1);
				char ch_after2 = line.charAt(line.indexOf(".") + 2);
				if (charIsNumber(ch_after1) && charIsNumber(ch_after2)) {
					result += ".";
					result = result + ch_after1;
					result = result + ch_after2;
					
					boolean flag = true;
					int i = 1;
					while (flag) {
						int char_index = line.indexOf(".") - i;
						if (char_index >= 0) {
							char ch = line.charAt(char_index);
							if (charIsNumber(ch)) {
								result = ch + result;
								i++;
							} else {
								flag = false;
							}
						} else {
							flag = false;
						}
					}
					//System.out.println("result: " + result);
					double val = Double.parseDouble(result);
					return val;
				}
			}
		}
		
		return 0.0;
	}
	//public static String info = "this is a fruit 1.22\nrandom crap 2.34\nthis is a jean 7.00\ntricky strawberry tshirt 125.60";
    public static void processLine(String line)
    {
        line= line.toLowerCase();
        System.out.printf(" line: %s\n", line);
        double price;
        String isClothes = null;
        String isFood = null;
        if((price = getPrice(line)) >= 0.0)
        {
            for(int i = 0; i < clothes.length; i++)
            {
                if(line.indexOf(clothes[i])>=0)
                {
                    isClothes = clothes[i];
                    clothesamount+=price;
                    break;
                }
            }
            if(isClothes==null) 
            {
                for(int i = 0; i < foods.length; i++)
                {
                    if(line.indexOf(foods[i])>=0)
                    {
                        isFood = foods[i];
                        foodamount+=price;
                        break;
                    }
                }
            }
            if(isClothes==null && isFood==null 
            && line.indexOf("subtotal")<0 && line.indexOf("tax")<0 && line.indexOf("total")<0 
            && line.indexOf("visa")<0 && line.indexOf("cash")<0)
            {
                otheramount+=price;
            }
        }
    }
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
		BufferedImage img = null;
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
//			img = ImageIO.read(client.getInputStream());
		} catch (IOException e){
			System.out.println("PrintWriter failed");
			System.exit(1);
		}
		String inLine = null;
		StringBuilder sb = new StringBuilder();
		
		try{
	//		inLine = in.readLine();
	//		int b = inLine.indexOf('*');
	//		int c = (inLine.indexOf(':') == -1)? inLine.indexOf();

	//		String num = inLine.substring(b, c);
	//		int length = Integer.parseInt(num);
	//		int numLoops = length / inLine.length();
	//		int count = 0;
	//		System.out.println(inLine + " " + length + " " + numLoops);
			while((inLine = in.readLine()) != null){
	//			inLine = in.readLine();
				if(inLine.contains("!")) break;
				int a = inLine.indexOf(':');
	//			System.out.println(inLine);
				if(a > 0)
					sb.append(inLine.substring(a + 1));
				else
					sb.append(inLine);
	//		count++;
			}
			System.out.println("done reading the string");
			InputStream stream = new ByteArrayInputStream(Base64.decodeBase64(sb.toString().getBytes()));
			img = ImageIO.read(stream);
			stream.close();
			File oFile = new File("imgOSF.jpg");
			out.println("LOLOLOLOLOLOLOLOL hi frank whats up");
			
			String workingDir = System.getProperty("user.dir");
			System.out.println(workingDir);
			if(!oFile.exists())
				oFile.createNewFile();
			ImageIO.write(img, "jpg", oFile);
		} catch (IOException e){
			System.out.println("Fork me");
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
		} catch (IOException e2) {
			System.out.println("Riperoni");
			e2.printStackTrace();
		}
		
		JSONObject obj = null;
		try {
			obj = applyOCR();
		} catch (IOException e1) {
			System.out.println("Rip");
			e1.printStackTrace();
		}
		String info = getJSONString(obj);
		
		int start = 0;
        int end = info.indexOf("\n", start+1);
        String line = info.substring(start, end);
        while(start<info.length() && start>=0)
        {
            System.out.printf("start: %d, end: %d, ", start, end, line);
            processLine(line);
            if(end == info.length()) break;
            start = end+1;
            end = info.indexOf("\n", start);
            if(end<0) end = info.length();
            line = info.substring(start, end);
        }
        ServerSocket server2 = null;
		try{
			server2 = new ServerSocket(8001);
		} catch (IOException e){
			e.printStackTrace();
			System.out.println("dun goofed");
			System.exit(-1);
		}
		Socket client2 = null;
		try{
			System.out.println("waiting on accept again...");
			client2 = server.accept();
			System.out.println("Accept has happened again :)");
		} catch (IOException e){
			System.out.println("failed to accept :(");
			System.exit(1);
		}
		PrintWriter out2 = null;
		try {
			out2 = new PrintWriter(client2.getOutputStream(), true);
		} catch (IOException e1) {
			System.out.println("Omg");
			e1.printStackTrace();
		}

        StringBuilder output = new StringBuilder();
        output.append(foodamount + " " + clothesamount + " " + otheramount);
        out2.println(output.toString());
//              System.out.printf("foods: %.2f\n", foodamount);
//        System.out.printf("clothes: %.2f\n", clothesamount);
//        System.out.printf("other: %.2f\n", otheramount);
        
        
		try {
			server2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static JSONObject applyOCR() throws IOException{
		String s = "https://api.havenondemand.com/1/api/sync/ocrdocument/v1";
		
		String file = "imgOSF.jpg";

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
/*		JSONObject j = applyOCR();
		String info = getJSONString(j);
		
		int start = 0;
        int end = info.indexOf("\n", start+1);
        String line = info.substring(start, end);
        while(start<info.length() && start>=0)
        {
            System.out.printf("start: %d, end: %d, ", start, end, line);
            processLine(line);
            if(end == info.length()) break;
            start = end+1;
            end = info.indexOf("\n", start);
            if(end<0) end = info.length();
            line = info.substring(start, end);
        }
        
        System.out.printf("foods: %.2f\n", foodamount);
        System.out.printf("clothes: %.2f\n", clothesamount);
        System.out.printf("other: %.2f\n", otheramount);*/
	}

}
