package wsa.gui;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/** Un oggetto {@code WebViewGui} permette di visualizzare una pagina web inglobandola in altre interfacce agevolmente
 * o di mostrarla come finestra separata.*/
public class WebViewGui {
    private Stage stage= new Stage();
    private VBox vBox;
    private WebView webView = new WebView();
    private WebEngine webEngine = webView.getEngine();

    public WebViewGui(){
        vBox = new VBox(webView);
        vBox.setVgrow(webView, Priority.ALWAYS);
        Scene scene = new Scene(vBox, 640, 480);
        stage.setScene(scene);
    }

    /**Permette il caricamento della pagina selezionata.
     * @param uri la pagina da caricare.*/
    public void loadURI(String uri) {webEngine.load(uri);}

    /** Ritorna la Vbox che ingloba l'interfaccia.
     * @return lo {@code vBox}.*/
    public VBox getvBox(){return vBox;}
    /** Ritorna lo {@code stage}.
     * @return lo {@code stage}.*/
    public Stage getStage(){return stage;}
    /** Imposta il titolo dello {@code stage}.
     * @param s  il nuovo titolo di {@code stage}.*/
    public void setTitle(String s){stage.setTitle(s);}
}
