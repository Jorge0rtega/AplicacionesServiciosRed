
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
public class Server {

    public static DataOutputStream dos;
    public static BufferedReader in;
    public static DataInputStream dis;

    public static void main(String[] args) throws IOException {
        String currentDirectory = "C:\\Users\\user\\Documents\\TT";
        int pto = 8000;
        ServerSocket s = new ServerSocket(pto);
        s.setReuseAddress(true);
        for (;;) {
            Socket cliente = s.accept();
            dos = new DataOutputStream(cliente.getOutputStream());
            in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            File file = new File(currentDirectory);
            directorios(file, dos);
            boolean done = true;
            boolean finish = true;
            while (done || finish) {
                //Recibiendo datos 

                String linea = null;
                String aux = "";
                System.out.println("ESPERANDO");
                while ((linea = in.readLine()) != null) {
                    if (linea.equals("DONE")) {
                        done = false;
                        break;
                    } else if (linea.equals("FINISH")) {
                        aux = "FINISH";
                        finish = false;
                        break;
                    } else {
                        aux = linea;
                    }
                }
                System.out.println("RECIBIDO");
                if (aux.equals("IN")) {
                    linea = in.readLine();
                    currentDirectory = linea;
                    file = new File(linea);
                    directorios(file, dos);
                } else if (aux.equals("DW")) {//Descargando hacia el localhost
                    System.out.println("Archivo a descargar: " + file.getAbsolutePath());
                    linea = in.readLine();
                    File fileEnv = new File(linea);
                    fileEnv.mkdirs();
                    fileEnv.setWritable(true);
                    fileEnv.setReadable(true);
                    enviarArchivo(fileEnv);
                }

            }
            dos.close();
            in.close();
            cliente.close();
        }

    }

    public static void enviarArchivo(File file) throws IOException {
        file.mkdirs();
        file.setReadable(true);

        file.setWritable(true);
        String path = "";
        String nombre = "";
        long tam = 0;
        if (file.isFile()) {
            tam = file.length();
            nombre = file.getName();
            path = file.getAbsolutePath();

        } else if (file.isDirectory()) {
            System.out.println("NOMBRE DIRECTORIO " + file.getAbsolutePath());

            /*
            
                Comprimiendo archivos
            
             */
            String sourceDir = file.getAbsolutePath();
            String zipFile = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf("\\")) + "\\resultado.zip";
            //"ruta/del/archivo/zip/resultado.zip";

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            addDirToZip(zos, sourceDir);

            zos.close();
            fos.close();


            file = new File(zipFile);
            nombre = file.getName();

            path = file.getAbsolutePath();
            tam = file.length();
        }
        System.out.println("ACA3");
        dos.writeUTF(nombre);
        dos.flush();
        dos.writeLong(tam);
        dos.flush();
        dis = new DataInputStream(new FileInputStream(path));
        long enviados = 0;
        int l = 0, porcentaje = 0;
        while (enviados < tam) {
            byte[] b = new byte[1500];
            l = dis.read(b);
            System.out.println("enviados: " + l);
            dos.write(b, 0, l);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int) ((enviados * 100) / tam);
            System.out.print("\rEnviado el " + porcentaje + " % del archivo");
        }//while
        System.out.println("\nArchivo enviado..");
        dis.close();

        /*
        File f = jf.getSelectedFile();
                String nombre = f.getName();
                String path = f.getAbsolutePath();
                long tam = f.length();
                System.out.println("Preparandose pare enviar archivo "+path+" de "+tam+" bytes\n\n");
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                long enviados = 0;
                int l=0,porcentaje=0;
                while(enviados<tam){
                    byte[] b = new byte[1500];
                    l=dis.read(b);
                    System.out.println("enviados: "+l);
                    dos.write(b,0,l);
                    dos.flush();
                    enviados = enviados + l;
                    porcentaje = (int)((enviados*100)/tam);
                    System.out.print("\rEnviado el "+porcentaje+" % del archivo");
                }//while
                System.out.println("\nArchivo enviado..");
         */
    }

    public static void directorios(File file, DataOutputStream dos) throws IOException {
//        for(String nombre: file.list()){
        for (File nombre : file.listFiles()) {
            System.out.println(nombre.getAbsolutePath());
            dos.writeBytes(nombre.getAbsolutePath() + "\n");
            /*            if(nombre.isDirectory()){
                directorios(nombre,dos);
            }*/

        }
        dos.writeBytes("DONE" + "\n");
        return;
    }

    private static void addDirToZip(ZipOutputStream zos, String sourceDir) throws IOException {
        File folder = new File(sourceDir);
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addDirToZip(zos, file.getAbsolutePath());
            } else {
                String entryName = file.getAbsolutePath().replace(sourceDir, "");
                System.out.println("SOURCE DIR -> "+sourceDir);
                System.out.println("ENTRYNAME DIR -> "+entryName);
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                fis.close();
                zos.closeEntry();
            }
        }
    }

}
