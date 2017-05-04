package wsa.gui.components;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wsa.gui.ExplorationGui;
import wsa.gui.components.views.SeedDialogController;
import wsa.web.SiteCrawler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/** Incorpora la views SeedDialog e mostra la finestra di dialogo per aggiungere e visualizzare i seed di un dominio.*/
public class SeedDialog {
    private Stage stage;
    private Scene scene;

    public SeedDialog(URI domain,Set<String> seedSet,SiteCrawler siteCrawler,ObservableList<String> dbSeed,ExplorationGui expGUI){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/SeedDialog.fxml"));
            scene=new Scene(loader.load());
            stage=new Stage();
            stage.setTitle("Seed");
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            SeedDialogController controller = loader.getController();
            controller.setDomain(domain);
            controller.setSeedSet(seedSet);
            controller.setSiteCrawler(siteCrawler);
            controller.setDbSeedListView(dbSeed);
            controller.setExplorationGui(expGUI);
            controller.setSeedListView(dbSeed);
            controller.initBrowseButton();
            controller.setStage(stage);
        } catch (IOException e) {}
    }

    /** Ritorna lo {@code stage}.
     * @return lo {@code stage}*/
    public Stage getStage(){return stage;}
}
