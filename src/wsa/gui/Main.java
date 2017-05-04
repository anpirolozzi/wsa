package wsa.gui;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;
import wsa.gui.components.Wellcome;

/*Imposta l'interfaccia generale dell'applicazione e ne permette l'avviamento.*/
public class Main extends Application{
    private BorderPane borderPane=new BorderPane();
    private Scene scene;
    private MenuBar menuBar = new MenuBar();
    private Menu fileMenu = new Menu("File");
    private Menu aboutMenu = new Menu("?");
    private MenuItem newMenuItem = new MenuItem("Nuova analisi...",new ImageView(new Image(getClass().getResource("/icons/new.png").toString())));
    private MenuItem openMenuItem = new MenuItem("Apri analisi...",new ImageView(new Image(getClass().getResource("/icons/open.png").toString())));
    private MenuItem exitMenuItem = new MenuItem("Esci",new ImageView(new Image(getClass().getResource("/icons/close.png").toString())));
    private MenuItem helpMenuItem = new MenuItem("Guida rapida",new ImageView(new Image(getClass().getResource("/icons/help.png").toString())));
    private Button newButton = new Button("Nuova analisi",new ImageView(new Image(getClass().getResource("/icons/new.png").toString())));
    private Button openButton = new Button("Apri analisi",new ImageView(new Image(getClass().getResource("/icons/open.png").toString())));
    private ToolBar toolBar = new ToolBar(newButton, openButton);
    private VBox vBox = new VBox(toolBar, ApplicationSession.getSiteSessionTabPane());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) {
            initGui(primaryStage);
            initAction(primaryStage);
            primaryStage.show();
    }

    /** Imposta l'interfaccia grafica (senza azioni).
     * @param primaryStage lo stage principale dell'applicazione.*/
    private void initGui(Stage primaryStage){
        fileMenu.getItems().addAll(newMenuItem, openMenuItem, exitMenuItem);
        aboutMenu.getItems().addAll(helpMenuItem);
        menuBar.getMenus().addAll(fileMenu, aboutMenu);
        if (System.getProperty ("os.name") != null && System.getProperty ("os.name").startsWith ("Mac"))
            menuBar.useSystemMenuBarProperty ().set (true);
        Wellcome wellcome = new Wellcome();
        ApplicationSession.getWelcomeTab().setClosable(false);
        ApplicationSession.getWelcomeTab().setText("Benvenuto");
        ApplicationSession.getWelcomeTab().setContent(wellcome.getWellcomePane());
        ApplicationSession.checkSiteTab();
        vBox.setPrefWidth(800);
        borderPane.setTop(menuBar);
        borderPane.setCenter(vBox);
        scene = new Scene(borderPane, 854, 480);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setTitle("Web Site Analyser");
        primaryStage.getIcons().add(new Image(getClass().getResource("/icons/internet.png").toString()));
    }

    /** Imposta le azioni dell'interfaccia grafica.
     * @param primaryStage lo stage principale dell'applicazione.*/
    private void initAction(Stage primaryStage){
        primaryStage.setOnCloseRequest(e->{System.exit(0);});
        newButton.setOnAction(e -> {
            ApplicationSession.launchNewGUI();
        });
        newMenuItem.setOnAction(e -> {
            ApplicationSession.launchNewGUI();
        });
        openButton.setOnAction(e -> {
            ApplicationSession.openArchive(primaryStage);
        });
        openMenuItem.setOnAction(e -> {
            ApplicationSession.openArchive(primaryStage);
        });
        exitMenuItem.setOnAction(t -> {
            System.exit(0);
        });
        helpMenuItem.setOnAction(t -> {
            WebViewGui webViewGui = new WebViewGui();
            webViewGui.loadURI(getClass().getResource("/help.html").toString());
            webViewGui.setTitle("Guida rapida");
            webViewGui.getStage().getIcons().add(new Image(getClass().getResource("/icons/help.png").toString()));
            webViewGui.getStage().initModality(Modality.APPLICATION_MODAL);
            webViewGui.getStage().show();
        });
    }
}