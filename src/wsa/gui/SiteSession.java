package wsa.gui;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import wsa.gui.components.LinkFromToDialog;
import wsa.gui.components.SeedDialog;
import wsa.gui.components.StatDialog;
import wsa.web.LoadResult;
import wsa.web.SiteCrawler;
import wsa.web.WebFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Rappresenta un istanza di analisi. Contiene un {@link wsa.web.SiteCrawler} e i dati principali scaturiti dall'analisi.
 * Gestisce l'interfacciamento con le funzionalità secondarie relative ad un dominio.*/
public class SiteSession {
    private URI domain;
    private Path path;
    private Boolean archiveOpened=true;
    private SiteCrawler siteCrawler;
    private Set<String> seedSet = new HashSet<>();
    private Set<URI> uriCrawledSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private LinkedList<LinkItem> linkItemsList =new LinkedList<LinkItem>();
    private ExplorationGui explorationGui;
    private SeedDialog seedDialog;
    private StatDialog statDialog;
    private LinkFromToDialog linkFromToDialog;
    private int maxlink = 0;
    private int maxPointedLink =0;
    private int internalUri = 0;
    private int internalLoadedUri =0;
    private int loadedUri =0;
    private int errorUri =0;

    public SiteSession(URI dom, Path pat){
        this.domain=dom;
        this.path=pat;
        try{
            siteCrawler= WebFactory.getSiteCrawler(domain,path);
        }
        catch (IOException ioexc){
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("IOException");
            alert.setContentText("Errore di lettura/scrittura.");
            alert.showAndWait();
        }
        ObservableList<String> dbSeed = FXCollections.observableArrayList();
        if(domain==null && path!=null){
            domain=getDomainFromArchive(path);
            for(URI seed:siteCrawler.getToLoad()){
                seedSet.add(seed.toString());
                dbSeed.addAll(seedSet);
            }
        }
        explorationGui = new ExplorationGui(domain);
        seedDialog=new SeedDialog(domain,seedSet,siteCrawler, dbSeed, explorationGui);
        statDialog=new StatDialog();
        initAction();
        if(dom==null && pat!=null){
            siteCrawler.start();
            archiveOpened=false;
            if(siteCrawler.isRunning()){
                explorationGui.setExecutingStatus();
                update();
            }
            else{
                siteCrawler.suspend();
                explorationGui.setSuspendedStatus();
                update();
            }
        }
    }

    /** Imposta ulteriori azioni della {@link wsa.gui.ExplorationGui} correlata alla sessione.*/
    private void initAction(){
        explorationGui.getSeedButton().setOnAction(e -> {seedDialog.getStage().showAndWait();});

        explorationGui.getStatsMenuItem().setOnAction(e -> {statDialog.getStage().showAndWait();});

        explorationGui.getStopButton().setOnAction(e -> {
            try {
                siteCrawler.cancel();
                explorationGui.setStoppedStatus();
            } catch (Exception exc) {explorationGui.setStoppedStatus();}
        });

        explorationGui.getLinkFromToMenuItem().setOnAction(e -> {
            linkFromToDialog = new LinkFromToDialog(linkItemsList);
            linkFromToDialog.getStage().showAndWait();
        });

        explorationGui.getExecButton().setOnAction(e -> {
            if (siteCrawler.isRunning()) {
                siteCrawler.suspend();
                explorationGui.setSuspendedStatus();
            } else if (!siteCrawler.getToLoad().isEmpty()) {
                siteCrawler.start();
                explorationGui.setExecutingStatus();
                update();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Attenzione");
                alert.setHeaderText("Nessun URI da scaricare");
                alert.setContentText("Non è previsto il download di altri URI. Aggiungi nuovi URI Seed per continuare.");
                alert.showAndWait();
            }
        });
    }

