package wsa.gui.components.views;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import wsa.gui.ApplicationSession;
import wsa.gui.SiteSession;
import wsa.web.SiteCrawler;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/*Controller del NewDialog.*/
public class NewDialogController {
    private Stage stage;
    @FXML
    private TextField domainTextField;
    @FXML
    private CheckBox archivingCheckBox;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private MenuItem browseFileItem;
    @FXML
    private MenuItem browseFolderItem;

    /**Permette la selezione di una pagina web da usare come dominio.*/
    @FXML
    private void browseFile(){
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleziona il dominio da aprire");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pagine Web", "*.html", "*.htm");
            chooser.getExtensionFilters().add(extFilter);
            File selectedFile = chooser.showOpenDialog(stage);
            Path path = Paths.get(selectedFile.getAbsolutePath());
            this.domainTextField.setText(String.valueOf(path.toUri()));
        }catch (NullPointerException nexc){}
    }

    /*Permette la selezione di una directory da usare come dominio.*/
    @FXML
    private void browseFolder(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleziona il dominio da aprire");
            File selectedFile = chooser.showDialog(stage);
            Path path = Paths.get(selectedFile.getAbsolutePath());
            this.domainTextField.setText(String.valueOf(path.toUri()));
        }catch (NullPointerException nexc){}
    }

    /**Chiude lo {@code stage}*/
    @FXML
    private void closeStage() {stage.close();}

    /**Crea la sessione di analisi con i parametri specificati e si accerta della correttezza di tali.*/
    @FXML
    private void createSiteSession(){
        try{
            if(SiteCrawler.checkDomain(URI.create(String.valueOf(this.domainTextField.getText())))){
                Path path=null;
                if (!Objects.equals(String.valueOf(this.domainTextField.getText()), "")) {
                    if (this.archivingCheckBox.isSelected()) {
                        DirectoryChooser dirchooser= new DirectoryChooser();
                        dirchooser.setTitle("Seleziona la directory di archiviazione");
                        File selectedFile = dirchooser.showDialog(stage);
                        if(selectedFile!=null){
                            path = Paths.get(selectedFile.getAbsolutePath());
                        }
                        else return;
                    }
                    SiteSession siteSession= new SiteSession(URI.create(String.valueOf(this.domainTextField.getText())), path);
                    ApplicationSession.getSiteSessionList().add(siteSession);
                    Tab newTab = new Tab();
                    newTab.setText(String.valueOf(this.domainTextField.getText()));
                    newTab.setContent(ApplicationSession.getSiteSessionList().get(ApplicationSession.getSiteSessionList().size() - 1).getExplorationGui().getvBox());
                    newTab.setOnClosed(ct -> {
                        ApplicationSession.getSiteSessionList().remove(siteSession);
                        ApplicationSession.checkSiteTab();
                    });
                    ApplicationSession.getSiteSessionTabPane().getSelectionModel().select(newTab);
                    ApplicationSession.getSiteSessionTabPane().getTabs().add(newTab);
                    stage.close();
                    ApplicationSession.checkSiteTab();
                }
            }
            else{
                Alert alert= new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Dominio non valido");
                alert.setContentText("Assicurati di inserire un dominio valido.");
                alert.showAndWait();
            }
        }catch (Exception exc){
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(exc.getMessage());
            alert.setContentText("Assicurati di inserire un dominio valido.");
            alert.showAndWait();
        }
    }

    /** Imposta lo {@code stage}.
     * @param stage  il nuovo {@code stage} */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}