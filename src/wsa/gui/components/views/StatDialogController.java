package wsa.gui.components.views;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

/*Controller dello StatDialog.*/
public class StatDialogController {
    @FXML
    private Text visitedUriText;
    @FXML
    private Text internalUriText;
    @FXML
    private Text errorsUriText;
    @FXML
    private Text maxLinkUriText;
    @FXML
    private Text maxLinkPointingUriText;
    @FXML
    private PieChart pieChart=new PieChart();
    private Stage stage;
    private ObservableList<PieChart.Data> pieData=FXCollections.observableArrayList(new ArrayList<PieChart.Data>());

    /**Inizializza ed azzera il PieChart {@code pieChart}*/
    public void initChart(){
        pieData.addAll(
                new PieChart.Data("Errori",0),
                new PieChart.Data("Scaricati (esterni)",0),
                new PieChart.Data("Scaricati (interni)", 0)
        );
        pieChart.setData(pieData);
    }

    /**Aggiorna il PieChart {@code pieChart}
     * @param visited il numero di uri visitati con successo
     * @param uriloadedint il numero di uri interni al dominio
     * @param error il numero di uri con errori*/
    public void updateChart(int visited, int uriloadedint, int error) {
        for(PieChart.Data pd:pieData){
            if(pd.getName()=="Scaricati (interni)"){pd.setPieValue(uriloadedint);}
            if(pd.getName()=="Scaricati (esterni)"){pd.setPieValue(visited-uriloadedint);}
            if(pd.getName()=="Errori"){pd.setPieValue(error);}
        }
    }

    /** Imposta lo {@code stage}.
     * @param stage  il nuovo {@code stage} */
    public void setStage(Stage stage) {this.stage = stage;}
    /** Imposta la {@code visitedUriText} che visualizza il numero di Uri analizzati con successo.
     * @param vis  la nuova {@code visitedUriText} */
    public void setVisitedUriText(int vis){this.visitedUriText.setText(String.valueOf(vis));}
    /** Imposta la {@code errorsUriText} che visualizza il numero di Uri analizzati con errori.
     * @param err  la nuova {@code errorsUriText} */
    public void setErrorsUriText(int err){this.errorsUriText.setText(String.valueOf(err));}
    /** Imposta la {@code maxLinkUriText} che visualizza il numero massimo di link in una pagina.
     * @param max  la nuova {@code maxLinkUriText} */
    public void setMaxLinkUriText(int max){this.maxLinkUriText.setText(String.valueOf(max));}
    /** Imposta la {@code internalUriText}  che visualizza il numero di Uri interni analizzati.
     * @param internal  la nuova {@code internalUriText} */
    public void setInternalUriText(int internal){this.internalUriText.setText(String.valueOf(internal));}
    /** Imposta la {@code maxLinkPointingUriText} che visualizza il numero massimo di link puntanti ad una pagina.
     * @param pointing  la nuova {@code maxLinkPointingUriText} */
    public void setMaxLinkPointingUriText(int pointing){this.maxLinkPointingUriText.setText(String.valueOf(pointing));}
}
