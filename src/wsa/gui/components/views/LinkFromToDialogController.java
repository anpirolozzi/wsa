package wsa.gui.components.views;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import wsa.gui.ApplicationSession;
import wsa.gui.SiteSession;

import java.util.LinkedList;

/*Controller del LinkFromToDialog.*/
public class LinkFromToDialogController {
    private LinkedList<SiteSession.LinkItem>linkList;
    private Stage stage;
    @FXML
    private ComboBox<String> comboDomain;
    @FXML
    private Text linknumberText;
    @FXML
    private Button closeButton;

    /** Inizializza l'interfaccia e aggiorna {@code comboDomain} con tutti i {@link wsa.gui.SiteSession} aperti.
     * @param list  lista di tutti i {@link wsa.gui.SiteSession.LinkItem} presenti in una {@link wsa.gui.SiteSession}.*/
    public void update(LinkedList<SiteSession.LinkItem> list){
        this.linkList=list;
        linknumberText.setText("");
        comboDomain.getItems().clear();
        Runnable taskUpdate = () -> {
            comboDomain.getItems().clear();
            while(comboDomain.getItems().size()!=ApplicationSession.getSiteSessionList().size()){
                comboDomain.getItems().clear();
                for(SiteSession ss:ApplicationSession.getSiteSessionList()){
                    comboDomain.getItems().add(ss.getDomain().toString());
                }
            }
        };
        Thread threadUpdate = new Thread(taskUpdate);
        threadUpdate.start();
    }

    /** Permette la chiusura dello {@code stage}.*/
    @FXML
    private void closeStage() {stage.close();}

    /** Permette il calcolo di link da un sito aperto verso un altro.*/
    @FXML
    private void compute() {
        try {
            if(!comboDomain.getValue().isEmpty()) {
                int counter = 0;
                if(!linkList.isEmpty()){
                    for (SiteSession.LinkItem li : linkList) {
                        if (String.valueOf(li.assoluto).contains(comboDomain.getValue())) {counter++;}
                        if (String.valueOf(li.assoluto).contains("file:/")){
                            if (String.valueOf(li.assoluto).replace("file:/","file:///").contains(comboDomain.getValue())) {counter++;}
                        }
                    }
                    linknumberText.setText(String.valueOf(counter));
                }
            }
        }catch (Exception e){}
    }

    /** Imposta lo {@code stage}.
     * @param stage  il nuovo {@code stage} */
    public void setStage(Stage stage) {this.stage = stage;}
    /** Imposta la {@code linkList} ossia la lista dei link scaricati.
     * @param linkList  la nuova {@code linkList} */
    public void setLinkList(LinkedList<SiteSession.LinkItem>linkList){this.linkList=linkList;}
}