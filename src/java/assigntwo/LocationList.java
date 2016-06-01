/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assigntwo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class LocationList extends HttpServlet {

    private PreparedStatement stmt;
    private Connection conn;
    private String dbDriver;
    private String dbUrl;
    private String dbTable;
    private String userName;
    private String password;

    public LocationList()
            throws SQLException, ClassNotFoundException, IOException, NamingException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("VisitorServletConfig.xml"));
        dbDriver = properties.get("dbDriver").toString();
        dbUrl = properties.get("dbUrl").toString();
        dbTable = properties.get("dbTable").toString();
        userName = properties.get("user").toString();
        password = properties.get("password").toString();

        Class.forName(dbDriver);
        conn = DriverManager.getConnection(dbUrl, userName, password);
        stmt = conn.prepareStatement("SELECT * FROM " + dbTable);
        
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject json = new JSONObject();
        JSONArray cities = new JSONArray();
        JSONObject result = new JSONObject();
        try {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                while (rs.next()) {
                    json = new JSONObject();
                    json.put("city", rs.getString("city"));
                    json.put("latitude", rs.getBigDecimal("latitude").toString());
                    json.put("longitude", rs.getBigDecimal("longitude").toString());
                    cities.add(json);
                }
                result.put("result", cities);
                System.out.println(cities.toString());
            } else {
                json.put("info", "fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result.toString());
    }
}
