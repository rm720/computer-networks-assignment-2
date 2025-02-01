import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    public static void main(String[] args) 
    {
        
        // proxy server socket for browser
        try(ServerSocket proxy_socket = new ServerSocket(8080))
        {
            while (true) 
            {
                // listen on socket proxy_socket for browser_socket
                try
                {
                    Socket browser_socket = proxy_socket.accept();
                    My_Thread thread = new My_Thread(browser_socket);
                    thread.start();
                }
                catch (Exception exeption)
                {
                    System.out.println("Proxy Failed #2.\n" + exeption.getMessage());
                }
            }
        }
        catch (Exception exeption)
        {
            System.out.println("Proxy Failed #1.\n" + exeption.getMessage());
        }
    }
}

// class run on thread
class My_Thread extends Thread
{
    private final Socket browser_socket;

    public My_Thread(Socket browser_socket)
    {
        this.browser_socket = browser_socket;   
    }


    @Override
    public void run()
    {
        String browser_request = new String();
        browser_request = Http.get_request_from_browser(browser_socket);
        browser_request = Http.edit_request_from_browser(browser_request);
        
        // setup socet for web site
        try (Socket web_socket = new Socket("www.bom.gov.au", 80)) 
        {
            if (Http.send_web_request(web_socket, browser_request))
            {
                Web_Data data = Http.get_web_response(web_socket);
                web_socket.close();
                Http.pass_response_to_browser(browser_socket, data.get_header().toString(), data.get_payload());
                browser_socket.close();
            } 
        }
        catch (Exception exeption)
        {
            System.out.println("Proxy Failed #3.\n" + exeption.getMessage());
        }
    }

}




