/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testClases;

import Clases.Conexion;
import Clases.Empleado;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author OSORIO
 */
public class test {

    public static void main(String[] args) {
        Connection con = Conexion.conecta();
       
        Date now = new Date();
            SimpleDateFormat fech = new SimpleDateFormat("yyyy-MM-dd");
            String dateHoy = fech.format(now);
            dateHoy = dateHoy + "%";
            JOptionPane.showMessageDialog(null, dateHoy);
        /*ArrayList<Empleado> empleados = Empleado.listaEmpleados(con, 1);
        for (Empleado empleado : empleados) {
            System.out.println(empleado.getNombre()+" "+empleado.getApellidos());
        }*/
    }
}
