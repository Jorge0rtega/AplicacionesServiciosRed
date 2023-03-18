/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientSide;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author user
 */
public class ServerFileChooser extends JPanel {

    private DefaultListModel<File> fileList = new DefaultListModel<>();
    private  JTextField directorioActual;
    private JList<File> list;
    private String currentDirectory;
    private DataOutputStream dos;
    private BufferedReader in;
    private static File curretFile;
    private DataInputStream dis;
    
    public ServerFileChooser(DefaultListModel<File> fileList,String currentDirectory,DataOutputStream dos,BufferedReader in, DataInputStream dis) {
        this.currentDirectory = currentDirectory;
        this.fileList = fileList;
        this.dos = dos;
        this.in = in;
        this.dis = dis;
        setPreferredSize(new Dimension(550, 200));
        setBackground(Color.red);
        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearFileChooser(),BorderLayout.CENTER);
        //add(crearBtns(),BorderLayout.SOUTH);

    }

    private JPanel crearEncabezado() {

        JLabel title = new JLabel("Archivos Servidor: ");
        title.setPreferredSize(new Dimension(150, 20));

        directorioActual= new JTextField(currentDirectory);
        directorioActual.setPreferredSize(new Dimension(300, 20));

        JPanel miPanel = new JPanel();
        miPanel.add(title);
        miPanel.add(directorioActual);
        return miPanel;
    }

    private JPanel crearFileChooser() {

        list = new JList<>(fileList);
        list.setCellRenderer(new FileRenderer());
        list.setVisible(true);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        list.setPreferredSize(new Dimension(500,500));
        
        /*list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList<File> list = (JList<File>) e.getSource();
                    File selectedFile = list.getSelectedValue();
                    System.out.println("Archivo seleccionado: " + selectedFile.getName());
                    directorioActual.setText(selectedFile.getName());
                }
            }
        });*/
        
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    File selectedFile = fileList.getElementAt(index);
                    curretFile = fileList.getElementAt(index);
                    if(selectedFile.isFile()){
                        return;
                    }
                    
                    String linea;
                    String currentDirectory = "";
                    System.out.println("ENVIANDO DIRECTORIO");
                    
                    try {
                        dos.writeBytes("IN"+ "\n");
                        dos.writeBytes("DONE" + "\n");
                        dos.writeBytes(selectedFile.getAbsolutePath() + "\n");

                        System.out.println("ENVIADO");
                        fileList.clear();
                        while ((linea = in.readLine()) != null) {
                            if (linea.equals("DONE")) {
                                break;
                            }
                            System.out.println("Cambio Directorio -> "+linea);
                            currentDirectory = linea.substring(0, linea.lastIndexOf("\\"));
                            fileList.addElement(new File(linea));
                        
                        }
                        list.setModel(fileList);
                        list.repaint();
                        directorioActual.setText(currentDirectory);
                    } catch (IOException ex) {
                        Logger.getLogger(ServerFileChooser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }else if(evt.getClickCount() == 1){
                    int index = list.locationToIndex(evt.getPoint());
                    File selectedFile = fileList.getElementAt(index);
                    if(selectedFile.isFile()){
                        curretFile = fileList.getElementAt(index);
                        System.out.println("Archivo seleccionado: " + selectedFile.getName());
                    }else if(selectedFile.isDirectory()){
                        curretFile = fileList.getElementAt(index);
                        System.out.println("Directorio seleccionado: " + selectedFile.getName());                        
                    }
                    //directorioActual.setText(selectedFile.getName());
                    //list.clearSelection();
                }
            }
        });
        
        
        //JList<File> list = new JList<>(files.toArray(new File[files.size()]));
        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVisible(true);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JPanel miPanel = new JPanel();
        miPanel.setBorder(null);
        miPanel.setAutoscrolls(false);
        miPanel.setPreferredSize(new Dimension(500, 400));
        miPanel.add(scrollPane);
        miPanel.setSize(new Dimension(500, 400));
        miPanel.add(crearBtns());
        return miPanel;
    }
    
    
    
    
    
    
    
    
    
    private JPanel crearBtns(){
        
        JButton btnAbrir = new JButton("DownLoad");
        JButton btnNew = new JButton("New ...");
        JButton btnDelete = new JButton("Delete");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAbrir.setPreferredSize(new Dimension(100,30));
        btnNew.setPreferredSize(new Dimension(100,30));
        btnDelete.setPreferredSize(new Dimension(100,30));
        btnCancelar.setPreferredSize(new Dimension(100,30));
        
        btnAbrir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadFD();
            }
        });
        
        
        
        
        
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list.clearSelection();
            }
        });
        
        
        JPanel miPanel = new JPanel();
        miPanel.add(btnAbrir);
        miPanel.add(btnNew);
        miPanel.add(btnDelete);
        miPanel.add(btnCancelar);
        miPanel.setAutoscrolls(false);
        miPanel.setPreferredSize(new Dimension(500, 100));
        return miPanel;
    }
   
    public void downloadFD(){
        File file = ServerFileChooser.getFileServidor();
        try {
            dos.writeBytes("DW" + "\n");
            dos.writeBytes("DONE" + "\n");
            dos.writeBytes(file.getAbsolutePath() + "\n");//Envio archivo a descargar
            String ruta_archivos = ClienteFileChooser.objeto.getCurrentDirectory().getAbsolutePath()+"\\"; 
            File f2 = new File(ruta_archivos);
            f2.mkdirs();
            f2.setWritable(true);
            String nombre = dis.readUTF();
            DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(ruta_archivos+nombre));
            long tam = dis.readLong();
            long recibidos=0;
              int l=0, porcentaje=0;
              while(recibidos<tam){
                  byte[] b = new byte[1500];
                  l = dis.read(b);
                  System.out.println("leidos: "+l);
                  dos2.write(b,0,l);
                  dos2.flush();
                  recibidos = recibidos + l;
                  porcentaje = (int)((recibidos*100)/tam);
                  System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
              }//while
              System.out.println("Archivo recibido..");
            ClienteFileChooser.objeto.rescanCurrentDirectory();
            /*
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivos+nombre));
              long recibidos=0;
              int l=0, porcentaje=0;
              while(recibidos<tam){
                  byte[] b = new byte[1500];
                  l = dis.read(b);
                  System.out.println("leidos: "+l);
                  dos.write(b,0,l);
                  dos.flush();
                  recibidos = recibidos + l;
                  porcentaje = (int)((recibidos*100)/tam);
                  System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
              }//while
              System.out.println("Archivo recibido..");
            
            
            */
            
            
            
            
            
            
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
    
    
    public static File getFileServidor(){
        return curretFile;
    }

}
