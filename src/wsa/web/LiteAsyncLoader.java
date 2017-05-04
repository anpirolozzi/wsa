package wsa.web;

import java.net.URL;
import java.util.concurrent.*;
/** Un loader asincrono per pagine web */
public class LiteAsyncLoader implements AsyncLoader {
    private final BlockingQueue<Loader> coda;
    private final ExecutorService esecutore;
    public LiteAsyncLoader() {
        coda = new LinkedBlockingQueue<>();
        esecutore = Executors.newFixedThreadPool(50);
    }

    /** Sottomette il downloading della pagina dello specificato URL e ritorna
     * un Future per ottenere il risultato in modo asincrono.
     * @param url  un URL di una pagina web
     * @throws IllegalStateException se il loader è chiuso
     * @return Future per ottenere il risultato in modo asincrono */
    @Override
    public Future<LoadResult> submit(URL url) {
        if (!esecutore.isShutdown()){
            return esecutore.submit(() -> {
                try{
                    Loader loader = null;
                    loader = coda.poll(20, TimeUnit.MILLISECONDS);
                    if (loader == null) {
                        loader = WebFactory.getLoader();
                    }
                    LoadResult loadResult = loader.load(url);
                    coda.add(loader);
                    return loadResult;
                }
                catch (InterruptedException e) {return null; }
            });
        }
        else {throw new IllegalStateException("Loader chiuso");}
    }

    /** Chiude il loader e rilascia tutte le risorse. Dopo di ciò non può più
     * essere usato. */
    @Override
    public void shutdown() {
        esecutore.shutdown();
    }

    /** Ritorna true se è chiuso.
     * @return true se è chiuso */
    @Override
    public boolean isShutdown() {
        return esecutore.isShutdown();
    }
}
