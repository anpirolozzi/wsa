package wsa.gui.components;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wsa.gui.components.views.NewDialogController;

import java.io.IOException;

/** Incorpora la views NewDialog e mostra la finestra di dialogo per creare una nuova sessione di analisi.*/
public class NewDialog {
    private static Stage stage;
    private static Scene scene;

    public NewDialog(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/NewDialog.fxml"));
            scene=new Scene(loader.load());
            stage=new Stage();
            stage.setTitle("Nuova analisi");
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            NewDialogController controller = loader.getController();
            controller.setStage(stage);
            stage.showAndWait();
        }
        catch (IOException e) {}
    }
}
