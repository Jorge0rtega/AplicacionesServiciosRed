/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comunicacioncs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jorge Ortega
 */
public class Cliente {

    public static ArrayList<String> leerDirectorio(File ruta) {
        File[] archivos = ruta.listFiles();//enlista los archivos del directorios actual
        ArrayList<String> rutas = new ArrayList<String>();
        for (File archivo : archivos) {//foreach de los archivos
            if (archivo.isFile()) {//es un archivo
//                System.out.println(archivo.getPath());
                rutas.add(archivo.getPath());//agrega la ruta relativa
            } else if (archivo.isDirectory()) {//es una carpeta
                rutas.add(archivo.getPath());
                rutas.addAll(leerDirectorio(archivo));//vuelve a iterar
            }
        }
        return rutas;
    }

    public static void formatearDirectorio(ArrayList<String> rutas) {
        System.out.println("-----------------Directorio-----------------");//formato
        for (String ruta : rutas) {
            System.out.println("- " + ruta);
        }
    }

    public static void directorioServidor(Socket sc) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));
            sc.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sc.getOutputStream()));
            pw.println(" ,"+" ,Directorio");
            pw.flush();
            ObjectInputStream ois = new ObjectInputStream(sc.getInputStream());//para leer al servidor
            ArrayList<String> rutasServidor = (ArrayList<String>) ois.readObject();//leer lo que manda el servidor
            formatearDirectorio(rutasServidor);//formatear lo que manda el servidor
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void directorioCliente() {//muestra todo el directorio
        File ruta = new File("Cliente");//especifica la ruta del cliente
        ArrayList<String> rutasCliente = new ArrayList<String>();
        rutasCliente = leerDirectorio(ruta);// para leer el direcorio
        formatearDirectorio(rutasCliente);//para mostrar el directorio con formato
    }
    public static void crearCarpetaCliente(String nombre, String ruta){//crear una carpeta de manera local
        File carpeta = new File("Cliente/"+ruta.concat("/"+nombre));
        if (!carpeta.exists()) {//si no existe la carpeta
            if (carpeta.mkdirs()) {//crea la carpeta
                System.out.println("Directorio creado");
            } else {//reporta el error
                System.out.println("Error al crear directorio");
            }
        }
    }
    public static void crearCarpetaServidor(String nombre, String ruta, Socket sc){//crear una carpeta de manera local
        
        try {
            sc.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sc.getOutputStream()));//configuracion del envio
            pw.println(ruta+""+","+nombre+""+",CrearCarpeta");//arma la intruccion a realizar junto con la ruta y el nombre
            pw.flush();//manda la instruccion
            
        }catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        BufferedReader ll = new BufferedReader(new InputStreamReader(System.in));
        try {
            int pto = 9000;
            String dir = "127.0.0.1";
            Socket clienteMeta = new Socket(dir, pto);//socket de la metainformacion
            System.out.println("Conexion con servidor establecida...");
//            Socket clienteDatos = new Socket(dir, pto);
            String opcion = "";
            String nombre="";
            String ruta="";
            while (!opcion.equals("10")) {
                
                System.out.println("-----------------Menu-----------------");
                System.out.println("0) Ver carpetas/archivos localmente");
                System.out.println("1) Ver carpetas/archivos remotamente");
                System.out.println("2) Crear carpeta local");
                System.out.println("3) Crear carpeta remota");
                System.out.println("4) Eliminar carpeta local");
                System.out.println("5) Eliminar carpeta remota");
                System.out.println("6) Enviar archivo/carpeta hacia la carpeta remota");
                System.out.println("7) Descargar archivo/carpeta desde la carpeta remota");
                System.out.println("8) Renombra archivo/carpeta en la carpeta local");
                System.out.println("9) Renombra archivo/carpeta en la carpeta remota");
                System.out.println("10) Salir");
                opcion = ll.readLine();
                switch (opcion) {
                    case "0": {
                        directorioCliente();
                        break;
                    }
                    case "1": {
                        directorioServidor(clienteMeta);//mada el socket
                        break;
                    }
                    case "2": {
                        System.out.println("Nombre de la carpeta: ");
                        nombre = ll.readLine();
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        crearCarpetaCliente(nombre, ruta);
                        break;
                    }
                    case "3": {
                        System.out.println("Nombre de la carpeta: ");
                        nombre = ll.readLine();
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        crearCarpetaServidor(nombre, ruta, clienteMeta);
                        break;
                    }
                    case "10": {
                        System.exit(0);//termina el programa
                    }
                }
                System.out.println("Presiona ENTER para continuar...");
                opcion=ll.readLine();
                
            }

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
