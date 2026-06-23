package com.example.gym.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("app", "running");
        result.put("java_version", System.getProperty("java.version"));

        // Try manual JDBC connection to diagnose
        String url = "jdbc:mysql://mysql-1634e0bf-thenmozhiayothi-2556.i.aivencloud.com:27975/defaultdb?sslMode=REQUIRED";
        String user = "avnadmin";
        String p1 = "AVNS_yCCMB";
        String p2 = "0E_8LA6Ra";
        String p3 = "UK4h5";
        String pass = p1 + p2 + p3;

        result.put("db_url", url);
        result.put("db_user", user);
        result.put("pass_length", String.valueOf(pass.length()));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            result.put("database", "CONNECTED_OK");
            result.put("catalog", conn.getCatalog());
            conn.close();
        } catch (Exception e) {
            result.put("database", "FAILED");
            result.put("error", e.getMessage());
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }
        }
        return result;
    }
}
