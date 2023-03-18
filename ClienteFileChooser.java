/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientSide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author user
 */
public class ClienteFileChooser extends JFileChooser {

    private JButton btnCrearDF;
    private JButton btnEliminarDF;
    private BufferedReader in;
    private DataOutputStream dos;
    public static ClienteFileChooser objeto;

    public ClienteFileChooser(BufferedReader in, DataOutputStream dos) {
        super();
        this.in = in;
        this.dos = dos;
        objeto = this;
        JPanel panel1 = (JPanel) this.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        panel2.add(btnCrear());
        panel2.add(btnEliminar());
        setApproveButtonText("DownLoad");
        
    }
    
    

    @Override
    public void approveSelection() {//Descargando archivos del servidor
        // Aquí se define el comportamiento del botón Abrir
        super.approveSelection();

        //downloadFD();
        System.out.println("Se ha seleccionado el archivo " + this.getSelectedFile().getAbsolutePath());
    }
    
    
    public void downloadFD(){
        File file = ServerFileChooser.getFileServidor();
        try {
            dos.writeBytes("DW" + "\n");
            dos.writeBytes("DONE" + "\n");
            dos.writeBytes(file.getAbsolutePath() + "\n");//Envio archivo a descargar
            
            System.out.println(this.getCurrentDirectory());
            /*while ((linea = in.readLine()) != null) {
                            if (linea.equals("DONE")) {
                                break;
                            }
                            System.out.println("Cambio Directorio -> "+linea);
                            currentDirectory = linea.substring(0, linea.lastIndexOf("\\"));
                            fileList.addElement(new File(linea));
                        
                        }*/
        } catch (IOException ex) {
            Logger.getLogger(ServerFileChooser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JButton btnCrear() {
        btnCrearDF = new JButton("New...");
        btnCrearDF.setPreferredSize(new Dimension(20, 26));
        btnCrearDF.setAlignmentY(TOP_ALIGNMENT);
        btnCrearDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClienteFileChooser.this.getSelectedFile() != null) {
                    System.out.println("Creando Archivo ruta -> " + ClienteFileChooser.this.getSelectedFile().getAbsolutePath());
                }
            }

        });
        return btnCrearDF;
    }

    private JButton btnEliminar() {
        btnEliminarDF = new JButton("Delete");
        btnEliminarDF.setPreferredSize(new Dimension(20, 26));
        btnEliminarDF.setAlignmentY(TOP_ALIGNMENT);
        btnEliminarDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClienteFileChooser.this.getSelectedFile() != null) {
                    System.out.println("Eliminando Archivo ruta -> " + ClienteFileChooser.this.getSelectedFile().getAbsolutePath());
                }
            }

        });
        return btnEliminarDF;
    }
    
    
            

}
