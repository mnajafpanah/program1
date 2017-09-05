/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

    private Socket socket;
    String code;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   String HTML;
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      HTML = readHTTPRequest(is);
      //readHTTPRequest(is);
      writeHTTPHeader(os,"text/html");
      writeContent(os, HTML);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}//end run

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
    
    System.out.println("in readHTTPRequest");
  
    String line;
    String HTMLString = "";
    
    Date d = new Date();
    DateFormat DFormat = DateFormat.getDateTimeInstance();
    try{
       
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        line = r.readLine();
        String L[] = line.split(" ");
        String FPath = L[1].substring(1);
        BufferedReader F = new BufferedReader(new FileReader(FPath));
        
        while(true) {
            try{
                while (!r.ready()) {
                    
                    Thread.sleep(1);
                    
                }//end while
                
                line = F.readLine();
                if(line.equals("<cs371date>")) {
                    line = DFormat.format(d);
                }
                if(line.equals("<cs371server>")) {
                    line = "Mohammad Najaf-Panah's WebServer for CS371 course";
                }
                //if(!line.equals(null)) {
                HTMLString += line;
                //}
                //if(line.length() == 0) {
                  //  break;
                //}
            }catch(Exception e){
                System.err.println("Request error: " + e);
                break;
            }
            
            
        }//end while
        
        code = "200";
    }catch(Exception e) {
        code = "404";
        System.err.println("Error:" + e);

    }
    
    System.out.println("exiting");
    return HTMLString;
    
}// end readHTTPRequest

    
    
/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
    System.out.println("in writeHTTPheader");

    Date d = new Date();
   
    DateFormat df = DateFormat.getDateTimeInstance();
   
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
   
    os.write(("HTTP/1.1 " + code + " OK\n").getBytes());
   
    os.write("Date: ".getBytes());
   
    os.write((df.format(d)).getBytes());
   
    os.write("\n".getBytes());
   
    os.write("Server: Jon's very own server\n".getBytes());
   
    //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   
    //os.write("Content-Length: 438\n".getBytes());
   
    os.write("Connection: close\n".getBytes());
   
    os.write("Content-Type: ".getBytes());
   
    os.write(contentType.getBytes());
   
    os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   
    return;
    
}//end writeHTTPHeader

    
    
/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String HTML) throws Exception
{
   
    System.out.println("in writecontent");
    System.out.println(HTML);
    // show html code (from requested file)
    if(code == "200") {
        
        os.write(HTML.getBytes());
                 
    } else{ //display 404 message
        
        os.write("<html><head></head><body>\n".getBytes());
        
        os.write("<h3>404 Not Found</h3>\n".getBytes());
   
        os.write("</body></html>\n".getBytes());
    }
                 
}//end writeContent

} // end class
