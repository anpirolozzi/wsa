package wsa.gui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import wsa.web.LoadResult;

/** Rappresenta un interfaccia grafica per mostrare i link contenuti in un URI già analizzato da un {@link wsa.web.SiteCrawler}.*/
public class LinkContainedGui {
    private VBox vBox =new VBox();
    private HBox listHbox;
    private HBox sizeHbox;
    private ObservableList<String> dbLinkContainedListView = FXCollections.observableArrayList();
    private ListView<String> linkContainedListView;
    private Label sizeLabel =new Label("Numero di link contenuti ");
    private Text sizeText =new Text("0");

    public LinkContainedGui(){
        linkContainedListView = new ListView<String>(dbLinkContainedListView);
        linkContainedListView.setPrefHeight(2160);
        listHbox =new HBox(linkContainedListView);
        listHbox.setHgrow(linkContainedListView, Priority.ALWAYS);
        sizeHbox =new HBox(sizeLabel, sizeText);
        vBox = new VBox(sizeHbox, listHbox);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setVgrow(linkContainedListView, Priority.ALWAYS);
    }

    /**Permette l'aggiornamento dei link contenuti nella pagina.
     * @param lr il {@link wsa.web.LoadResult} relativo all'URI analizzato.*/
    public void update(LoadResult lr){
        Platform.runLater(() -> {
            dbLinkContainedListView.clear();
            String linksList[] = new String[lr.parsed.getLinks().size()];
            lr.parsed.getLinks().toArray(linksList);
            dbLinkContainedListView.addAll(linksList);
            sizeText.setText(String.valueOf(lr.parsed.getLinks().size()));
        });
    }

    /** Ritorna la Vbox che ingloba l'interfaccia.
     * @return il {@code vBox}.*/
    public VBox getvBox(){return vBox;}
}
