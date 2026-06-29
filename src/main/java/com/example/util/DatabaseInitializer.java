package com.example.util;

import com.example.model.Admin;
import com.example.model.Barang;
import com.example.model.Kasir;
import com.example.model.Kategori;
import com.example.model.Kondisi;
import com.example.model.Ukuran;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void init() throws SQLException {
        createDatabase();
        createTables();
        seedIfEmpty();
    }

    private static void createDatabase() throws SQLException {
        try (Connection root = DatabaseConnection.getRoot();
             Statement st = root.createStatement()) {
            st.execute("CREATE DATABASE IF NOT EXISTS umkm_thrift"
                + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private static void createTables() throws SQLException {
        Connection conn = DatabaseConnection.get();
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(50) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "nama_lengkap VARCHAR(100) NOT NULL,"
                + "role VARCHAR(10) NOT NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS barang ("
                + "kode VARCHAR(20) NOT NULL PRIMARY KEY,"
                + "nama VARCHAR(100) NOT NULL,"
                + "kategori VARCHAR(30) NOT NULL,"
                + "brand VARCHAR(100),"
                + "ukuran VARCHAR(20) NOT NULL,"
                + "kondisi VARCHAR(20) NOT NULL,"
                + "harga_beli DECIMAL(15,2) NOT NULL,"
                + "harga_jual DECIMAL(15,2) NOT NULL,"
                + "stok INT NOT NULL DEFAULT 0,"
                + "path_gambar VARCHAR(500)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS transaksi ("
                + "kode_transaksi VARCHAR(30) NOT NULL PRIMARY KEY,"
                + "tanggal DATETIME NOT NULL,"
                + "kasir_username VARCHAR(50) NOT NULL,"
                + "diskon DECIMAL(15,2) NOT NULL DEFAULT 0,"
                + "diskon_persen TINYINT(1) NOT NULL DEFAULT 0,"
                + "subtotal DECIMAL(15,2) NOT NULL,"
                + "total DECIMAL(15,2) NOT NULL,"
                + "bayar DECIMAL(15,2),"
                + "kembalian DECIMAL(15,2),"
                + "metode_bayar VARCHAR(20) NOT NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS item_transaksi ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "kode_transaksi VARCHAR(30) NOT NULL,"
                + "kode_barang VARCHAR(20) NOT NULL,"
                + "nama_barang VARCHAR(100) NOT NULL,"
                + "harga_beli DECIMAL(15,2) NOT NULL,"
                + "harga_jual DECIMAL(15,2) NOT NULL,"
                + "qty INT NOT NULL,"
                + "subtotal DECIMAL(15,2) NOT NULL,"
                + "UNIQUE KEY uq_trx_barang (kode_transaksi, kode_barang),"
                + "FOREIGN KEY (kode_transaksi) REFERENCES transaksi(kode_transaksi)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static void seedIfEmpty() throws SQLException {
        Connection conn = DatabaseConnection.get();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        seedUsers(conn);
        seedBarang(conn);
    }

    private static void seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password, nama_lengkap, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"admin", "admin123", "Admin Toko", "ADMIN"},
                {"kasir", "kasir123", "Kasir Satu", "KASIR"}
            };
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedBarang(Connection conn) throws SQLException {
        String sql = "INSERT INTO barang (kode, nama, kategori, brand, ukuran, kondisi,"
            + " harga_beli, harga_jual, stok, path_gambar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[][] seed = {
            {"BR-001", "Kemeja Flanel Kotak", Kategori.ATASAN, "Uniqlo", Ukuran.M, Kondisi.LIKE_NEW, 45000, 95000, 1, "placeholder.png"},
            {"BR-002", "Jeans Skinny Dark", Kategori.BAWAHAN, "H&M", Ukuran.L, Kondisi.VERY_GOOD, 40000, 85000, 1, "placeholder.png"},
            {"BR-003", "Jaket Bomber Hitam", Kategori.OUTER, "Zara", Ukuran.L, Kondisi.GOOD, 80000, 185000, 1, "placeholder.png"},
            {"BR-004", "Dress Motif Bunga", Kategori.DRESS, "H&M", Ukuran.M, Kondisi.LIKE_NEW, 55000, 125000, 1, "placeholder.png"},
            {"BR-005", "Topi Baseball Nike", Kategori.AKSESORIS, "Nike", Ukuran.ALL_SIZE, Kondisi.VERY_GOOD, 25000, 65000, 2, "placeholder.png"},
            {"BR-006", "Kaos Polos Navy", Kategori.ATASAN, "Uniqlo", Ukuran.S, Kondisi.LIKE_NEW, 20000, 55000, 1, "placeholder.png"},
            {"BR-007", "Celana Cargo Hijau", Kategori.BAWAHAN, "Adidas", Ukuran.L, Kondisi.GOOD, 60000, 135000, 1, "placeholder.png"},
            {"BR-008", "Hoodie Oversize", Kategori.ATASAN, "Champion", Ukuran.XL, Kondisi.VERY_GOOD, 75000, 165000, 1, "placeholder.png"},
            {"BR-009", "Kemeja Denim Biru", Kategori.ATASAN, "Levi's", Ukuran.M, Kondisi.GOOD, 50000, 110000, 1, "placeholder.png"},
            {"BR-010", "Rok Plisket Hitam", Kategori.BAWAHAN, "Zara", Ukuran.S, Kondisi.LIKE_NEW, 45000, 98000, 1, "placeholder.png"},
            {"BR-011", "Coat Panjang Coklat", Kategori.OUTER, "Uniqlo", Ukuran.L, Kondisi.VERY_GOOD, 120000, 245000, 1, "placeholder.png"},
            {"BR-012", "Dress Midi Polos", Kategori.DRESS, "H&M", Ukuran.M, Kondisi.GOOD, 50000, 115000, 1, "placeholder.png"},
            {"BR-013", "Tas Selempang Canvas", Kategori.AKSESORIS, "Bershka", Ukuran.ALL_SIZE, Kondisi.VERY_GOOD, 35000, 78000, 1, "placeholder.png"},
            {"BR-014", "Sweater Rajut Krem", Kategori.ATASAN, "Uniqlo", Ukuran.L, Kondisi.LIKE_NEW, 65000, 145000, 1, "placeholder.png"},
            {"BR-015", "Celana Chino Khaki", Kategori.BAWAHAN, "Dockers", Ukuran.M, Kondisi.GOOD, 45000, 98000, 1, "placeholder.png"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : seed) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, ((Kategori) row[2]).name());
                ps.setString(4, (String) row[3]);
                ps.setString(5, ((Ukuran) row[4]).name());
                ps.setString(6, ((Kondisi) row[5]).name());
                ps.setDouble(7, ((Number) row[6]).doubleValue());
                ps.setDouble(8, ((Number) row[7]).doubleValue());
                ps.setInt(9, (Integer) row[8]);
                ps.setString(10, (String) row[9]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
