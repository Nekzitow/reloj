/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI.Empleados;

import Clases.Controladores;
import Clases.Departamento;
import Clases.Empleado;
import Clases.Puesto;
import Clases.UFMatcherClass;
import Clases.UFScannerClass;
import GUI.Principal;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author OSORIO
 */
public class GuiCaptarHUella extends javax.swing.JInternalFrame {

    //variables del sdk
    private static int BUFFER_SIZE = 10000000;
    private static final long serialVersionUID = 1L;
    private int nScannerNumber = 0;
    public Connection con;
    public Empleado empleado;
    public Principal principal;
    private ImagePanel imgPanel = null;
    public int nC = 0;
    private UFScannerClass libScanner = null;

    private UFMatcherClass libMatcher = null;

    private int nInitFlag = 0;

    private int nCaptureFlag = 0;

    private byte[][] byteTemplateArray = null;

    private int[] intTemplateSizeArray = null;

    private int nTemplateCnt = 0;

    private int nLogListCnt = 0;

    private Pointer hMatcher = null;

    private PointerByReference refTemplateArray = null;  //  @jve:decl-index=0:

    Pointer[] pArray = null;

    private String[] strTemplateArray = null;

    private int nSecurityLevel = 0;

    private int nDetectFake = 2;

    private int nFastMode = 0;

    public final int MAX_TEMPLATE_SIZE = 384;

    /**
     * Creates new form GuiCaptarHUella
     */
    public GuiCaptarHUella(Connection con, Empleado empleado,Principal p) {
        initComponents();
        this.con = con;
        this.empleado = empleado;
        this.principal = p;
        llenarCampos();
        disableButtons();
        iniciarLector();
    }

