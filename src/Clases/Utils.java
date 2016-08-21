/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import java.sql.Connection;
import java.util.Date;
import javax.swing.JOptionPane;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author OSORIO
 */
public class Utils {

    public static int comparaHorario(Date horaEntrada, Date horaSalida) {
        int respuesta = 0;
        DateTime horaEntra = new DateTime(horaEntrada);
        DateTime horaSal = new DateTime(horaSalida);
        Period periodo = new Period(horaEntra, horaSal);
        respuesta = periodo.getHours();
        return respuesta;
    }

    public static boolean evaluarAdmon(Connection con, Date comparar1,
            Date comparar2, Date comparar3, Horario hora, int idEmpleado, String horario) {
        int respuesta = 0;
        boolean valor = false;
        if (evaluarLimite(comparar1, comparar2, hora.getTiempoAntes() * 60) || evaluarLimite2(comparar1, comparar2)) {
            System.out.println("Fecha en el rango");
            if (evaluarLimite2(comparar1, comparar2)) {
                //se guarda como retardo
                JOptionPane.showMessageDialog(null, "SE GUARDARA COMO RETARDO");
                Horario.checkAsistencia(con, idEmpleado, 1, hora.getIdAsignacion(), horario, 1);
                valor = false;
            } else {
                //se hace el guardado 
                Horario.checkAsistencia(con, idEmpleado, 1, hora.getIdAsignacion(), horario, 2);
                valor = true;
            }

        } else if (evaluarLimite(comparar1, comparar3, hora.getTiempoDespues() * 60)) {
            Horario.checkAsistencia(con, idEmpleado, 2, hora.getIdAsignacion(), horario, 2);
            valor = true;
        } else {
            valor = false;
        }
        return valor;
    }

    public static boolean evaluarDocente(Connection con, Date comparar1,
            Date comparar2, Date comparar3, Horario hora, int idEmpleado, String horario) {
        boolean valor = false;
        if (evaluarLimite(comparar1, comparar2, hora.getTiempoAntes() * 60)) {
            Horario.checkAsistencia(con, idEmpleado, 1, hora.getIdAsignacion(), horario, 2);
            valor = true;
        } else if (evaluarLimite(comparar1, comparar3, hora.getTiempoDespues() * 60)) {
            Horario.checkAsistencia(con, idEmpleado, 2, hora.getIdAsignacion(), horario, 2);
            valor = true;
        } else {
            valor = false;
        }
        return valor;
    }

    public static boolean evaluarDesayuno(Connection con, Date comparar1,
            Date comparar2, Date comparar3, Horario hora, int idEmpleado, String horario) {
        boolean valor = false;
        if (evaluarLimite(comparar1, comparar2, hora.getTiempoAntes() * 60)) {
            //hacemos la insercion
            Horario.checkAsistencia(con, idEmpleado, 1, hora.getIdAsignacion(), horario, 2);
            valor = true;
        } else if (evaluarLimite(comparar1, comparar3, hora.getTiempoDespues() * 60)) {
            Horario.checkAsistencia(con, idEmpleado, 2, hora.getIdAsignacion(), horario, 2);
            valor = true;
        } else {
            valor = false;
        }
        return valor;
    }

    public static boolean evaluarLimite(Date date1, Date date2, int tiempo) {
        boolean correcto = false;
        long diferencia = (Math.abs(date1.getTime() - date2.getTime())) / 1000;
        long limit = (tiempo * 1000) / 1000L;//limite de tiempo
        if (diferencia <= limit) {
            correcto = true;
        }
        return correcto;
    }

    public static boolean evaluarLimite2(Date date1, Date date2) {
        boolean correcto = false;
        long diferencia = (Math.abs(date1.getTime() - date2.getTime())) / 1000;
        long limit = (5400 * 1000) / 1000L;//limite de tiempo
        if (diferencia <= limit) {
            correcto = true;
        }
        return correcto;
    }
}