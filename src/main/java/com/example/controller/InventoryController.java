package com.example.controller;

import com.example.exception.ValidationException;
import com.example.model.Barang;
import com.example.model.Kategori;
import com.example.repository.BarangRepository;
import com.example.service.AudioManager;
import com.example.service.ImportService;
import com.example.service.InventoryService;
import com.example.util.AlertHelper;
import com.example.util.RupiahFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> kategoriFilter;
    @FXML private TableView<Barang> tableView;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, String> colKategori;
    @FXML private TableColumn<Barang, String> colBrand;
    @FXML private TableColumn<Barang, String> colUkuran;
    @FXML private TableColumn<Barang, String> colKondisi;
    @FXML private TableColumn<Barang, String> colHargaBeli;
    @FXML private TableColumn<Barang, String> colHargaJual;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Void> colAksi;

    private final InventoryService service = new InventoryService(new BarangRepository());
    private final ImportService importService = new ImportService();
    private final ObservableList<Barang> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKode()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori().name()));
        colBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));
        colUkuran.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUkuran().name()));
        colKondisi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKondisi().name()));
        colHargaBeli.setCellValueFactory(c -> new SimpleStringProperty(RupiahFormatter.format(c.getValue().getHargaBeli())));
        colHargaJual.setCellValueFactory(c -> new SimpleStringProperty(RupiahFormatter.format(c.getValue().getHargaJual())));
        colStok.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStok()));
        addAksiColumn();

        ObservableList<Kategori> kategoriOptions = FXCollections.observableArrayList();
        kategoriOptions.add(null);
        kategoriOptions.addAll(Kategori.values());
        kategoriFilter.setItems(kategoriOptions);
        kategoriFilter.getSelectionModel().select(null);

        searchField.textProperty().addListener((obs, oldV, newV) -> refresh());
        kategoriFilter.valueProperty().addListener((obs, oldV, newV) -> refresh());

        tableView.setItems(items);
        refresh();
    }

    private void addAksiColumn() {
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button hapusBtn = new Button("Hapus");
            private final HBox box = new HBox(6, editBtn, hapusBtn);

            {
                editBtn.getStyleClass().add("ghost-button");
                hapusBtn.getStyleClass().add("ghost-button");
                editBtn.setOnAction(e -> {
                    Barang b = getTableView().getItems().get(getIndex());
                    AudioManager.getInstance().playSFX("click");
                    openForm(b);
                });
                hapusBtn.setOnAction(e -> {
                    Barang b = getTableView().getItems().get(getIndex());
                    AudioManager.getInstance().playSFX("click");
                    if (AlertHelper.showConfirm("Hapus Barang", "Hapus '" + b.getNama() + "' (" + b.getKode() + ")?")) {
                        try {
                            service.delete(b.getKode());
                            refresh();
                        } catch (IOException ex) {
                            AudioManager.getInstance().playSFX("error");
                            AlertHelper.showError("Error", "Gagal menghapus: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleImport() {
        AudioManager.getInstance().playSFX("click");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Barang dari CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(tableView.getScene().getWindow());
        if (file == null) return;

        try {
            List<Barang> imported = importService.importBarangFromCsv(file.getAbsolutePath());
            int added = 0;
            int skipped = 0;
            for (Barang b : imported) {
                try {
                    service.create(b);
                    added++;
                } catch (ValidationException e) {
                    skipped++;
                }
            }
            AudioManager.getInstance().playSFX("success");
            AlertHelper.showInfo("Import Selesai",
                "Berhasil ditambahkan: " + added + " barang\nDilewati (duplikat/error): " + skipped);
            refresh();
        } catch (IOException | ValidationException e) {
            AudioManager.getInstance().playSFX("error");
            AlertHelper.showError("Import Gagal", e.getMessage());
        }
    }

    @FXML
    private void handleTambah() {
        AudioManager.getInstance().playSFX("click");
        openForm(null);
    }

    private void openForm(Barang existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/barang-form.fxml"));
            Parent root = loader.load();
            BarangFormController controller = loader.getController();
            controller.setContext(service, existing);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Tambah Barang" : "Edit Barang");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            refresh();
        } catch (IOException e) {
            AudioManager.getInstance().playSFX("error");
            AlertHelper.showError("Error", "Gagal membuka form: " + e.getMessage());
        }
    }

    private void refresh() {
        try {
            List<Barang> data;
            Kategori kat = kategoriFilter.getValue();
            String kw = searchField.getText();
            if (kat != null) {
                data = service.filterByKategori(kat);
            } else {
                data = service.findAll();
            }
            if (kw != null && !kw.isBlank()) {
                String lc = kw.toLowerCase();
                data = data.stream()
                        .filter(b ->
                            b.getNama().toLowerCase().contains(lc) ||
                            b.getBrand().toLowerCase().contains(lc) ||
                            b.getKode().toLowerCase().contains(lc))
                        .collect(Collectors.toList());
            }
            items.setAll(data);
        } catch (IOException e) {
            AlertHelper.showError("Error", "Gagal memuat barang: " + e.getMessage());
        }
    }
}
