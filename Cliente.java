/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientSide;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

/**
 *
 * @author user
 */
public class Cliente {

    public static void main(String[] args) throws IOException {
        
        
        
        /*JFileChooser jfS = new JFileChooser();
        jfS.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfS.setMultiSelectionEnabled(true);
        jfS.setPreferredSize(new Dimension(550,200));*/
        
        
       
        int pto = 8000;
        String dir = "127.0.0.1";
        Socket cl = new Socket(dir, pto);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
        DataInputStream dis = new DataInputStream(cl.getInputStream());

        String linea;
        
        VentanaClienteJFrame ventana = new VentanaClienteJFrame();
        ventana.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    // Realizar la acción deseada cuando el JFrame se cierra
                    dos.writeBytes("FINISH"+"\n");
                    cl.shutdownInput();
                    cl.shutdownOutput();
                    dos.close();
                    in.close();
                    dis.close();
                    cl.close();
                } catch (IOException ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ClienteFileChooser jfC = new ClienteFileChooser(in,dos);
        
        jfC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfC.setMultiSelectionEnabled(true);
        /*jfS.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfS.setMultiSelectionEnabled(true);*/
        List<File> files = new ArrayList<>();
        DefaultListModel<File> fileList = new DefaultListModel<>();
        String currentDirectory = "";
        while((linea = in.readLine()) != null){
           System.out.println("LINEA -> "+linea);
           if(linea.equals("DONE")){
               break;
           }
           currentDirectory = linea.substring(0,linea.lastIndexOf("\\"));
            fileList.addElement(new File(linea));
            files.add(new File(linea));
            //fileList.addElement(new File(linea));
         }
        /*
        JPanel panel1 = (JPanel)this.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        */
        
        //JPanel panel1 = (JPanel) jfS.getComponent(2);
        //System.out.println(panel1.getComponents()[0].toString());
        //JPanel panel2 = (JPanel) panel1.getComponent(0);
        //System.out.println(panel2.getComponent(0).toString());
        //JPanel panel1 = ((JPanel)((JPanel)jfS.getComponent(2)).getComponent(0));
//        System.out.println(((JViewport)((JScrollPane)panel1.getComponent(0)).getComponent(0)).getComponent(0).toString());
//        JViewport portV = ((JViewport)((JScrollPane)panel1.getComponent(0)).getComponent(0));
//        System.out.println(portV.getComponent(0).toString());
//        panel1.removeAll();
        
        
        
        //JList<File> list = new JList<>(fileList);
        //list.setCellRenderer(new FileRenderer());
        //JList<File> list = new JList<>(files.toArray(new File[files.size()]));
        
        //JScrollPane scrollPane = new JScrollPane(list);
        //scrollPane.setPreferredSize(new Dimension(100,200));
        //jfS.setAccessory(scrollPane);
        
//        panel1.add(scrollPane);
        ventana.add(jfC,BorderLayout.WEST);
//        ventana.add(jfS,BorderLayout.EAST);
        ServerFileChooser sfc = new ServerFileChooser(fileList,currentDirectory,dos,in,dis);
        ventana.add(sfc,BorderLayout.EAST);

        ventana.setVisible(true);
        
        /*jfS.repaint();
        
        int r = jfS.getDialogType();
        if(r == JFileChooser.APPROVE_OPTION && jfS.getSelectedFile() != null){
            System.out.println(jfS.getSelectedFile().getAbsolutePath());
        }*/ 
        
    }

    public static void directorios(File file) {
//        for(String nombre: file.list()){
        for (File nombre : file.listFiles()) {
            System.out.println(nombre.getAbsolutePath());
            if (nombre.isDirectory()) {
                directorios(nombre);
            }

        }
        return;
    }
}
