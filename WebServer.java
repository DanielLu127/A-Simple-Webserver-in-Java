import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WebServer {

    public static void main(String args[]) throws IOException  {

        ServerSocket server = new ServerSocket(8088);
        while (true)  {
            Socket socket = server.accept();
            RequestHandler newRequest = new RequestHandler(socket);
            newRequest.start();
        }
    }
}


class RequestHandler extends Thread {
    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            dateFormat.format(new Date());

            String line = input.readLine();
            String [] temp = line.split(" ");

            if (!temp[0].equals("GET") || !temp[2].equals("HTTP/1.1")) {
                output.print("HTTP/1.0 400 Bad Request\r\n\r\n");
                output.close();
            }

            String filePath = "C:\\server\\files\\" + temp[1];
            File targetFile = new File(filePath);

            if(!targetFile.exists()) {
                System.out.println("404");
                output.print("HTTP/1.0 404 Not Found\r\n"+ "Content-type: text/html\r\n\r\n"+
                        "<html><head></head><body>HTTP/1.0 404 Not Found<br>"+filePath+" not found</body></html>\n");
                output.close();
                return;
            }

            if (!targetFile.canRead()) {
                System.out.println("403");
                output.print("HTTP/1.0 403 Forbidden Access \r\n"+ "Location: /"+filePath+"\r\n\r\n");
                output.close();
                return;
            }

            String filename = temp[1];
            System.out.println(filename);
            String [] temp1 =  filename.split("\\.");
            String postfix = temp1[1];
            String contentType ="";
            if (postfix.equals("jpg")) {
                contentType = "jpg";
            }
            else if (postfix.equals("png")) {
                contentType = "png";
            }
            else if (postfix.equals("html")) {
                contentType = "html";
            }
            else if (postfix.equals("gif")) {
                contentType = "gif";
            }
            else {
                contentType = "text";
            }


            output.print("HTTP/1.0 200 OK\r\n"+
                    "Date: " + dateFormat + "\r\n" +
                    "Server: my server" + "\r\n" +
                    "Content-type: "+ contentType + "\r\n" +
                    "Content-length: " + targetFile.length() + "\r\n" +
                    "\r\n\r\n");

            FileInputStream fileStream = new FileInputStream(targetFile);

            byte buffer[]=new byte[1000];
            int i;
            while ((i=fileStream.read(buffer))>0) {
                output.write(buffer, 0, i);
            }

            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
