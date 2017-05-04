package wsa.gui;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wsa.web.LoadResult;
import wsa.web.WebFactory;

import java.net.URI;
import java.util.LinkedList;

/** Mostra un interfaccia grafica per mostrare i dettagli di un URI già analizzato da un {@link wsa.web.SiteCrawler}.*/
public class PageGui {
    private Stage stage = new Stage();
    private Scene scene;
    private VBox vBox;
    private TabPane tabPane = new TabPane();
    private Tab webViewTab = new Tab();
    private Tab infoUriTab =new Tab();
    private Tab linkContainedTab =new Tab();
    private Tab linkPointingTab =new Tab();
    private WebViewGui webViewGui;
    private InfoUriGui infoUriGui;
    private LinkContainedGui linkContainedGui;
    private LinkPointingGui linkPointingGui;

    public PageGui(URI dom, URI uri, LinkedList<SiteSession.LinkItem> linklist){
        webViewGui =new WebViewGui();
        webViewGui.loadURI(String.valueOf(uri));
        webViewTab.setText("Visualizzazione pagina");
        webViewTab.setClosable(false);
        webViewTab.setContent(webViewGui.getvBox());
        infoUriGui = new InfoUriGui(dom);
        infoUriTab.setText("Informazioni aggiuntive");
        infoUriTab.setClosable(false);
        infoUriTab.setContent(infoUriGui.getvBox());
        linkContainedGui =new LinkContainedGui();
        linkContainedTab.setText("Link contenuti");
        linkContainedTab.setClosable(false);
        linkContainedTab.setContent(linkContainedGui.getvBox());
        linkPointingGui =new LinkPointingGui();
        linkPointingTab.setText("Link puntanti");
        linkPointingTab.setClosable(false);
        linkPointingTab.setContent(linkPointingGui.getvbox());
        if(ApplicationSession.isInDomain(dom,uri)) {
            tabPane.getTabs().addAll(webViewTab, infoUriTab, linkContainedTab, linkPointingTab);
        }
        else {
            tabPane.getTabs().addAll(webViewTab, infoUriTab, linkPointingTab);
        }
        vBox = new VBox(tabPane);
        Parent rootSeedScene = new BorderPane(vBox);
        scene = new Scene(rootSeedScene,640,420);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Dettagli di " + String.valueOf(uri));
        stage.setResizable(true);

        Runnable taskUpdate= () -> {
            try{
                LoadResult lr=WebFactory.getLoader().load(uri.toURL());
                if(ApplicationSession.isInDomain(dom,uri)){
                    infoUriGui.update(lr);
                    linkContainedGui.update(lr);
                    linkPointingGui.update(uri,linklist);
                }
                else{
                    infoUriGui.update(lr);
                    linkPointingGui.update(uri,linklist);
                }
            }catch (Exception e){}
        };
        Thread threadUpdate=new Thread(taskUpdate);
        threadUpdate.start();
    }

    /** Ritorna lo {@code stage}.
     * @return lo {@code stage}.*/
    public Stage getStage(){return stage;}
}
