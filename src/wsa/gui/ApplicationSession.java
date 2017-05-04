package wsa.gui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import wsa.gui.components.NewDialog;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Rappresenta la sessione dell'applicazione. Include le sessioni aperte di tutti i domini e gestisce l'interfacciamento
 * di tutti i domini con l'applicazione.*/
public class ApplicationSession {
    private static TabPane siteSessionTabPane= new TabPane();
    private static Tab welcomeTab = new Tab();
    private static List<SiteSession> siteSessionList = FXCollections.observableArrayList();

    /**Visualizza la schermata di benvenuto nel caso nessuna analisi sia aperta. Mantiene inoltre coerente la TabPane
     * e la lista relativa alle {@link wsa.gui.SiteSession} aperte.*/
    public static void checkSiteTab(){
        if(siteSessionList.size()==0){siteSessionTabPane.getTabs().add(welcomeTab);}
        else if(siteSessionTabPane.getTabs().size()!= siteSessionList.size()){
            siteSessionTabPane.getTabs().remove(0);
        }
    }

    /**Instanzia una {@link wsa.gui.components.NewDialog}.*/
    public static void launchNewGUI(){NewDialog newDialog=new NewDialog();}

    /**Permette l'apertura di un archivio verificando prima la correttezza del path.
     * @param window lo owner Window del DirectoryChooser.*/
    public static void openArchive(Window window){
        try {
            Path path;
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleziona la directory dell'archivio");
            File selectedFile = chooser.showDialog(window);
            if (selectedFile != null) {
                path = Paths.get(selectedFile.getAbsolutePath());
                SiteSession siteSession= new SiteSession(null, path);
                ApplicationSession.siteSessionList.add(siteSession);
                Tab openedTab = new Tab();
                openedTab.setText(siteSession.getDomain().toString());
                openedTab.setContent(ApplicationSession.siteSessionList.get(ApplicationSession.siteSessionList.size() - 1).getExplorationGui().getvBox());
                openedTab.setOnClosed(e -> {
                    ApplicationSession.siteSessionList.remove(siteSession);
                    ApplicationSession.checkSiteTab();
                });
                ApplicationSession.siteSessionTabPane.getSelectionModel().select(openedTab);
                ApplicationSession.siteSessionTabPane.getTabs().add(openedTab);
                ApplicationSession.checkSiteTab();
            }
        }catch (Exception exc){
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Errore di caricamento");
            alert.setContentText("Assicurati di aprire una directory contenente un archivio valido.");
            alert.showAndWait();
        }
    }

    /**Controlla se un URI appartiene ad un dominio.
     * @param domain il dominio su cui verificare lo URI.
     * @param u  lo URI da verificare.*/
    public static boolean isInDomain(URI domain,URI u){return ((u==domain)||(domain.relativize(u)!=u)? true: false);}

    /** Ritorna la lista delle {@link wsa.gui.SiteSession} aperte.
     * @return la {@code siteSessionList}.*/
    public static List<SiteSession> getSiteSessionList(){return siteSessionList;}
    /** Imposta la {@code siteSessionList} relativa alle sessioni di {@link wsa.gui.SiteSession} aperte.
     * @param list  la nuova {@code siteSessionList}.*/
    public static void setSiteSessionList(ObservableList<SiteSession> list) {siteSessionList = list;}
    /** Ritorna la {@code TabPane} contenente tutte le tab relative ai siti aperti o di benvenuto.
     * @return la {@code siteSessionTabPane}.*/
    public static TabPane getSiteSessionTabPane(){return siteSessionTabPane;}
    /** Imposta la {@code siteSessionTabPane} contenente tutte le tab relative ai siti aperti o di benvenuto.
     * @param tp  la nuova {@code siteSessionTabPane}.*/
    public static void setSiteSessionTabPane(TabPane tp) {siteSessionTabPane = tp;}
    /** Ritorna la schermata di benvenuto.
     * @return la {@code welcomeTab}.*/
    public static Tab getWelcomeTab(){return welcomeTab;}
}
