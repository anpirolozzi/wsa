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

import java.net.URI;
import java.util.LinkedList;

/** Rappresenta un interfaccia grafica per mostrare i link che puntano ad una pagina già analizzata da un {@link wsa.web.SiteCrawler}.*/
public class LinkPointingGui {
    private VBox vbox=new VBox();
    private HBox sizeHbox;
    private HBox listHbox;
    private ObservableList<String> dbLinkPointingListView = FXCollections.observableArrayList();
    private ListView<String> linkPointingListView;
    private Label sizeLabel =new Label("Numero di link puntanti ");
    private Text sizeText =new Text("0");

    public LinkPointingGui(){
        linkPointingListView = new ListView<String>(dbLinkPointingListView);
        linkPointingListView.setPrefHeight(2160);
        listHbox =new HBox(linkPointingListView);
        listHbox.setHgrow(linkPointingListView, Priority.ALWAYS);
        sizeHbox =new HBox(sizeLabel, sizeText);
        vbox= new VBox(sizeHbox, listHbox);
        vbox.setPadding(new Insets(5,5,5,5));
        vbox.setVgrow(linkPointingListView,Priority.ALWAYS);
    }

    /**Permette l'aggiornamento dei link che puntano alla pagina.
     * @param uri lo URI del quale si vuole aggiornare l'informazione.
     * @param linkItemsList  la lista dei {@link wsa.gui.SiteSession.LinkItem} finora trovati.*/
    public void update(URI uri, LinkedList<SiteSession.LinkItem> linkItemsList){
        Platform.runLater(() -> {
            dbLinkPointingListView.clear();
            if(!linkItemsList.isEmpty()) {
                for (SiteSession.LinkItem li : linkItemsList) {
                    if (String.valueOf(li.assoluto).equals(uri.toString())) {
                        dbLinkPointingListView.add(li.link.toString());
                    }
                }
                sizeText.setText(String.valueOf(dbLinkPointingListView.size()));
            }
        });
    }

    /** Ritorna la Vbox che ingloba l'interfaccia.
     * @return il {@code vBox}.*/
    public VBox getvbox(){return vbox;}
}
