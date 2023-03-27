/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comunicacioncs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void directorioServidor(Socket sm, Socket sd) {
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(".," + ".,Directorio");
            pw.flush();
            ObjectInputStream ois = new ObjectInputStream(sd.getInputStream());//para leer al servidor
            ArrayList<String> rutasServidor = (ArrayList<String>) ois.readObject();//leer lo que manda el servidor
            formatearDirectorio(rutasServidor);//formatear lo que manda el servidor
            ois.close();
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

    public static void crearCarpetaCliente(String nombre, String ruta) {//crear una carpeta de manera local
        File carpeta = new File("Cliente/" + ruta.concat("/" + nombre));
        if (!carpeta.exists()) {//si no existe la carpeta
            if (carpeta.mkdirs()) {//crea la carpeta
                System.out.println("Directorio creado");
            } else {//reporta el error
                System.out.println("Error al crear directorio");
            }
        } else {
            System.out.println("La carpeta ya exite");
        }
    }

    public static void crearCarpetaServidor(String nombre, String ruta, Socket sc) {//crear una carpeta de manera local

        try {
            sc.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sc.getOutputStream()));//configuracion del envio
            pw.println(ruta + "" + "," + nombre + "" + ",CrearCarpeta");//arma la intruccion a realizar junto con la ruta y el nombre
            pw.flush();//manda la instruccion
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void eliminarCarpetaCliente(File directorio) {
        if (directorio.isDirectory()) {
            for (File archivos : directorio.listFiles()) {
                eliminarCarpetaCliente(archivos);
            }
        }
        directorio.delete();

    }

    private static void eliminarCarpetaServidor(Socket sm, String ruta) {
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(ruta + "," + ".,EliminarCarpeta");//solo se manta la ruta y la accion
            pw.flush();
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void envioArchivo(String archivo, String ruta, Socket sm, Socket sd) {
        ZipFiles zip = new ZipFiles();
        File arch = new File("Cliente/" + archivo);
        //ver si archivo
        if (arch.isFile()) {
            zip.zipSingleFile(arch, "Cliente/archivozip.zip");
        } else if (arch.isDirectory()) {//ver si es directorio
            zip.zipDirectory(arch, "Cliente/archivozip.zip");
        } else {
            System.out.println("Hubo un error al hacer zip");
        }
        File archzip = new File("Cliente/archivozip.zip");
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(ruta + "," + ".,envioArchivo");
            pw.flush();
            String nombre = archzip.getName();
            String path = archzip.getAbsolutePath();
            long tam = archzip.length();
            System.out.println("Preparandose pare enviar archivo " + path + " de " + tam + " bytes\n\n");
            DataOutputStream dosm = new DataOutputStream(sm.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(path));
            dosm.writeUTF(nombre);
            dosm.flush();
            dosm.writeLong(tam);
            dosm.flush();
            long enviados = 0;
            int l = 0, porcentaje = 0;
            DataOutputStream dosd = new DataOutputStream(sd.getOutputStream());
            while (enviados < tam) {
                byte[] b = new byte[1500];
                l = dis.read(b);
//                System.out.println("enviados: " + l);
                dosd.write(b, 0, l);
                dosd.flush();
                enviados = enviados + l;
                porcentaje = (int) ((enviados * 100) / tam);
                System.out.print("\rEnviado el " + porcentaje + " % del archivo");
            }//while
            System.out.println("\nArchivo enviado..");
            dis.close();
            dosm.close();
            dosd.close();
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void descargaArchivo(String rutaS, String rutaC, Socket sm, Socket sd) {
        DataInputStream dissd = null;
        DataOutputStream dos = null;
        ZipFiles zip = new ZipFiles();
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(rutaS + "," + ".,descargarArchivo");
            pw.flush();
            DataInputStream dis = null;

//            System.out.println("test2");
            dis = new DataInputStream(sm.getInputStream());//para metadatos
            dissd = new DataInputStream(sd.getInputStream());//para datos
            String nombre = dis.readUTF();
//            System.out.println("nombre = " + nombre);
            long tam = dis.readLong();
//            System.out.println("tam = " + tam);
            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n\n");
            dos = new DataOutputStream(new FileOutputStream("Cliente/" + rutaC + "/" + nombre));
            long recibidos = 0;
            int l = 0, porcentaje = 0;
            while (recibidos < tam) {
                byte[] b = new byte[1500];
                l = dissd.read(b);
//                System.out.println("leidos: " + l);
                dos.write(b, 0, l);
                dos.flush();
                recibidos = recibidos + l;
                porcentaje = (int) ((recibidos * 100) / tam);
                System.out.print("\rRecibido el " + porcentaje + " % del archivo");
            }//while
            dis.close();
            dos.close();
            dissd.close();
            zip.unzip("Cliente/" + rutaC + "/" + nombre, "Cliente/" + rutaC);
            File archivo = new File("Cliente/" + rutaC + "/" + nombre);
            archivo.delete();
            System.out.println("Archivo recibido..");

        } catch (SocketException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void renombrarCliente(String nombre, String ruta) {
        File archivo = new File("Cliente/" + ruta);
//        File nuevoArchivo = new File(nombre+archivo.getAbsolutePath().substring(archivo.getAbsolutePath().indexOf("."), archivo.getAbsolutePath().length()));
        File nuevoArchivo = new File(archivo.getAbsolutePath().substring(0, archivo.getAbsolutePath().lastIndexOf("\\") + 1) + nombre);
//        System.out.println("archivo = " + archivo.getAbsolutePath());
//        System.out.println("ruta = " + archivo.getAbsolutePath().substring(0, archivo.getAbsolutePath().lastIndexOf("\\") + 1));
        if (nuevoArchivo.exists()) {
            System.out.println("Este nombre de archivo ya existe");
        }
        archivo.renameTo(nuevoArchivo);
//        System.out.println("nuevoArchivo = " + nuevoArchivo.getAbsolutePath());

    }

    private static void renombrarCarpetaServidor(String nombre, String ruta, Socket sm) {
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(ruta + "," + nombre+",renombrarArchivo");
            pw.flush();
        } catch (SocketException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void cerrarConexion(Socket sm) {
        try {
            sm.setOOBInline(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sm.getOutputStream()));
            pw.println(".,.,cerrarConexion");
            pw.flush();
        } catch (SocketException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
        BufferedReader ll = new BufferedReader(new InputStreamReader(System.in));
        try {
            int pto = 8000;
            String dir = "127.0.0.1";
            Socket clienteMeta = new Socket(dir, pto);//socket de la metainformacion
            System.out.println("Conexion con servidor de metadatos establecida...");

            String opcion = "";
            String nombre = "";
            String ruta = "";
            String archivo = "";
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
                        Socket clienteDatos = new Socket("127.0.0.1", 9000);
                        System.out.println("Conexion con servidor de datos establecida...");
                        directorioServidor(clienteMeta, clienteDatos);//mada el socket
                        clienteDatos.close();
                        System.out.println("Servidor de datos cerrado con exito");
                        break;
                    }
                    case "2": {
                        System.out.println("Nombre de la carpeta: ");
                        nombre = ll.readLine();
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        crearCarpetaCliente(nombre, ruta);
                        System.out.println("Carpeta creada con exito");
                        break;
                    }
                    case "3": {
                        System.out.println("Nombre de la carpeta: ");
                        nombre = ll.readLine();
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        crearCarpetaServidor(nombre, ruta, clienteMeta);
                        System.out.println("Carpeta creada con exito");
                        break;
                    }
                    case "4": {
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        File carpeta = new File("Cliente/" + ruta);
                        eliminarCarpetaCliente(carpeta);
                        System.out.println("Carpeta eliminada con exito");
                        break;
                    }
                    case "5": {
                        System.out.println("Ruta: ");
                        ruta = ll.readLine();
                        eliminarCarpetaServidor(clienteMeta, ruta);
                        System.out.println("Carpeta eliminada con exito");
                        break;
                    }
                    case "6": {
                        System.out.println("Ruta del archivo o carpeta a enviar: ");
                        archivo = ll.readLine();
                        System.out.println("Ruta donde se guardara en el servidor: ");
                        ruta = ll.readLine();
                        Socket clienteDatos = new Socket("127.0.0.1", 9000);
                        System.out.println("Conexion con servidor de datos establecida...");
                        envioArchivo(archivo, ruta, clienteMeta, clienteDatos);
                        System.out.println("Archivo/carpeta enviada con exito");
                        clienteDatos.close();
                        System.out.println("Servidor de datos cerrado con exito");
                        clienteMeta = new Socket(dir, pto);

                        break;
                    }
                    case "7": {
                        System.out.println("Ruta del archivo o carpeta a descargar: ");
                        archivo = ll.readLine();
                        System.out.println("Ruta donde se guardara en el cliente: ");
                        ruta = ll.readLine();
                        Socket clienteDatos = new Socket("127.0.0.1", 9000);
                        System.out.println("Conexion con servidor de datos establecida...");
                        descargaArchivo(archivo, ruta, clienteMeta, clienteDatos);
                        System.out.println("Archivo/carpeta enviada con exito");
                        clienteDatos.close();
                        System.out.println("Servidor de datos cerrado con exito");
                        clienteMeta = new Socket(dir, pto);

                        break;
                    }
                    case "8": {
                        System.out.println("Ruta de la carpeta/archivo: ");
                        ruta = ll.readLine();
                        System.out.println("Nuevo nombre: ");
                        nombre = ll.readLine();
                        renombrarCliente(nombre, ruta);
                        System.out.println("Carpeta renombrada con exito");
                        break;
                    }
                    case "9": {
                        System.out.println("Ruta de la carpeta/archivo: ");
                        ruta = ll.readLine();
                        System.out.println("Nuevo nombre: ");
                        nombre = ll.readLine();
                        renombrarCarpetaServidor(nombre, ruta, clienteMeta);
                        System.out.println("Carpeta creada con exito");
                        break;
                    }
                    case "10": {
                        cerrarConexion(clienteMeta);
                        clienteMeta.close();
                        System.out.println("Servidor de metadatos cerrado con exito...");
                        System.exit(0);//termina el programa
                    }

                    default: {
                        System.out.println("Esta opcion no existe");
                    }
                }
                System.out.println("Presiona ENTER para continuar...");

                opcion = ll.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    

}
