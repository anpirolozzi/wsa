package wsa.gui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import wsa.web.LoadResult;
import wsa.web.html.Parsed;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Consumer;

/** Mostra le informazione richieste di un URI già analizzato da un {@link wsa.web.SiteCrawler}.*/
public class InfoUriGui {
    private URI dominio;
    private VBox vBox =new VBox();
    private HBox imgHbox = new HBox();
    private HBox parsingHbox = new HBox();
    private HBox domainHbox = new HBox();
    private Label imgLabel =new Label("Numero di immagini: ");
    private Text imgText =new Text("0");
    private Label parsingLabel =new Label("Numero di nodi dell'albero di parsing: ");
    private Text parsingText =new Text("0");
    private Label domainLabel =new Label("URI appartenente al dominio: ");
    private Text domainText =new Text("???");

    public InfoUriGui(URI dom){
        this.dominio=dom;
        imgHbox =new HBox(imgLabel, imgText);
        imgHbox.setVisible(false);
        parsingHbox =new HBox(parsingLabel, parsingText);
        parsingHbox.setVisible(false);
        domainHbox =new HBox(domainLabel, domainText);
        vBox =new VBox(domainHbox, imgHbox, parsingHbox);
        vBox.setPadding(new Insets(15, 15, 15, 15));
    }

    /**Permette l'aggiornamento delle informazioni interessate dall'URI coinvolto.
     * @param lr il {@link wsa.web.LoadResult} relativo all'URI analizzato.*/
    public void update(LoadResult lr){
        Platform.runLater(() -> {
            Boolean isInDomain=ApplicationSession.isInDomain(dominio, URI.create(String.valueOf(lr.url)));
            domainText.setText(String.valueOf(isInDomain));
            if(isInDomain){
                imgText.setText(String.valueOf(lr.parsed.getByTag("img").size()));
                imgHbox.setVisible(true);
                ArrayList<String> nodesList =new ArrayList<>();
                Consumer<Parsed.Node> countNodeConsumer = (Parsed.Node node) -> {
                    if(node.tag!=null){nodesList.add(node.tag);}
                };
                lr.parsed.visit(countNodeConsumer);
                parsingText.setText(String.valueOf(nodesList.size()));
                parsingHbox.setVisible(true);
            }
        });
    }

    /** Ritorna la Vbox che ingloba l'interfaccia.
     * @return il {@code vBox}.*/
    public VBox getvBox(){return vBox;}
}