    public void iniciarLector() {
        String lCurDirTemp = System.getProperty("java.io.tmpdir");
        System.out.println(lCurDirTemp);
        lCurDirTemp = lCurDirTemp + "\\BioMiniLib";
        File directorio = new File(lCurDirTemp);
        directorio.mkdir();
        boolean lValDll = Controladores.descomprime(lCurDirTemp);
        try {
            Controladores.AgregarRuta(lCurDirTemp);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (nInitFlag != 0) {

            MsgBox("already init..");

            return;
        }

        nCaptureFlag = 0;

        try {

            libScanner = (UFScannerClass) Native.loadLibrary("UFScanner", UFScannerClass.class);
            libMatcher = (UFMatcherClass) Native.loadLibrary("UFMatcher", UFMatcherClass.class);
        } catch (Exception ex) {

            setStatusMsg("loadLlibrary : UFScanner,UFMatcher fail!!");
            MsgBox("loadLlibrary : UFScanner,UFMatcher fail!!");

            return;
        }
        int nRes = 0;

        nRes = libScanner.UFS_Init();

        if (nRes == 0) {

            System.out.println("UFS_Init() success!!");

            nInitFlag = 1;

            //MsgBox("Scanner Init success!!");
            nRes = testCallScanProcCallback();

            if (nRes == 0) {

                setStatusMsg("==>UFS_SetScannerCallback pScanProc ...");

                IntByReference refNumber = new IntByReference();
                nRes = libScanner.UFS_GetScannerNumber(refNumber);

                if (nRes == 0) {
                    nScannerNumber = refNumber.getValue();
                    setStatusMsg("UFS_GetScannerNumber() scanner number :" + nScannerNumber);

                    PointerByReference refMatcher = new PointerByReference();

                    nRes = libMatcher.UFM_Create(refMatcher);

                    if (nRes == 0) {

                        UpdateScannerList(); //list upate ==> getcurrentscannerhandle�� list�� ������//
                        System.out.println("after upadtelist");
                        initVariable(1);
                        System.out.println("after initVariable");
                        initArray(100, 1024); //array size,template size

                        hMatcher = refMatcher.getValue();

                        IntByReference refValue = new IntByReference();
                        IntByReference refFastMode = new IntByReference();

                        //security level (1~7)
                        nRes = libMatcher.UFM_GetParameter(hMatcher, 302, refValue); //302 : security level :UFM_

                        if (nRes == 0) {
                            nSecurityLevel = refValue.getValue();//
                            setStatusMsg("get security level,302(security) value is " + refValue.getValue());
                        } else {
                            setStatusMsg("get security level fail! code: " + nRes);
                            MsgBox("get security level fail! code: " + nRes);
                        }

                        //fast mode
                        nRes = libMatcher.UFM_SetParameter(hMatcher, libMatcher.UFM_PARAM_FAST_MODE, refFastMode);
                        if (nRes == 0) {
                            nFastMode = refFastMode.getValue();
                            setStatusMsg("get fastmode,301(fastmode) value is " + refFastMode.getValue());
                            //MsgBox("get fastmode,301(fastmode) value is "+refFastMode.getValue());
                        } else {
                            setStatusMsg("get fastmode value fail! code: " + nRes);
                            MsgBox("get fastmode value fail! code: " + nRes);
                        }
                        if (nFastMode == 1) {

                        }

                        int nSelectedIdx = 0;

                        if (hMatcher != null) {
                            switch (nSelectedIdx) {

                                case 0:
                                    nRes = libMatcher.UFM_SetTemplateType(hMatcher, libMatcher.UFM_TEMPLATE_TYPE_SUPREMA); //2001 Suprema type

                                    break;
                                case 1:
                                    nRes = libMatcher.UFM_SetTemplateType(hMatcher, libMatcher.UFM_TEMPLATE_TYPE_ISO19794_2); //2002 iso type

                                    break;
                                case 2:
                                    nRes = libMatcher.UFM_SetTemplateType(hMatcher, libMatcher.UFM_TEMPLATE_TYPE_ANSI378); //2003 ansi type

                                    break;
                            }
                        }

                    } else {

                        setStatusMsg("UFM_Create fail!! code :" + nRes);

                        return;
                    }

                } else {
                    MsgBox("GetScannerNumber fail!! code :" + nRes);
                    setStatusMsg("GetScannerNumber fail!! code :" + nRes);
                    return;

                }

            } else {

                setStatusMsg("UFS_SetScannerCallback() fail,code :" + nRes);

            }

        }

        if (nRes != 0) {
            System.out.println("Init() fail!!");
            setStatusMsg("Init fail!! return code:" + nRes);
            MsgBox("Scanner Init fail!!");
        }

    }

    public void initArray(int nArrayCnt, int nMaxTemplateSize) {
        if (byteTemplateArray != null) {
            byteTemplateArray = null;

        }

        if (intTemplateSizeArray != null) {
            intTemplateSizeArray = null;

        }

        byteTemplateArray = new byte[nArrayCnt][MAX_TEMPLATE_SIZE];

        intTemplateSizeArray = new int[nArrayCnt];

        refTemplateArray = new PointerByReference();

    }

    public void initVariable(int nFlag) {

        if (nFlag == 1) { //UFS_Init\//
            nInitFlag = 1;
        } else {
            nInitFlag = 0;
        }

        nCaptureFlag = 0;

        nLogListCnt = 0;
        Pointer hScanner = null;
        hScanner = GetCurrentScannerHandle();

        IntByReference pValue = new IntByReference();

        pValue.setValue(5000);

        int nRes = libScanner.UFS_SetParameter(hScanner, libScanner.UFS_PARAM_TIMEOUT, pValue);
        if (nRes == 0) {
            setStatusMsg("Change combox-timeout,201(timeout) value is " + pValue.getValue());
        } else {
            setStatusMsg("Change combox-timeout,change parameter value fail! code: " + nRes);
        }

        pValue.setValue(100);
        nRes = libScanner.UFS_SetParameter(hScanner, libScanner.UFS_PARAM_BRIGHTNESS, pValue);
        if (nRes == 0) {
            setStatusMsg("Change combox-brightness,202 value is " + pValue.getValue());
        } else {
            setStatusMsg("Change combox-brightness,change parameter value fail! code: " + nRes);
        }

        pValue.setValue(7);
        nRes = libScanner.UFS_SetParameter(hScanner, libScanner.UFS_PARAM_SENSITIVITY, pValue);
        if (nRes == 0) {
            setStatusMsg("Change combox-detect_fake,312(fake detect) value is " + pValue.getValue());
        } else {
            setStatusMsg("Change combox-detect_fake,change parameter value fail! code: " + nRes);
        }

        pValue.setValue(7);
        nRes = libScanner.UFS_SetParameter(hScanner, libScanner.UFS_PARAM_SENSITIVITY, pValue);
        if (nRes == 0) {
            setStatusMsg("Change combox-sensitivity,203 value is " + pValue.getValue());
        } else {
            setStatusMsg("Change combox-sensitivity,change parameter value fail! code: " + nRes);
        }

        nRes = libScanner.UFS_SetTemplateType(hScanner, libScanner.UFS_TEMPLATE_TYPE_SUPREMA); //2001 Suprema type
        if (nRes == 0) {
            setStatusMsg("Change combox-Scan TemplateType:2001");
        } else {
            setStatusMsg("Change combox-Scan TemplateType,change parameter value fail! code: " + nRes);
        }
    }

    private ImagePanel getImagePanel() {
        if (imgPanel == null) {
            imgPanel = new ImagePanel();
            imgPanel.setLayout(null);
            imgPanel.setBounds(new Rectangle(260, 17, 270, 310));
            imgPanel.setBackground(Color.red);
        }
        return imgPanel;
    }

    public void llenarCampos() {
        nombre.setText(this.empleado.getNombre() + " " + this.empleado.getApellidos());
        Departamento depaa = Departamento.getDepartamentos(con, this.empleado.getIdDepartamento());
        depa.setText(depaa.getNombre());
        //OBTENEMOS EL PUESTO
        Puesto puesto = Puesto.getPuesto(this.con, this.empleado.getIdPuesto());
        jLabel5.setText(puesto.getDescripcion());
    }

    public void disableButtons() {

        verifica.setEnabled(false);
        guardar.setEnabled(false);
    }

    public void MsgBox(String log) {
        JOptionPane.showMessageDialog(null, log);
    }

    public void drawCurrentFingerImage() {
        /*test draw image*/
        IntByReference refResolution = new IntByReference();
        IntByReference refHeight = new IntByReference();
        IntByReference refWidth = new IntByReference();
        Pointer hScanner = null;

        hScanner = GetCurrentScannerHandle();

        libScanner.UFS_GetCaptureImageBufferInfo(hScanner, refWidth, refHeight, refResolution);
        byte[] pImageData = new byte[refWidth.getValue() * refHeight.getValue()];

        libScanner.UFS_GetCaptureImageBuffer(hScanner, pImageData);

        imgPanel.drawFingerImage(refWidth.getValue(), refHeight.getValue(), pImageData);
    }

    public Pointer GetCurrentScannerHandle() {
        Pointer hScanner = null;
        int nRes = 0;
        int nNumber = 0;

        PointerByReference refScanner = new PointerByReference();
        IntByReference refScannerNumber = new IntByReference();

//		�Ʒ� success!!//
        nRes = libScanner.UFS_GetScannerNumber(refScannerNumber);

        if (nRes == 0) {

            nNumber = refScannerNumber.getValue();

            if (nNumber <= 0) {

                return null;
            }

        } else {

            return null;
        }
        nRes = libScanner.UFS_GetScannerHandle(0, refScanner);

        hScanner = refScanner.getValue();

        if (nRes == 0 && hScanner != null) {
            return hScanner;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nombre = new javax.swing.JLabel();
        depa = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        captura = new javax.swing.JButton();
        verifica = new javax.swing.JButton();
        guardar = new javax.swing.JButton();
        jPanel3 = getImagePanel();
        mensajes = new javax.swing.JLabel();

        setClosable(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalles del Empleado"));

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Nombre:");

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Departamento:");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Puesto:");

        nombre.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        nombre.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        depa.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        depa.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(depa, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(depa, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Captura Huella"));

        captura.setText("Capturar Huella");
        captura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capturaActionPerformed(evt);
            }
        });

        verifica.setText("Verificar");
        verifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificaActionPerformed(evt);
            }
        });

        guardar.setText("Guardar Huella");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setPreferredSize(new java.awt.Dimension(120, 126));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 116, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 122, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(captura, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                    .addComponent(verifica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(193, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mensajes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(mensajes, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(74, 74, 74)
                .addComponent(captura, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(verifica)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(guardar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(97, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void capturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capturaActionPerformed
        // TODO add your handling code here:
        if (nInitFlag == 0) {
            MsgBox("Lector no inicado!");
            return;
        }

        Pointer hScanner = null;

        hScanner = GetCurrentScannerHandle();

        if (hScanner != null) {

            int nRes = libScanner.UFS_ClearCaptureImageBuffer(hScanner);

            setStatusMsg("desplace su dedo");

            nRes = libScanner.UFS_CaptureSingleImage(hScanner);

            setStatusMsg("capturar imagen simple");

            if (nRes == 0) {

                byte[] bTemplate = new byte[512];

                IntByReference refTemplateSize = new IntByReference();

                IntByReference refTemplateQuality = new IntByReference();
                try {
                    nRes = libScanner.UFS_Extract(hScanner, bTemplate, refTemplateSize, refTemplateQuality);

                    if (nRes == 0) {

                        //setStatusMsg("save template file template size:" + refTemplateSize.getValue() + " quality:" + refTemplateQuality.getValue());

                        /*if (nTemplateCnt > 99) {
                            setStatusMsg("template queue full!! limited 100 template, now " + nTemplateCnt);
                            MsgBox("template queue full!! limited 100 template, now " + nTemplateCnt);
                            return;
                        }*/
                        int tempsize = refTemplateSize.getValue();

                        System.arraycopy(bTemplate, 0, byteTemplateArray[0], 0, refTemplateSize.getValue());//byte[][]

                        intTemplateSizeArray[0] = refTemplateSize.getValue();

                        //setStatusMsg("eroll template array idx:" + nTemplateCnt + " template size:" + intTemplateSizeArray[nTemplateCnt]);
                        setStatusMsg("Huella tomada");
                        nTemplateCnt++;
                        //se pinta la huella en el jpanel
                        drawCurrentFingerImage();
                        nCaptureFlag = 1;
                        verifica.setEnabled(true);
                    } else {

                    }
                } catch (Exception ex) {

                    //MsgBox("exception err:"+ex.getMessage());
                }
            }

        } else {
            // scanner pointer  null	
        }

        //PARA CAPTURAR SIMPLE
        /*if (nInitFlag == 1) {
            int nRes = CaptureSingle();
            String log = null;
            log = "capture single image,return value is " + nRes;
            MsgBox(log);
            //drawCurrentFingerImage();

        } else {
            MsgBox("initiate!");
        }*/
    }//GEN-LAST:event_capturaActionPerformed

    private void verificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificaActionPerformed
        // TODO add your handling code here:
        int nSelectedIdx = 0;

        if (nSelectedIdx == -1) {
            MsgBox("selet enroll id");
            return;
        }
        //MsgBox(" enroll id:" + nSelectedIdx + " place a finger");
        MsgBox("Deplaze su dedo, al encender la luz");

        Pointer hScanner = null;
        hScanner = GetCurrentScannerHandle();

        if (hScanner == null) {
            setStatusMsg("Falló al obtener el lector actual");
            return;
        }

        libScanner.UFS_ClearCaptureImageBuffer(hScanner);

        setStatusMsg("Deplaze su dedo");

        int nRes = libScanner.UFS_CaptureSingleImage(hScanner);

        if (nRes != 0) {
            setStatusMsg("Falló al capturar la imagen!! " + nRes);
            return;
        }

        byte[] bTemplate = new byte[512];
        PointerByReference refError;
        IntByReference refTemplateSize = new IntByReference();

        IntByReference refTemplateQuality = new IntByReference();

        IntByReference refVerify = new IntByReference();

        nRes = libScanner.UFS_Extract(hScanner, bTemplate, refTemplateSize, refTemplateQuality);

        if (nRes == 0) {
            try {

                nRes = libMatcher.UFM_Verify(hMatcher, bTemplate, refTemplateSize.getValue(), byteTemplateArray[nSelectedIdx], intTemplateSizeArray[nSelectedIdx], refVerify);//byte[][]

                if (nRes == 0) {
                    if (refVerify.getValue() == 1) {
                        //setStatusMsg("verify success!! enroll_id: " + (nSelectedIdx + 1));
                        //MsgBox("verify success!! enroll_id: " + (nSelectedIdx + 1));
                        setStatusMsg("La huella coincide");
                        MsgBox("La huella coincide");
                        guardar.setEnabled(true);
                        captura.setEnabled(false);
                        verifica.setEnabled(false);
                    } else {
                        setStatusMsg("La huella no coincide");
                        MsgBox("La huella no coincide");
                        //setStatusMsg("verify fail!! enroll_id: " + (nSelectedIdx + 1));
                        //MsgBox("verify fail!! enroll_id: " + (nSelectedIdx + 1));
                    }
                } else {
                    setStatusMsg("verify fail!! " + nRes);

                    byte[] refErr = new byte[512];
                    nRes = libMatcher.UFM_GetErrorString(nRes, refErr);
                    if (nRes == 0) {
                        setStatusMsg("==>UFM_GetErrorString err is " + Native.toString(refErr));
                        MsgBox("==>UFM_GetErrorString err is " + Native.toString(refErr));
                    }

                }
            } catch (Exception ex) {

            }

        } else {
            setStatusMsg("extract template fail!! " + nRes);

        }
    }//GEN-LAST:event_verificaActionPerformed

    private void guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        //Se procedera a guardar en la BD
        int respuesta = JOptionPane.showConfirmDialog(this, "Confirme para guardar la huella", "Guardar", JOptionPane.OK_CANCEL_OPTION);
        if (respuesta == 0) {
            //guardamos
            System.out.println(this.empleado.getId());
            boolean respu = Empleado.saveTemplate(con, this.byteTemplateArray[0], this.intTemplateSizeArray[0], this.empleado.getId());
            if (respu) {
                JOptionPane.showMessageDialog(rootPane, "SE GUARDO EL REGISTRO");
                unInit();
                ModuleEmpleado();
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(rootPane, "NO SE GUARDO EL REGISTRO");
            }
        }
    }//GEN-LAST:event_guardarActionPerformed
    public int CaptureSingle() {

        int nRes = 0;
        Pointer hScanner = null;

        System.out.println("call GetCurrentScannerHandle()");

        hScanner = GetCurrentScannerHandle();

        if (hScanner != null) {

            System.out.println("GetScannerHandle return hScanner pointer: " + hScanner);

            MsgBox("get Scanner handle success pointer:" + hScanner);

        } else {

            System.out.println("GetScannerHandle fail!!");

            MsgBox("get Scanner handle fail!!");

            return -1;
        }

        setStatusMsg("Start single image capturing");

        nRes = libScanner.UFS_CaptureSingleImage(hScanner);

        if (nRes == 0) {
            System.out.println("==>UFS_CaptureSingleImage return value is.." + nRes);

            nCaptureFlag = 1;

            drawCurrentFingerImage();

        } else {

            setStatusMsg("SingleImage fail!! code:" + nRes);

            byte[] refErr = new byte[512];

            nRes = libScanner.UFS_GetErrorString(nRes, refErr);
            if (nRes == 0) {
                System.out.println("==>UFS_GetErrorString err is " + Native.toString(refErr));
            }

            MsgBox("caputure single img fail!!");
        }

        return nRes;
    }

    public void setStatusMsg(String log) {
        mensajes.setText("");
        mensajes.setText(log);
    }

    class ImagePanel extends JPanel {

        //private PlanarImage image;
        private BufferedImage buffImage = null;

        private void drawFingerImage(int nWidth, int nHeight, byte[] buff) {
            buffImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_BYTE_GRAY);
            buffImage.getRaster().setDataElements(0, 0, nWidth, nHeight, buff);

            Graphics g = buffImage.createGraphics();
            g.drawImage(buffImage, 0, 0, 120, 126, null);
            g.dispose();
            repaint();
        }

        public void paintComponent(Graphics g) {
            g.drawImage(buffImage, 0, 0, this);
        }

    }

    public int testCallScanProcCallback() {
        int nRes = 0;

        PointerByReference refParam = new PointerByReference();
        refParam.getPointer().setInt(0, 1);

        nRes = libScanner.UFS_SetScannerCallback(pScanProc, refParam);
        if (nRes == 0) {
            System.out.println("==>UFS_SetScannerCallback pScanProc ..." + pScanProc);

        }
        return nRes;
    }

    UFScannerClass.UFS_SCANNER_PROC pScanProc = new UFScannerClass.UFS_SCANNER_PROC() {
        public int callback(String szScannerId, int bSensorOn, PointerByReference pParam) //interface ..must be implemeent
        {
            nC++;

            System.out.println(nC + "==========================================");  //
            System.out.println("==>ScanProc calle scannerID:" + szScannerId);  //  
            System.out.println("sensoron value is " + bSensorOn);
            System.out.println("void * pParam  value is " + pParam.getValue());
            System.out.println(nC + "==========================================");  //

            UpdateScannerList();

            return 1;
        }
    };

    public void UpdateScannerList() {

        int nSelectedIdx = 0;
        int nRes = 0;
        PointerByReference hTempScanner = new PointerByReference();
        Pointer hScanner = null;
        IntByReference refType = new IntByReference();

        byte[] bScannerId = new byte[512];

        String szListLog = null;
        int nNumber = 0;

        System.out.println("==update Scanner list==");

        IntByReference refNumber = new IntByReference();

        nRes = libScanner.UFS_GetScannerNumber(refNumber);

        if (nRes == 0) {

            System.out.println("UFS_GetScannerNumber() res value is " + nRes);

            nNumber = refNumber.getValue();
        } else {

            return;
        }

        if (nNumber <= 0) {

            return;
        }

        for (int j = 0; j < nNumber; j++) {
            nRes = libScanner.UFS_GetScannerHandle(j, hTempScanner);
            hScanner = null;
            hScanner = hTempScanner.getValue();

            if (nRes == 0 && hScanner != null) {

                nRes = libScanner.UFS_GetScannerID(hScanner, bScannerId);

                nRes = libScanner.UFS_GetScannerType(hScanner, refType);

                szListLog = "ID : " + Native.toString(bScannerId) + " Type : " + refType.getValue();

            }
        }

    }

    UFScannerClass.UFS_CAPTURE_PROC pCaptureProc = new UFScannerClass.UFS_CAPTURE_PROC() {
        public int callback(Pointer hScanner, int bFingerOn, Pointer pImage, int nWidth, int nHeight, int nResolution, PointerByReference pParam) {
            nC++;

            /*
			System.out.println(nC+"==========================================");  //
			System.out.println("==>captureProc calle scanner:"+hScanner);  //  
			System.out.println(" fingerOn:"+bFingerOn);  //  
			System.out.println("width: "+nWidth);
			System.out.println("height: "+nHeight);
			System.out.println("resolution: "+nResolution);
			System.out.println("void * pParam  value is "+pParam.getValue());
			System.out.println(nC+"==========================================");  //
             */
            drawCurrentFingerImage();

            /*
			jFingerInfo.setText("");
			jFingerInfo.setText("width:"+nWidth + " height:"+nHeight+" resolution:"+nResolution);
             */
            //MsgBox("call"+nC); //exception error==> SDK work thread(UFS_Capture_Thread) while loop �� try ,catch  
            return 1;

        }
    };

    public void unInit() {
        System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()

        int nRes = libScanner.UFS_Uninit();

        if (nRes == 0) {

            //setStatusMsg("UFS_Uninit sucess!!");

            nRes = libMatcher.UFM_Delete(hMatcher);

            nInitFlag = 0;

            //MsgBox("UFS_Uninit success!");

        } else {
            //setStatusMsg("UFS_Uninit fail!!");
        }
    }
    
    public void ModuleEmpleado(){
        Empleados emp = new Empleados(this.con, this.principal);
        this.principal.jDesktopPane1.add(emp);
        emp.show();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton captura;
    private javax.swing.JLabel depa;
    private javax.swing.JButton guardar;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel mensajes;
    private javax.swing.JLabel nombre;
    private javax.swing.JButton verifica;
    // End of variables declaration//GEN-END:variables
}
