import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MiniServerSocketExample {
    private static final int PORT = 8080;
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("MiniServer active " + PORT);
            PrintWriter writer = new PrintWriter("src/autentificado.txt", "UTF-8");
            writer.close();
            while (true) {
                new ThreadSocket(server.accept());
            }
        } catch (Exception e) {
        }
    }
}
class ThreadSocket extends Thread {
    private Socket insocket;
    ThreadSocket(Socket insocket) {
        this.insocket = insocket;
        this.start();
    }
    @Override
    public void run() {
        try {
            InputStream is = insocket.getInputStream();
            PrintWriter out = new PrintWriter(insocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            String lineaArchivo;
            line = in.readLine();
            Integer encontreLogin = 0;
            String request_method = line;
            String[] listaPartes = request_method.split(" ");
            System.out.println("HTTP-HEADER: " + line);
            line = "";
            listaPartes[1]=listaPartes[1].replace("?", "");
            System.out.println("==METODO: "+listaPartes[0]+" URL: "+ listaPartes[1]);


            //==========================================================================================================
            //AQUÍ SE EMPIEZAN A FILTRAR EL TIPO DE REQUEST
            //==========================================================================================================

            if (listaPartes[0].equals("GET")) {

                if (listaPartes[1].equals("/home_old")) {

                    out.print("HTTP/1.0 302 Moved Permanently\r\n" +
                            "Location: http://" +
                            insocket.getLocalAddress().getHostAddress() + ":" +
                            insocket.getLocalPort() + "/\r\n\r\n");
                }

                else if (listaPartes[1].equals("/secret")) {

                    try (BufferedReader br = new BufferedReader(new FileReader("src/autentificado.txt"))) {
                        while ((lineaArchivo = br.readLine()) != null) {
                            if (lineaArchivo.equals("Logeo detectado")){
                                encontreLogin = 1;
                                out.println("HTTP/1.0 200 OK");
                                out.println("Content-Type: text/html; charset=utf-8");
                                out.println("Server: T1REDES");
                                out.println("");
                                out.println("<H1>Autentificación Completada</H1>");

                            }
                        }
                        if (encontreLogin == 0){
                            out.println("HTTP/1.0 403 Forbidden");
                            out.println("Content-Type: text/html; charset=utf-8");
                            out.println("Server: T1REDES");
                            out.println("");
                            out.println("<H1>ACCESO DENEGADO</H1>");
                        }
                    }

                }

                else if (listaPartes[1].equals("/login")) {

                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type: text/html; charset=utf-8");
                    out.println("Server: T1REDES");
                    out.println("");

                    try (BufferedReader br = new BufferedReader(new FileReader("src/login.html"))) {
                        while ((lineaArchivo = br.readLine()) != null) {
                            out.println(lineaArchivo);
                        }
                    }

                }
                //CASO DE LA PÁGINA DE INICIO
                else if (listaPartes[1].equals("/")){
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type: text/html; charset=utf-8");
                    out.println("Server: MINISERVER");
                    // este linea en blanco marca el final de los headers de la response
                    out.println("");
                    // Envía el HTML
                    try (BufferedReader br = new BufferedReader(new FileReader("src/home.html"))) {
                        while ((lineaArchivo = br.readLine()) != null) {
                            out.println(lineaArchivo);
                        }
                    }

                }
                //CASO EN EL QUE INTENTEN ACCEDER A URL NO EXISTENTE
                else{
                    out.println("HTTP/1.0 404 Not Found");
                    out.println("Content-Type: text/html; charset=utf-8");
                    out.println("Server: T1REDES");
                    out.println("");
                    out.println("<H1>Error 404 bad request</H1>");
                }
            }

            else if (listaPartes[0].equals("POST")) {
                //======================================================================================================
                //BUSQUEDA DATA POST
                //======================================================================================================
                int postDataI = -1;
                while ((line = in.readLine()) != null && (line.length() != 0)) {
                    System.out.println("HTTP-HEADER: " + line);
                    if (line.indexOf("Content-Length:") > -1) {
                        postDataI = new Integer(
                                line.substring(
                                        line.indexOf("Content-Length:") + 16,
                                        line.length())).intValue();
                    }
                }
                String postData = "";
                //======================================================================================================
                //LECTURA DATA POST
                //======================================================================================================
                if (postDataI > 0) {
                    char[] charArray = new char[postDataI];
                    in.read(charArray, 0, postDataI);
                    postData = new String(charArray);
                }

                //System.out.println("Metodo de entrada " + request_method);
                //System.out.println("Post-> " + postData);

                if (listaPartes[1].equals("/secret")) {
                    String[] campos = postData.split("&");
                    String user = campos[0].split("=")[1];
                    String pass = campos[1].split("=")[1];

                    //LOGEO EXITOSO
                    if (user.equals("admin") && pass.equals("password")){
                        try{
                            PrintWriter writer = new PrintWriter("src/autentificado.txt", "UTF-8");
                            writer.println("Logeo detectado");
                            writer.close();
                        } catch (IOException e) {
                            System.out.println("ERROR: "+e);
                        }

                        out.println("HTTP/1.0 200 OK");
                        out.println("Content-Type: text/html; charset=utf-8");
                        out.println("Server: T1REDES");
                        out.println("");
                        out.println("<H1>Autentificación Completada</H1>");


                    }
                    //LOGGEO FALLIDO
                    else{
                        out.println("HTTP/1.0 403 Forbidden");
                        out.println("Content-Type: text/html; charset=utf-8");
                        out.println("Server: T1REDES");
                        out.println("");
                        out.println("<H1>ACCESO DENEGADO</H1>");
                    }
                }
            }
            out.close();
            insocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}