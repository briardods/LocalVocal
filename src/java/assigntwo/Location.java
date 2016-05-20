/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assigntwo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import javax.naming.NamingException;
import org.json.simple.JSONObject;

public class Location extends HttpServlet {

    private PreparedStatement stmt;
    private Connection conn;
    private String dbDriver;
    private String dbUrl;
    private String dbTable;
    private String latitude;
    private String longitude;
    private String userName;
    private String password;

    public Location()
            throws SQLException, ClassNotFoundException, IOException, NamingException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("VisitorServletConfig.xml"));
        dbDriver = properties.get("dbDriver").toString();
        dbUrl = properties.get("dbUrl").toString();
        dbTable = properties.get("dbTable").toString();
        latitude = properties.get("dbLat").toString();
        longitude = properties.get("dbLong").toString();
        userName = properties.get("user").toString();
        password = properties.get("password").toString();

        Class.forName(dbDriver);
        conn = DriverManager.getConnection(dbUrl, userName, password);
        stmt = conn.prepareStatement("SELECT * FROM " + dbTable + " WHERE (" + latitude + " BETWEEN ? AND ? ) AND ( " + longitude + " BETWEEN ? AND ? )");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject json = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        BigDecimal params[] = new BigDecimal[2];
        int i = 0;
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            params[i] = new BigDecimal(paramValues[0]);
            i++;
        }
        try {
            stmt.setBigDecimal(1, params[0].subtract(new BigDecimal(0.1)));
            stmt.setBigDecimal(2, params[0].add(new BigDecimal(0.1)));
            stmt.setBigDecimal(3, params[1].subtract(new BigDecimal(0.1)));
            stmt.setBigDecimal(4, (params[1].add(new BigDecimal(0.1))));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                InputStream inputStreamImage = new FileInputStream("C:\\Users\\Briar\\Documents\\School\\2016 Semester 1\\DMS\\AssignTwo\\src\\images\\" + rs.getString("image"));
                InputStream inputStreamAudio = new FileInputStream("C:\\Users\\Briar\\Documents\\School\\2016 Semester 1\\DMS\\AssignTwo\\src\\audio\\" + rs.getString("audio"));
                String encodedImage = encodeFile(inputStreamImage);
                String encodedAudio = encodeFile(inputStreamAudio);
                json.put("city", rs.getString("city"));
                json.put("population", rs.getInt("population"));
                json.put("image", encodedImage);
                json.put("audio", encodedAudio);
            } else {
                json.put("info", "fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(json);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
    }

    private String encodeFile(InputStream inputStream) {
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        String encodedString = new sun.misc.BASE64Encoder().encode(bytes);
        return encodedString;
    }
}
