/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author OSORIO
 */
public class Conexion {
    public static Connection Con;
    public static Connection conecta(){
        try{
            Connection c = DriverManager.getConnection("jdbc:postgresql://192.168.0.150:5432/sium_test?currentSchema=sium", "checador","sistemas1010");
            Con=c;
            if (c != null) {
			System.out.println("Conexión exitosa");
		} else {
			System.out.println("Fallo la conexion");
		}
            return c;
                
        }catch(Exception e){
                e.printStackTrace();
                return null;
        }
        
    }
}
