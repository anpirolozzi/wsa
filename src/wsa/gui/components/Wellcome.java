package wsa.gui.components;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/** Incorpora la views Wellcome che mostra la schermata di benvenuto.*/
public class Wellcome {
    private static BorderPane wellcomePane;

    public Wellcome(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/Wellcome.fxml"));
            wellcomePane = new BorderPane(loader.load());
        } catch (IOException e) {}
    }

    /** Ritorna la schermata di benvenuto.
     * @return il {@code wellcomePane}*/
    public BorderPane getWellcomePane(){return wellcomePane;}
}
