package wsa.gui.components;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wsa.gui.components.views.StatDialogController;

import java.io.IOException;

/** Incorpora la views StatDialog e mostra la finestra di dialogo relativa alle statistiche di un dominio.*/
public class StatDialog {
    private Stage stage;
    private Scene scene;
    private StatDialogController controller;

    public StatDialog(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/StatDialog.fxml"));
            scene=new Scene(loader.load());
            stage=new Stage();
            stage.setTitle("Statistiche");
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            controller = loader.getController();
            controller.initChart();
            controller.setStage(stage);
        }
        catch (IOException e) {}
    }

    /** Ritorna lo {@code stage}.
     * @return lo {@code stage}*/
    public Stage getStage(){return stage;}
    /** Ritorna il controller collegato alla view inglobata.
     * @return il {@code controller}*/
    public StatDialogController getController(){return controller;}
}
