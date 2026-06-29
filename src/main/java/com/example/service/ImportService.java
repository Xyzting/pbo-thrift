package com.example.service;

import com.example.exception.ValidationException;
import com.example.model.Barang;
import com.example.model.Kategori;
import com.example.model.Kondisi;
import com.example.model.Ukuran;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ImportService {

    public List<Barang> importBarangFromCsv(String filePath) throws IOException, ValidationException {
        List<Barang> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            boolean header = true;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                row++;
                String[] col = line.split(",", -1);
                if (col.length < 9) {
                    throw new ValidationException("Baris " + row + ": kolom tidak lengkap (butuh 9 kolom)");
                }
                try {
                    Barang b = new Barang(
                        col[0].trim(),
                        col[1].trim(),
                        Kategori.valueOf(col[2].trim().toUpperCase()),
                        col[3].trim(),
                        Ukuran.valueOf(col[4].trim().toUpperCase()),
                        Kondisi.valueOf(col[5].trim().toUpperCase()),
                        Double.parseDouble(col[6].trim()),
                        Double.parseDouble(col[7].trim()),
                        Integer.parseInt(col[8].trim()),
                        col.length > 9 ? col[9].trim() : null
                    );
                    result.add(b);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Baris " + row + ": nilai tidak valid — " + e.getMessage());
                }
            }
        }
        return result;
    }
}
