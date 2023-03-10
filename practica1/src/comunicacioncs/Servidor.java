/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */package comunicacioncs;

import static comunicacioncs.Cliente.leerDirectorio;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jorge Ortega
 */
public class Servidor {

    public static ArrayList<String> leerDirectorio(File ruta) {
        File[] archivos = ruta.listFiles();
        ArrayList<String> rutas = new ArrayList<String>();
        for (File archivo : archivos) {
            if (archivo.isFile()) {
//                System.out.println(archivo.getPath());
                rutas.add(archivo.getPath());
            } else if (archivo.isDirectory()) {
                rutas.add(archivo.getPath());
                rutas.addAll(leerDirectorio(archivo));
            }
        }
        return rutas;
    }

    public static void crearCarpetaServidor(String nombre, String ruta) {//crear una carpeta de manera local
        File carpeta = new File("Servidor/" + ruta.concat("/" + nombre));
        System.out.println("carpeta = " + carpeta.getAbsolutePath());
        if (!carpeta.exists()) {//si no existe la carpeta
            if (carpeta.mkdirs()) {//crea la carpeta
                System.out.println("Directorio creado");
            } else {//reporta el error
                System.out.println("Error al crear directorio");
            }
        }
    }

    public static void enviarDirectorio(Socket sm) {
        try {
            File ruta = new File("Servidor");//carpeta del servidor
            ArrayList<String> rutas = new ArrayList<String>();
            rutas = leerDirectorio(ruta);//arma el directorio
            ObjectOutputStream oos = new ObjectOutputStream(sm.getOutputStream());//especifica el envio
            oos.writeObject(rutas);//seleccion para el envio de las rutas
            oos.flush();//envio
            System.out.println("Direcorio enviado..");
            oos.close();
//            clienteEnvio.close();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            int pto = 9000;
            ServerSocket servidorMeta = new ServerSocket(pto);
//            ServerSocket servidorDatos = new ServerSocket(pto);
            servidorMeta.setReuseAddress(true);
            System.out.println("Servidor iniciado...");
            String ruta = "";
            String nombre = "";
            String accion = "";

            for (;;) {
                Socket cl = servidorMeta.accept();//acepta la conexion del cliente
                BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream(), "ISO-8859-1"));//recibe la accion a realizar
                String msj = br.readLine();
                //formato de la instruccion ruta,nombre,accion
                ruta = msj.substring(0, msj.indexOf(","));//sustrae la ruta
                msj = msj.substring(msj.indexOf(",") + 1, msj.length());
                nombre = msj.substring(0, msj.indexOf(","));//sustrae el nombre
                accion = msj.substring(msj.indexOf(",") + 1, msj.length());//sustrae la accion
                System.out.println("accion = " + accion);
                switch (accion) {//selecciona la accion
                    case "Directorio":
                        enviarDirectorio(cl);
                        break;
                    case "CrearCarpeta":
                        crearCarpetaServidor(nombre, ruta);
                        break;
                }
                cl.close();

            }//for   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
