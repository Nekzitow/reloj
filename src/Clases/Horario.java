/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author OSORIO
 */
public class Horario {
    private int id;
    private String horaEntrada;
    private String horaSalida;
    private int dia;
    private int idTipo;
    private int tiempoAntes;
    private int tiempoDespues;
    
    public Horario(){
        this.id = 0;
        this.horaEntrada = "";
        this.horaSalida = "";
        this.dia = 0;
        this.idTipo = 0;
        this.tiempoAntes = 0;
        this.tiempoDespues = 0;
    }

    public Horario(int id, String horaEntrada, String horaSalida, int dia, int idTipo,int tiempoAntes,int tiempoDespues) {
        this.id = id;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.dia = dia;
        this.idTipo = idTipo;
        this.tiempoAntes = tiempoAntes;
        this.tiempoDespues = tiempoDespues;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public int getTiempoAntes() {
        return tiempoAntes;
    }

    public void setTiempoAntes(int tiempoAntes) {
        this.tiempoAntes = tiempoAntes;
    }

    public int getTiempoDespues() {
        return tiempoDespues;
    }

    public void setTiempoDespues(int tiempoDespues) {
        this.tiempoDespues = tiempoDespues;
    }    
    
    /**
     * 
     * @param con
     * @param idEmpleado
     * @return ArrayList<Horario>
     */
    public ArrayList<Horario> HorarioEmpleado(Connection con,int idEmpleado){
        ArrayList<Horario> lista = new ArrayList<>();
        try {
            String query = "SELECT horario.*,tiempo_antes,tiempo_despues FROM horario "
                    + "INNER JOIN asignacion_horario ON horario.id = id_horario "
                    + "INNER JOIN tipo_horario ON tipo_horario.id = id_tipo_horario "
                    + "INNER JOIN parametros ON parametros.id = id_parametros "
                    + " WHERE id_empleado = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, idEmpleado);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Horario horario = new Horario();
                horario.setId(rs.getInt("id"));
                horario.setHoraEntrada(rs.getString("hora_entrada"));
                horario.setHoraSalida(rs.getString("hora_salida"));
                horario.setDia(rs.getInt("dia"));
                horario.setIdTipo(rs.getInt("id_tipo_horario"));
                horario.setTiempoAntes(rs.getInt("tiempo_antes"));
                horario.setTiempoDespues(rs.getInt("tiempo_despues"));
                lista.add(horario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    /**
     * 
     * @param con
     * @param idEmpleado
     * @return 
     */
    public static  ArrayList<Horario> horarioDia(Connection con,int idEmpleado){
        Calendar cal = Calendar.getInstance();
        ArrayList<Horario> lista = new ArrayList<>();
        try {
            String query = "SELECT horario.*,tiempo_antes,tiempo_despues FROM horario "
                    + "INNER JOIN asignacion_horario ON horario.id = id_horario "
                    + "INNER JOIN tipo_horario ON tipo_horario.id = id_tipo_horario "
                    + "INNER JOIN parametros ON parametros.id = id_parametros "
                    + " WHERE id_empleado = ? AND dia = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, idEmpleado);
            pstmt.setInt(2, getDiaActual());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Horario horario = new Horario();
                horario.setId(rs.getInt("id"));
                horario.setHoraEntrada(rs.getString("hora_entrada"));
                horario.setHoraSalida(rs.getString("hora_salida"));
                horario.setDia(rs.getInt("dia"));
                horario.setIdTipo(rs.getInt("id_tipo_horario"));
                horario.setTiempoAntes(rs.getInt("tiempo_antes"));
                horario.setTiempoDespues(rs.getInt("tiempo_despues"));
                lista.add(horario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    public static int getDiaActual(){
         Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int dia = 0;
        switch (day) {
            case 0:
                //sabado
                dia = 5;
                break;
            case 1:
                //domingo
                dia = 6;
                break;
            case 2:
                //lunes
                dia = 0;
                break;
            case 3:
                //martes
                dia = 1;
                break;
            case 4:
                //miercoles
                dia = 2;
                break;
            case 5:
                //jueves
                dia = 3;
                break;
            case 6:
                //viernes
                dia = 4;
                break;
        }
        return dia;
    }
}
