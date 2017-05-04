package wsa.gui.components.views;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import wsa.gui.ExplorationGui;
import wsa.web.SiteCrawler;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/*Controller del SeedDialog.*/
public class SeedDialogController {
    private ObservableList<String> dbSeedListView;
    private Set<String> seedSet;
    private URI domain;
    private SiteCrawler siteCrawler;
    private ExplorationGui explorationGui;
    private Stage stage;
    @FXML
    private TextField newSeedTextField;
    @FXML
    private Button browseButton;
    @FXML
    private ListView<String> seedListView;
    @FXML
    private Button closeButton;
    @FXML
    private Button addButton;

    /** Permette la selezione di un seed da file nel caso dominio specificato sia locale.*/
    @FXML
    private void browse(){
        if(domain.getScheme().contains("file")){
            FileChooser chooser=new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pagine Web", "*.html", "*.htm");
            chooser.getExtensionFilters().add(extFilter);
            chooser.setTitle("Seleziona un Uri Seed");
            File selectedFile = chooser.showOpenDialog(stage);
            if(selectedFile!=null) {
                Path path = Paths.get(selectedFile.getAbsolutePath());
                this.newSeedTextField.setText(String.valueOf(path.toUri()));
            }
        }
    }

    /**Cancella eventuali input incompleti e chiude lo {@code stage}*/
    @FXML
    private void close() {
        newSeedTextField.clear();
        stage.close();
    }

    /**Permette l'aggiunta di seed nel {@link wsa.web.SiteCrawler} ed aggiorna l'interfaccia nel caso le condizioni lo permettano.
     * @throws IllegalStateException nel caso in cui lo Uri sia gia stato analizzato.
     * @throws IllegalArgumentException nel caso in cui lo Uri non sia leggittimo come seed.*/
    @FXML
    private void addSeed(){
        try {
            if((!siteCrawler.getErrors().contains(URI.create(newSeedTextField.getText())))&&(!siteCrawler.getLoaded().contains(URI.create(newSeedTextField.getText())))){
                if(!newSeedTextField.getText().contains("file:///")||(newSeedTextField.getText().contains("file:///")&&(newSeedTextField.getText().endsWith(".htm")||newSeedTextField.getText().endsWith(".html")))){
                    seedListView.setItems(dbSeedListView);
                    siteCrawler.addSeed(URI.create(newSeedTextField.getText()));
                    seedSet.add(String.valueOf(newSeedTextField.getText()));
                    dbSeedListView.removeAll(seedSet);
                    dbSeedListView.addAll(seedSet);
                    explorationGui.setReadyStatus();
                    newSeedTextField.clear();}
                else {throw new IllegalArgumentException("URI Seed non valido");}}
            else {throw new IllegalStateException("URI Seed già analizzato");}
        }
        catch (IllegalArgumentException|NullPointerException argexc){
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("URI Seed non valido");
            alert.setContentText("Assicurati di inserire un seed compatibile col dominio specificato.");
            alert.showAndWait();
        }
        catch (IllegalStateException exc){
            Alert alert= new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informazione");
            alert.setHeaderText("URI Seed già analizzato");
            alert.setContentText("Assicurati di inserire un seed che non sia già stato analizzato.");
            alert.showAndWait();
        }
    }

    /** Permette la disabilitazione del bottone che permette la selezione del seed tramite FileChooser*/
    public void disableBrowseButton(Boolean b){browseButton.setDisable(b);}

    /**Inizializza il {@code browseButton} che permette la selezione del seed tramite FileChooser*/
    public void initBrowseButton(){
        if(domain.getScheme().contains("file")){
            disableBrowseButton(false);
        }
    }

    /** Imposta gli elementi di {@code seedListView}.
     * @param dbSeed  la lista degli elementi da impostare in {@code seedListView} */
    public void setSeedListView(ObservableList<String> dbSeed){seedListView.setItems(dbSeed);}
    /** Imposta lo {@code stage}.
     * @param stage  il nuovo {@code stage} */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    /** Imposta il {@code domain}.
     * @param dom  il nuovo {@code domain} */
    public void setDomain(URI dom){this.domain=dom;}
    /** Imposta il {@code dbSeedListView} ossia gli elementi che visualizza {@code seedListView}.
     * @param db la nuova {@code dbSeedListView} */
    public void setDbSeedListView(ObservableList<String> db){this.dbSeedListView =db;}
    /** Imposta il {@code seedSet} ossia il set dei seed inseriti.
     * @param set  il nuovo {@code seedSet} */
    public void setSeedSet(Set<String> set){this.seedSet=set;}
    /** Imposta il {@code siteCrawler}.
     * @param sitecrawl  il nuovo {@code siteCrawler} */
    public void setSiteCrawler(SiteCrawler sitecrawl){this.siteCrawler=sitecrawl;}
    /** Imposta la {@code explorationGui}.
     * @param expGUI  la nuova {@code explorationGui} */
    public void setExplorationGui(ExplorationGui expGUI){this.explorationGui =expGUI;}
}