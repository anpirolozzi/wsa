package wsa.web;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import org.w3c.dom.Document;
import wsa.web.html.LiteParsed;
import wsa.web.html.Parsed;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicReference;

/** Un Loader permette di scaricare una pagina alla volta */
public class LiteLoader implements Loader {
    private final AtomicReference<WebEngine> atomicWebEngine;
    private final AtomicReference<WebResult> atomicWebResult;

    public LiteLoader(){
        atomicWebEngine = new AtomicReference<>();
        atomicWebResult = new AtomicReference<>();
        Platform.runLater(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.setJavaScriptEnabled(false);
            atomicWebEngine.set(webEngine);
            webEngine.getLoadWorker().stateProperty().addListener((o, ov, nv) -> {
                if (nv == Worker.State.SUCCEEDED || nv == Worker.State.FAILED || nv == Worker.State.CANCELLED) {
                    Document document = webEngine.getDocument();
                    atomicWebResult.set(new WebResult(nv == Worker.State.SUCCEEDED && document != null ? new LiteParsed(document) : null, nv));
                }
            });
        });
    }

    /** Ritorna il risultato del tentativo di scaricare la pagina specificata. È
     * bloccante, finchè l'operazione non è conclusa non ritorna.
     * @param url  l'URL di una pagina web
     * @return il risultato del tentativo di scaricare la pagina */
    @Override
    public LoadResult load(URL url) {
        while (atomicWebEngine.get()==null){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        };
        WebEngine webEngine = atomicWebEngine.get();
        atomicWebResult.set(null);
        Platform.runLater(() -> {
            webEngine.load("");
        });
        while (atomicWebResult.get()== null) {
            try {
                Thread.sleep(20);
            }catch (InterruptedException e){break;}
        }
        atomicWebResult.set(null);
        Platform.runLater(() -> {
            webEngine.load(url.toString());
        });
            while (atomicWebResult.get() == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            }
        WebResult webResult = atomicWebResult.get();
        return new LoadResult(url, webResult.albero, ((webResult.albero != null) ? null:new Exception("Errore di download: " + webResult.stato)));
    }

    /** Ritorna null se l'URL è scaricabile senza errori, altrimenti ritorna
     * un'eccezione che riporta l'errore.
     * @param url  un URL
     * @return null se l'URL è scaricabile senza errori, altrimenti
     * l'eccezione */
    @Override
    public Exception check(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.setRequestProperty("Accept", "text/html;q=1.0,*;q=0");
            urlConnection.setRequestProperty("Accept-Encoding", "identity;q=1.0,*;q=0");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(7000);
            urlConnection.connect();
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    /**Contiene l'albero di parsing e lo stato di download di un risultato.*/
    static class WebResult {
        final Parsed albero;
        final Worker.State stato;
        WebResult(Parsed alb, Worker.State s){
            albero= alb;
            stato=s;
        }
    }
}