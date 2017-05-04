package wsa.gui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URI;
import java.util.LinkedList;
import java.util.Objects;

/** Rappresenta l'interfaccia grafica principale di un {@link wsa.gui.SiteSession}.*/
public class ExplorationGui {
    private URI dominio;
    private LinkedList<SiteSession.LinkItem> pointedLinkList =new LinkedList<SiteSession.LinkItem>();
    private ObservableList<SiteSession.UriCrawled> dbUriCrawledTableView = FXCollections.observableArrayList();
    private TableView<SiteSession.UriCrawled> uriCrawledTableView = new TableView<SiteSession.UriCrawled>();
    private TableColumn uriTableColumn = new TableColumn("URI");
    private TableColumn stateTableColumn = new TableColumn("Stato");
    private Button execButton = new Button("Avvia",new ImageView(new Image(getClass().getResource("/icons/play.png").toString())));
    private Button seedButton = new Button ("Seed",new ImageView(new Image(getClass().getResource("/icons/seed.png").toString())));
    private Button stopButton = new Button ("Ferma",new ImageView(new Image(getClass().getResource("/icons/stop.png").toString())));
    private MenuButton advancedMenuButton = new MenuButton("Strumenti",new ImageView(new Image(getClass().getResource("/icons/tools.png").toString())));
    private MenuItem statsMenuItem = new MenuItem("Statistiche");
    private MenuItem linkFromToMenuItem = new MenuItem("Link da questo sito verso...");
    private Label statusLabel = new Label("Stato: ");
    private Text statusText = new Text("In attesa di aggiunta seed");
    private Label processedLabel = new Label("Processati");
    private Text processedText = new Text("0");
    private Label queueLabel = new Label("In coda");
    private Text queueText = new Text("0");
    private Separator separator= new Separator();
    private ToolBar navToolBar= new ToolBar(advancedMenuButton, execButton, stopButton, seedButton);
    private ToolBar infoToolbar = new ToolBar(statusLabel, statusText,separator, processedLabel, processedText, queueLabel, queueText);
    private HBox tableViewHbox = new HBox (uriCrawledTableView);
    private VBox vBox = new VBox(navToolBar, tableViewHbox, infoToolbar);

    public ExplorationGui(URI dom){
        this.dominio=dom;
        initGui();
        initAction();
    }

    /** Imposta l'interfaccia grafica (senza azioni).*/
    private void initGui(){
        uriTableColumn.setCellValueFactory(new PropertyValueFactory<SiteSession.UriCrawled, String>("uri"));
        uriTableColumn.prefWidthProperty().bind(uriCrawledTableView.widthProperty().multiply(0.70));
        stateTableColumn.setCellValueFactory(new PropertyValueFactory<SiteSession.UriCrawled, String>("state"));
        stateTableColumn.prefWidthProperty().bind(uriCrawledTableView.widthProperty().multiply(0.25));
        uriCrawledTableView.setItems(dbUriCrawledTableView);
        uriCrawledTableView.getColumns().addAll(uriTableColumn, stateTableColumn);
        uriCrawledTableView.setPrefHeight(2160);
        uriCrawledTableView.setPlaceholder(new Label("Nessun URI analizzato. Assicurati di aver avviato l'analisi."));
        uriCrawledTableView.autosize();
        uriCrawledTableView.setEditable(false);
        tableViewHbox.setHgrow(uriCrawledTableView, Priority.ALWAYS);
        vBox.setVgrow(uriCrawledTableView, Priority.ALWAYS);
        advancedMenuButton.getItems().addAll(statsMenuItem,linkFromToMenuItem);
        advancedMenuButton.setAlignment(Pos.CENTER_RIGHT);
        advancedMenuButton.setDisable(true);
        execButton.setDisable(true);
        stopButton.setDisable(true);
        processedLabel.setVisible(false);
        processedText.setVisible(false);
        queueLabel.setVisible(false);
        queueText.setVisible(false);
    }

