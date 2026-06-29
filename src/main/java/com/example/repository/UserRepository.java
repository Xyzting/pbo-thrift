package com.example.repository;

import com.example.model.Admin;
import com.example.model.Kasir;
import com.example.model.User;
import com.example.util.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public List<User> findAll() throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            List<User> result = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IOException("Gagal membaca data user: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUsername(String username) throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IOException("Gagal mencari user: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");
        String namaLengkap = rs.getString("nama_lengkap");
        String role = rs.getString("role");
        if ("ADMIN".equals(role)) return new Admin(username, password, namaLengkap);
        return new Kasir(username, password, namaLengkap);
    }
}
