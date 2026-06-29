package com.example.service;

import com.example.model.Transaksi;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportLaporanToCsv(List<Transaksi> list, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            writer.write("ID Transaksi,Tanggal,Kasir,Metode,Subtotal,Diskon,Total,Bayar,Kembalian");
            writer.newLine();

            for (Transaksi t : list) {
                writer.write(String.join(",",
                    t.getId(),
                    FMT.format(t.getTanggal()),
                    t.getKasirUsername(),
                    t.getMetodeBayar().name(),
                    String.valueOf((long) t.getSubtotal()),
                    (t.isDiskonPersen() ? t.getDiskon() + "%" : String.valueOf((long) t.getDiskon())),
                    String.valueOf((long) t.getTotal()),
                    String.valueOf((long) t.getBayar()),
                    String.valueOf((long) t.getKembalian())
                ));
                writer.newLine();
            }
        }
    }
}
