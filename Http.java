import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class Http 
{
    // receives request from the browser via socket and changes it to HTTP request to website
    public static String get_request_from_browser(Socket socket)
    {
        String request = new String();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.isBlank()) break;
                if (!  ((line.contains("Encoding")) || (line.startsWith("Connection:")))  )
                {
                    request = request + line + "\r\n";
                } 


                if (line.contains("HTTP"))
                {
                    System.out.println("Time received request form browser:" + Calendar.getInstance().getTime().toString());
                }
            

            }
        }
        catch (Exception exeption)
        {
            System.out.println("Failed to get request from browser.\n" + exeption.getMessage());
        }
        //System.out.println("Original request from browser:" + request);



        return request;
    }

    // edir original reguest from browser
    public static String edit_request_from_browser(String request)
    {
        request = request.replaceAll("localhost:8080", "www.bom.gov.au");
        request = request.replaceAll("HTTP/1.1", "HTTP/1.0");
        request = request.replaceAll("gzip", "identity");
        request = request.replaceAll("keep-alive", "close");
        return request;
    }


    // send request data from web via socket
    public static Boolean send_web_request(Socket socket, String request)  
    {
        Boolean result = false;
        try
        {
            if(!request.startsWith("POST"))
            {
                OutputStream from_socket = socket.getOutputStream();
                PrintWriter to_web = new PrintWriter(from_socket, true);

                to_web.println(request);
                to_web.println();
                to_web.flush(); 
                result =  true;
            }

        }
        catch (IOException exeption)
        {
            System.out.println("Failed to send request to web.\n" + exeption.getMessage());
        }
        return result;
    }


    // recieve response from the website via socket 
    // returns data from web in the custom class
    public static Web_Data get_web_response(Socket socket)
    {
        Web_Data result = new Web_Data();
        try
        {
            InputStream  from_web  = socket.getInputStream();
            DataInputStream reader = new DataInputStream(from_web);
            
            Http_Header header;
            String headers = "";
            
            byte[] payload_bytes = {};
            Integer length_of_content = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                
                // detect headers
                if ((line.startsWith("content-length") || line.startsWith("Content-Length"))) {
                    length_of_content = Integer.parseInt(line.split(":")[1].trim());
                }
                if (line.length() == 0) {
                    break;
                }

                if (line.contains(("HTTP/1.0"))) 
                {
                    System.out.println("Time recieved response from website" + Calendar.getInstance().getTime().toString());
                    System.out.println(line);
                }
                
            
                headers = headers + line + "\r\n";

            }

            header = new Http_Header(headers.toString());
            Filter filter = new Filter();
            // detect html data
            if (header.has_content_text){
                String payloads = "";
    
        
                while ((line = reader.readLine()) != null) {
                    // payload.append(line).append('\n');
                    line = filter.applyLineFilter(line);
                    payloads = payloads + line + "\n";
                }
                payload_bytes = payloads.toString().getBytes();
            }

            else {
                Integer length = 0;
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                int load_size = 128;
                byte[] bytes = new byte[load_size]; 

                // to help load speed we increase payload size dynamically
                Integer multiplier = 2;;
        
                while((length = reader.read(bytes)) != -1){
                    byteArray.write(bytes, 0, length);
                    bytes = new byte[load_size * multiplier]; 
                    multiplier = Math.min(multiplier+2,64);
                }
                payload_bytes = byteArray.toByteArray();
                byteArray.close();
            }

            // LOG
            System.out.println("Cities names changed:" + filter.capitals_replaced);
            System.out.println("Links changed:" + filter.links_replaced);


            reader.close();
            from_web.close();

            result =  new Web_Data(header, payload_bytes);
        }
        catch (IOException exeption)
        {
            System.out.println("Failed to get response from the web.\n" + exeption.getMessage());
        }
        return result;
    }

    // gives response from web to the browser via scket
    public static void pass_response_to_browser(Socket socket, String header, byte[] content) throws IOException 
    {
        try
        {
            OutputStream stream = socket.getOutputStream();

            stream.write(header.getBytes());
            stream.flush();
            
            stream.write("\r\n".getBytes());
            stream.flush();

            stream.write(content);
            stream.flush();

            stream.write("\r\n\r\n".getBytes());
            stream.flush();
        }
        catch (IOException exeption)
        {
            System.out.println("Failed to pass response to browser.\n" + exeption.getMessage());
        }
    }
}
