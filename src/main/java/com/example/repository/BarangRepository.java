package com.example.repository;

import com.example.model.Barang;
import com.example.model.Kategori;
import com.example.model.Kondisi;
import com.example.model.Ukuran;
import com.example.util.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BarangRepository {

    public List<Barang> findAll() throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            List<Barang> result = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM barang ORDER BY kode")) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IOException("Gagal membaca data barang: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<Barang> items) throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            conn.setAutoCommit(false);
            try {
                try (Statement del = conn.createStatement()) {
                    del.execute("DELETE FROM barang");
                }
                String sql = "INSERT INTO barang (kode, nama, kategori, brand, ukuran, kondisi,"
                    + " harga_beli, harga_jual, stok, path_gambar)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (Barang b : items) {
                        ps.setString(1, b.getKode());
                        ps.setString(2, b.getNama());
                        ps.setString(3, b.getKategori().name());
                        ps.setString(4, b.getBrand());
                        ps.setString(5, b.getUkuran().name());
                        ps.setString(6, b.getKondisi().name());
                        ps.setDouble(7, b.getHargaBeli());
                        ps.setDouble(8, b.getHargaJual());
                        ps.setInt(9, b.getStok());
                        ps.setString(10, b.getPathGambar());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IOException("Gagal menyimpan data barang: " + e.getMessage(), e);
        }
    }

    public Optional<Barang> findByKode(String kode) throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM barang WHERE kode = ?")) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IOException("Gagal mencari barang: " + e.getMessage(), e);
        }
    }

    public List<Barang> search(String keyword) throws IOException {
        if (keyword == null || keyword.isBlank()) return findAll();
        String lc = keyword.toLowerCase();
        return findAll().stream()
                .filter(b ->
                    b.getNama().toLowerCase().contains(lc) ||
                    b.getBrand().toLowerCase().contains(lc) ||
                    b.getKode().toLowerCase().contains(lc))
                .collect(Collectors.toList());
    }

    public void reload() throws IOException {}

    private Barang mapRow(ResultSet rs) throws SQLException {
        return new Barang(
            rs.getString("kode"),
            rs.getString("nama"),
            Kategori.valueOf(rs.getString("kategori")),
            rs.getString("brand"),
            Ukuran.valueOf(rs.getString("ukuran")),
            Kondisi.valueOf(rs.getString("kondisi")),
            rs.getDouble("harga_beli"),
            rs.getDouble("harga_jual"),
            rs.getInt("stok"),
            rs.getString("path_gambar")
        );
    }
}