    /** Imposta le azioni dell'interfaccia grafica.*/
    private void initAction(){
        uriCrawledTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (uriCrawledTableView.getSelectionModel().getSelectedItem() != null) {
                TableView.TableViewSelectionModel selectionModel = uriCrawledTableView.getSelectionModel();
                ObservableList selectedCells = selectionModel.getSelectedCells();
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);
                Object selected = uriCrawledTableView.getItems().get(tablePosition.getRow()).getUri();
                Object status= uriCrawledTableView.getItems().get(tablePosition.getRow()).getState();
                if(!Objects.equals(String.valueOf(status), "Scaricato")){
                    Alert alert= new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText("Impossibile visualizzare i dettagli");
                    alert.setContentText("L'URI selezionato non è stato scaricato con successo.");
                    alert.showAndWait();
                }
                else {
                    PageGui pageGui = new PageGui(dominio, URI.create(String.valueOf(selected)), pointedLinkList);
                    pageGui.getStage().show();
                    pageGui.getStage().toFront();
                }
            }
        });

    }

    /** Predispone l'interfaccia nello stato di pronto, cioè quando si aspetta il primo avvio dell'utente.*/
    public void setReadyStatus(){
        this.statusText.setText("Pronto per l'esecuzione");
        this.execButton.setDisable(false);
    }

    /** Predispone l'interfaccia nello stato di esecuzione.*/
    public void setExecutingStatus(){
        advancedMenuButton.setDisable(false);
        execButton.setText("Sospendi");
        execButton.setGraphic(new ImageView(new Image(getClass().getResource("/icons/pause.png").toString())));
        execButton.setDisable(false);
        stopButton.setDisable(false);
        statusText.setText("Analisi in esecuzione");
        processedLabel.setVisible(true);
        processedText.setVisible(true);
        queueLabel.setVisible(true);
        queueText.setVisible(true);
    }

    /** Predispone l'interfaccia nello stato di sospensione.*/
    public void setSuspendedStatus(){
        advancedMenuButton.setDisable(false);
        execButton.setText("Avvia");
        execButton.setGraphic(new ImageView(new Image(getClass().getResource("/icons/play.png").toString())));
        execButton.setDisable(false);
        stopButton.setDisable(false);
        statusText.setText("Analisi in attesa");
        processedLabel.setVisible(true);
        processedText.setVisible(true);
        queueLabel.setVisible(true);
        queueText.setVisible(true);
    }

    /** Predispone l'interfaccia nello stato di fermo, cioè quando l'analisi viene terminata dall'utente.*/
    public void setStoppedStatus(){
        execButton.setDisable(true);
        stopButton.setDisable(true);
        seedButton.setDisable(true);
        statusText.setText("Analisi arrestata");
        queueLabel.setVisible(false);
        queueText.setVisible(false);
    }

    /** Ritorna gli {@link wsa.gui.SiteSession.UriCrawled} relativi agli URI analizzati.
     * @return la {@code dbUriCrawledTableView}.*/
    public ObservableList<SiteSession.UriCrawled> getDbUriCrawledTableView(){return dbUriCrawledTableView;}
    /** Ritorna la VBox che ingloba l'interfaccia grafica.
     * @return la {@code vBox}.*/
    public VBox getvBox(){return vBox;}
    /** Ritorna la Hbox contenente la TableView.
     * @return la {@code tableViewHbox}.*/
    public HBox getTableViewHbox(){return tableViewHbox;}
    /**Ritorna il menu per visualizzare le statistiche di un sito.
     * @return lo {@code statsMenuItem}.*/
    public MenuItem getStatsMenuItem(){return statsMenuItem;}
    /**Ritorna il menu per visualizzare i link da un sito verso un altro.
     * @return il {@code linkFromToMenuItem}.*/
    public MenuItem getLinkFromToMenuItem(){return linkFromToMenuItem;}
    /** Ritorna il bottone che permette di sospendere ed avviare un analisi.
     * @return lo {@code execButton}.*/
    public Button getExecButton(){return execButton;}
    /** Ritorna il bottone che permette di fermare un analisi.
     * @return lo {@code stopButton}.*/
    public Button getStopButton(){return stopButton;}
    /** Ritorna il bottone che permette di visualizzare la {@link wsa.gui.components.SeedDialog} relativa al dominio.
     * @return il {@code seedButton}.*/
    public Button getSeedButton(){return seedButton;}
    /** Ritorna la Text contenente il numero di Uri analizzati.
     * @return la {@code processedText}.*/
    public Text getProcessedText(){return processedText;}
    /** Ritorna la Text contenente il numero di Uri in coda di analisi in un {@link wsa.web.SiteCrawler}.
     * @return la {@code queueText}.*/
    public Text getQueueText(){return queueText;}

    /** Imposta la {@code pointedLinkList}.
     * @param list  la nuova {@code pointedLinkList}.*/
    public void setPointedLinkList(LinkedList<SiteSession.LinkItem> list){this.pointedLinkList=list;}
}