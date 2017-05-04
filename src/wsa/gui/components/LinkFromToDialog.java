package wsa.gui.components;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wsa.gui.SiteSession;
import wsa.gui.components.views.LinkFromToDialogController;

import java.io.IOException;
import java.util.LinkedList;

/** Incorpora la views LinkFromToDialog e mostra la finestra di dialogo per trovare il numero di link da un sito verso altro.*/
public class LinkFromToDialog {
    private static Stage stage;
    private static Scene scene;
    private LinkFromToDialogController controller;

    public LinkFromToDialog(LinkedList<SiteSession.LinkItem> linkItems){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/LinkFromToDialog.fxml"));
            scene=new Scene(loader.load());
            stage=new Stage();
            stage.setTitle("Link da un sito verso altro");
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            controller = loader.getController();
            controller.setStage(stage);
            controller.update(linkItems);
        }
        catch (IOException e) {}
    }

    /** Ritorna lo {@code stage}.
     * @return lo {@code stage}*/
    public Stage getStage(){return stage;}
}
