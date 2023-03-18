/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */package comunicacioncs;

import static comunicacioncs.Cliente.leerDirectorio;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static void enviarDirectorio(Socket sDatos) {
        try {
            File ruta = new File("Servidor");//carpeta del servidor
            ArrayList<String> rutas = new ArrayList<String>();
            rutas = leerDirectorio(ruta);//arma el directorio
            ObjectOutputStream oos = new ObjectOutputStream(sDatos.getOutputStream());//especifica el envio al servidor de datos
            oos.writeObject(rutas);//seleccion para el envio de las rutas
            oos.flush();//envio
            System.out.println("Direcorio enviado..");
            oos.close();
//            clienteEnvio.close();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void eliminarCarpetaServidor(File directorio) {
        if (directorio.isDirectory()) {
            for (File archivos : directorio.listFiles()) {
                eliminarCarpetaServidor(archivos);
            }
        }
        directorio.delete();
    }

    private static void envioServidor(String ruta, Socket sd, Socket sm) {
        DataInputStream dis = null;
        DataInputStream dissd = null;
        DataOutputStream dos = null;
        ZipFiles zip = new ZipFiles();
        try {
            System.out.println("test2");
            dis = new DataInputStream(sm.getInputStream());//para metadatos
            dissd = new DataInputStream(sd.getInputStream());//para datos
            String nombre = dis.readUTF();
            System.out.println("nombre = " + nombre);
            long tam = dis.readLong();
            System.out.println("tam = " + tam);
            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n\n");
            dos = new DataOutputStream(new FileOutputStream("Servidor/" + ruta + "/" + nombre));
            long recibidos = 0;
            int l = 0, porcentaje = 0;
            while (recibidos < tam) {
                byte[] b = new byte[1500];
                l = dissd.read(b);
                System.out.println("leidos: " + l);
                dos.write(b, 0, l);
                dos.flush();
                recibidos = recibidos + l;
                porcentaje = (int) ((recibidos * 100) / tam);
                System.out.print("\rRecibido el " + porcentaje + " % del archivo");
            }//while
            dis.close();
            dos.close();
            dissd.close();
            zip.unzip("Servidor/" + ruta + "/" + nombre, "Servidor/" + ruta);
            File archivo = new File("Servidor/" + ruta + "/" + nombre);
            archivo.delete();
            System.out.println("Archivo recibido..");

        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void decargaServidor(String archivo, Socket sd, Socket sm) {
        ZipFiles zip = new ZipFiles();
        File arch = new File("Servidor/" + archivo);
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
                System.out.println("enviados: " + l);
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

    private static void renombrarCarpetaServidor(String nombre, String ruta) {
        File archivo = new File("Servidor/" + ruta);
//        File nuevoArchivo = new File(nombre+archivo.getAbsolutePath().substring(archivo.getAbsolutePath().indexOf("."), archivo.getAbsolutePath().length()));
        File nuevoArchivo = new File(archivo.getAbsolutePath().substring(0, archivo.getAbsolutePath().lastIndexOf("\\") + 1) + nombre);
        System.out.println("archivo = " + archivo.getAbsolutePath());
        System.out.println("ruta = " + archivo.getAbsolutePath().substring(0, archivo.getAbsolutePath().lastIndexOf("\\") + 1));
        if (nuevoArchivo.exists()) {
            System.out.println("Este nombre de archivo ya existe");
        }
        archivo.renameTo(nuevoArchivo);
        System.out.println("nuevoArchivo = " + nuevoArchivo.getAbsolutePath());
    }

    public static void main(String[] args) {
        try {
            int pto = 8000;
            ServerSocket servidorMeta = new ServerSocket(pto);
            servidorMeta.setReuseAddress(true);
            System.out.println("Servidor iniciado de Metadatos iniciado...");
            String ruta = "";
            String nombre = "";
            String accion = "";
            Socket sm = servidorMeta.accept();//acepta la conexion del cliente
            System.out.println("Cliente concetado...");
            for (;;) {

                if (sm.isClosed()) {
                    sm = servidorMeta.accept();//acepta la conexion del cliente
                    System.out.println("Cliente concetado...");
                }
                ServerSocket servidorDatos = new ServerSocket(9000);//inicio del servidor por si se envian datos
//                System.out.println("Servidor de Datos iniciado...");
                BufferedReader br = new BufferedReader(new InputStreamReader(sm.getInputStream(), "ISO-8859-1"));//recibe la accion a realizar
                String msj = br.readLine();

                //formato de la instruccion ruta,nombre,accion
                if (msj != null) {
                    ruta = msj.substring(0, msj.indexOf(","));//sustrae la ruta
                    System.out.println("ruta = " + ruta);
                    msj = msj.substring(msj.indexOf(",") + 1, msj.length());
                    nombre = msj.substring(0, msj.indexOf(","));//sustrae el nombre
                    System.out.println("nombre = " + nombre);
                    accion = msj.substring(msj.indexOf(",") + 1, msj.length());//sustrae la accion
                    System.out.println("accion = " + accion);
                } else {
                    ruta = " ";
                    nombre = " ";
                    accion = " ";
                }

                switch (accion) {//selecciona la accion
                    case "Directorio":

                        Socket sd = servidorDatos.accept();//acepta la conexion del cliente
                        enviarDirectorio(sd);
                        sd.close();

                        break;
                    case "CrearCarpeta":
                        crearCarpetaServidor(nombre, ruta);
                        break;
                    case "EliminarCarpeta":
                        File carpeta = new File("Servidor/" + ruta);
                        eliminarCarpetaServidor(carpeta);
                        break;
                    case "envioArchivo":

                        Socket sdEA = servidorDatos.accept();//acepta la conexion del cliente
                        System.out.println("test1");
                        envioServidor(ruta, sdEA, sm);
                        sdEA.close();
                        sm = servidorMeta.accept();//acepta la conexion del cliente
                        break;
                    case "descargarArchivo":

                        Socket sdDA = servidorDatos.accept();//acepta la conexion del cliente
                        System.out.println("test1");
                        decargaServidor(ruta, sdDA, sm);
                        sdDA.close();
                        sm = servidorMeta.accept();//acepta la conexion del cliente
                        break;
                    case "renombrarArchivo":
                        renombrarCarpetaServidor(nombre, ruta);
                    case "cerrarConexion":
                        sm.close();
                    default:
                        break;

                }
//                sm.close();
                servidorDatos.close();//cierre del servidor de los datos
//                System.out.println("Servidor de datos cerrado");
            }//for   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