    /**Esegue l'aggiornamento dei dati della sessione e dell'interfaccia relativa. Aggiorna i dati solamente quando il
     * {@link wsa.web.SiteCrawler} è in esecuzione o all'apertura dell'archivio. */
    private void update() {
        Runnable taskUpdate = () -> {
            while (siteCrawler.isRunning()||!archiveOpened){
                try {
                    archiveOpened=true;
                    ConcurrentLinkedQueue<URI> queueLoaded= new ConcurrentLinkedQueue(siteCrawler.getLoaded());
                    ConcurrentLinkedQueue<URI> queueToLoad= new ConcurrentLinkedQueue(siteCrawler.getToLoad());
                    ConcurrentLinkedQueue<URI> queueError= new ConcurrentLinkedQueue(siteCrawler.getErrors());
                    //Inizializzo le componenti grafiche garantendo coerenza con i dati
                    if (loadedUri <(queueLoaded.size())){
                        loadedUri =queueLoaded.size();
                        statDialog.getController().setVisitedUriText(loadedUri);
                    }
                    if (errorUri <=(queueError.size())){
                        errorUri =queueError.size();
                        statDialog.getController().setErrorsUriText(errorUri);
                    }
                    explorationGui.getProcessedText().setText(String.valueOf(loadedUri + errorUri));
                    explorationGui.getQueueText().setText(String.valueOf(queueToLoad.size()));
                    statDialog.getController().setMaxLinkPointingUriText(maxPointedLink);
                    statDialog.getController().updateChart(loadedUri, internalLoadedUri, errorUri);
                    explorationGui.setPointedLinkList(linkItemsList);
                    statDialog.getController().setInternalUriText(internalUri);
                    statDialog.getController().setMaxLinkUriText(maxlink);
                    //Analizza gli URI scaricati con successo
                    for (URI u : queueLoaded) {
                        if (!uriCrawledSet.contains(u)) {
                            LoadResult res = WebFactory.getLoader().load(u.toURL());
                            //Calcola il numero max link in una pagina
                            if (res.parsed.getLinks().size() > maxlink) {
                                maxlink = res.parsed.getLinks().size();
                            }
                            //Calcola il numero URI interni al dominio
                            if (ApplicationSession.isInDomain(domain, u)) {
                                internalUri++;
                                internalLoadedUri++;
                            }
                            //Aggiorna la lista dei link puntanti con i link trovati
                            for(String s:res.parsed.getLinks()){
                                linkItemsList.add(new LinkItem(u, s));
                            }
                            explorationGui.getDbUriCrawledTableView().add(new UriCrawled(String.valueOf(u), String.valueOf(res.exc == null ? "Scaricato" : res.exc.getMessage())));
                            explorationGui.getTableViewHbox().setVisible(true);
                            uriCrawledSet.add(u);
                        }
                    }
                    //Analizza gli URI con errori
                    for (URI u : queueError) {
                        if (!uriCrawledSet.contains(u)) {
                            //Calcola il numero di URI interni al dominio
                            if (ApplicationSession.isInDomain(domain, u)) {
                                internalUri++;
                            }
                            explorationGui.getDbUriCrawledTableView().add(new UriCrawled(String.valueOf(u), "Errore nel crawling"));
                            explorationGui.getTableViewHbox().setVisible(true);
                            uriCrawledSet.add(u);
                        }
                    }
                    //Calcola il numero massimo di link che puntano ad una pagina (maxPointedLink)
                    for(LinkItem lx : linkItemsList) {
                        int pointedLink=0;
                        for(LinkItem ly : linkItemsList){
                            if (lx.assoluto.toString().equals(ly.assoluto.toString())){
                                pointedLink++;
                            }
                            if(pointedLink > maxPointedLink){
                                maxPointedLink = pointedLink;
                            }
                        }
                    }
                } catch (Exception e) {}
            }
        };
        Thread threadUpdate = new Thread(taskUpdate);
        threadUpdate.setDaemon(true);
        threadUpdate.start();
    }

    /**Permette di ottenere l'URI dominio da un archivio wsa.
     * @param p il path dell'archivio.
     * @return l'URI dominio dell'archivio selezionato se il file viene letto correttamente.
     * @return null se il file non viene letto correttemente.*/
    private URI getDomainFromArchive(Path p){
        try {
            BufferedReader lettore = new BufferedReader(new FileReader(p.toAbsolutePath()+"/Archivio.wsa"));
            String lettura = lettore.readLine();
            String[] liste= lettura.split("\\[",-1);
            return URI.create(liste[1].toString());
        } catch (IOException  ioe) {}
        return null;
    }

    /** Ritorna il dominio della sessione.
     * @return il {@code domain}.*/
    public URI getDomain(){return domain;}
    /** Ritorna l'interfaccia grafica della sessione.
     * @return la {@code explorationGui}.*/
    public ExplorationGui getExplorationGui(){return explorationGui;}

    /*Rappresenta un link analizzabile. Contiene informazioni dell'URI contentente il link, del link stesso e del link
    * reso assoluto.*/
    public static class LinkItem {
        public URI sorgente;
        public String link;
        public URL assoluto;
        LinkItem(URI source, String lnk){
            try {
                this.sorgente = source;
                this.link=lnk;
                if(!URI.create(link).isAbsolute()){
                    URL sorgenteUrl = sorgente.toURL();
                    this.assoluto = new URL(sorgenteUrl, link);
                }
                else{assoluto=new URL(link);}
            }catch (Exception e){}
        }
    }

    /**Rappresenta un URI analizzato. Contiene informazioni circa l'uri e il suo stato di scaricamento.*/
    public static class UriCrawled {
        private final SimpleStringProperty uri;
        private final SimpleStringProperty state;

        public UriCrawled(String uri, String state) {
            this.uri = new SimpleStringProperty(uri);
            this.state = new SimpleStringProperty(state);
        }

        /** Ritorna lo Uri analizzato.
         * @return lo {@code uri}.*/
        public String getUri() {return uri.get();}
        /** Imposta {@code uri} ossia lo Uri analizzato.
         * @param uri  il nuovo {@code uri}.*/
        public void setUri(String uri) {this.uri.set(uri);}
        /** Ritorna lo stato di scaricamento dello Uri.
         * @return lo {@code state}.*/
        public String getState() {return state.get();}
        /** Imposta lo {@code state} ossia lo stato di scaricamento dello Uri.
         * @param state il nuovo {@code state}.*/
        public void setState(String state) {this.state.set(state);}
    }
}
