package com.example.repository;

import com.example.model.ItemTransaksi;
import com.example.model.MetodeBayar;
import com.example.model.Transaksi;
import com.example.util.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaksiRepository {

    public List<Transaksi> findAll() throws IOException {
        try {
            return queryWithJoin("SELECT t.*, i.kode_barang, i.nama_barang, i.harga_beli AS item_hb, "
                + "i.harga_jual AS item_hj, i.qty, i.subtotal AS item_sub "
                + "FROM transaksi t LEFT JOIN item_transaksi i ON t.kode_transaksi = i.kode_transaksi "
                + "ORDER BY t.tanggal DESC");
        } catch (SQLException e) {
            throw new IOException("Gagal membaca transaksi: " + e.getMessage(), e);
        }
    }

    public List<Transaksi> findByDateRange(LocalDate from, LocalDate to) throws IOException {
        try {
            LocalDateTime start = from.atStartOfDay();
            LocalDateTime end = to.atTime(LocalTime.MAX);
            Connection conn = DatabaseConnection.get();
            String sql = "SELECT t.*, i.kode_barang, i.nama_barang, i.harga_beli AS item_hb, "
                + "i.harga_jual AS item_hj, i.qty, i.subtotal AS item_sub "
                + "FROM transaksi t LEFT JOIN item_transaksi i ON t.kode_transaksi = i.kode_transaksi "
                + "WHERE t.tanggal BETWEEN ? AND ? ORDER BY t.tanggal DESC";
            List<Transaksi> result = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet rs = ps.executeQuery()) {
                    result = buildFromResultSet(rs);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IOException("Gagal membaca transaksi: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<Transaksi> items) throws IOException {
        try {
            Connection conn = DatabaseConnection.get();
            String trxSql = "INSERT IGNORE INTO transaksi"
                + " (kode_transaksi, tanggal, kasir_username, diskon, diskon_persen,"
                + " subtotal, total, bayar, kembalian, metode_bayar)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String itemSql = "INSERT IGNORE INTO item_transaksi"
                + " (kode_transaksi, kode_barang, nama_barang, harga_beli, harga_jual, qty, subtotal)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psTrx = conn.prepareStatement(trxSql);
                 PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                for (Transaksi t : items) {
                    psTrx.setString(1, t.getId());
                    psTrx.setTimestamp(2, Timestamp.valueOf(t.getTanggal()));
                    psTrx.setString(3, t.getKasirUsername());
                    psTrx.setDouble(4, t.getDiskon());
                    psTrx.setBoolean(5, t.isDiskonPersen());
                    psTrx.setDouble(6, t.getSubtotal());
                    psTrx.setDouble(7, t.getTotal());
                    psTrx.setDouble(8, t.getBayar());
                    psTrx.setDouble(9, t.getKembalian());
                    psTrx.setString(10, t.getMetodeBayar().name());
                    psTrx.addBatch();

                    for (ItemTransaksi it : t.getItems()) {
                        psItem.setString(1, t.getId());
                        psItem.setString(2, it.getKodeBarang());
                        psItem.setString(3, it.getNamaBarang());
                        psItem.setDouble(4, it.getHargaBeli());
                        psItem.setDouble(5, it.getHargaJual());
                        psItem.setInt(6, it.getQty());
                        psItem.setDouble(7, it.getSubtotal());
                        psItem.addBatch();
                    }
                }
                psTrx.executeBatch();
                psItem.executeBatch();
            }
        } catch (SQLException e) {
            throw new IOException("Gagal menyimpan transaksi: " + e.getMessage(), e);
        }
    }

    public void reload() throws IOException {}

    private List<Transaksi> queryWithJoin(String sql) throws SQLException {
        Connection conn = DatabaseConnection.get();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return buildFromResultSet(rs);
        }
    }

    private List<Transaksi> buildFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Transaksi> map = new LinkedHashMap<>();
        while (rs.next()) {
            String kode = rs.getString("kode_transaksi");
            Transaksi t = map.get(kode);
            if (t == null) {
                t = new Transaksi();
                t.setId(kode);
                t.setTanggal(rs.getTimestamp("tanggal").toLocalDateTime());
                t.setKasirUsername(rs.getString("kasir_username"));
                t.setDiskon(rs.getDouble("diskon"));
                t.setDiskonPersen(rs.getBoolean("diskon_persen"));
                t.setSubtotal(rs.getDouble("subtotal"));
                t.setTotal(rs.getDouble("total"));
                t.setBayar(rs.getDouble("bayar"));
                t.setKembalian(rs.getDouble("kembalian"));
                t.setMetodeBayar(MetodeBayar.valueOf(rs.getString("metode_bayar")));
                t.setItems(new ArrayList<>());
                map.put(kode, t);
            }
            String kodeBarang = rs.getString("kode_barang");
            if (kodeBarang != null) {
                ItemTransaksi item = new ItemTransaksi(
                    kodeBarang,
                    rs.getString("nama_barang"),
                    rs.getDouble("item_hb"),
                    rs.getDouble("item_hj"),
                    rs.getInt("qty")
                );
                item.setSubtotal(rs.getDouble("item_sub"));
                t.getItems().add(item);
            }
        }
        return new ArrayList<>(map.values());
    }
}
