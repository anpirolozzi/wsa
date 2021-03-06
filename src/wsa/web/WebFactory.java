package wsa.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/** Una factory per oggetti che servono a scaricare pagine web.
 * L'implementazione del progetto fornisce l'implementazione di default per i
 * Loader. Inoltre l'implementazione di getAsyncLoader usa esclusivamente Loader
 * forniti da getLoader, l'implementazione di getCrawler usa esclusivamente
 * AsyncLoader fornito da getAsyncLoader e l'implementazione di getSiteCrawler
 * usa esclusivamente Crawler fornito da getCrawler. */
public class WebFactory {
    private static LoaderFactory loadFactory=null;
    /** Imposta la factory per creare Loader
     * @param lf  factory per Loader */
    public static void setLoaderFactory(LoaderFactory lf) {loadFactory =lf;}

    /** Ritorna un nuovo Loader. Se non è stata impostata una factory tramite il
     * metodo setLoaderFactory, il Loader è creato tramite l'implementazione di
     * default, altrimenti il Loader è creati tramite la factory impostata
     * con setLoaderFactory.
     * @return un nuovo Loader */
    public static Loader getLoader() {
        return loadFactory==null?new LiteLoader():loadFactory.newInstance();
    }

    /** Ritorna un nuovo loader asincrono che per scaricare le pagine usa
     * esclusivamente Loader forniti da getLoader.
     * @return un nuovo loader asincrono. */
    public static AsyncLoader getAsyncLoader() {
        return new LiteAsyncLoader();
    }

    /** Ritorna un Crawler che inizia con gli specificati insiemi di URI.
     * Per scaricare le pagine usa esclusivamente AsyncLoader fornito da
     * getAsyncLoader.
     * @param loaded  insieme URI scaricati
     * @param toLoad  insieme URI da scaricare
     * @param errs  insieme URI con errori
     * @param pageLink  determina gli URI per i quali i link contenuti nelle
     *                  relative pagine sono usati per continuare il crawling
     * @return un Crawler con le proprietà specificate */
    public static Crawler getCrawler(Collection<URI> loaded,
                                     Collection<URI> toLoad,
                                     Collection<URI> errs,
                                     Predicate<URI> pageLink) {
        return new LiteCrawler(loaded,toLoad,errs,pageLink);
    }


    /** Ritorna un SiteCrawler. Se dom e dir sono entrambi non null, assume che
     * sia un nuovo web site con dominio dom da archiviare nella directory dir.
     * Se dom non è null e dir è null, l'esplorazione del web site con dominio
     * dom sarà eseguita senza archiviazione. Se dom è null e dir non è null,
     * assume che l'esplorazione del web site sia già archiviata nella
     * directory dir e la apre. Per scaricare le pagine usa esclusivamente un
     * Crawler fornito da getCrawler.
     * @param dom  un dominio o null
     * @param dir  un percorso di una directory o null
     * @throws IllegalArgumentException se dom e dir sono entrambi null o dom è
     * diverso da null e non è un dominio o dir è diverso da null non è una
     * directory o dom è null e dir non contiene l'archivio di un SiteCrawler.
     * @throws IOException se accade un errore durante l'accesso all'archivio
     * del SiteCrawler
     * @return un SiteCrawler */
    public static SiteCrawler getSiteCrawler(URI dom, Path dir) throws IOException {
        AtomicReference<File> archivio = new AtomicReference<>(null);
        if ((dom==null && dir==null)){throw new IllegalArgumentException("Parametri non specificati");}
        if ((dom!=null && !(SiteCrawler.checkDomain(dom)) )) {throw new IllegalArgumentException("Dominio invalido");}
        if (dir != null) {
            archivio.set(new File(dir.toAbsolutePath() + "/Archivio.wsa"));
            if (Files.exists(dir.toAbsolutePath()) && !(Files.isDirectory(dir.toAbsolutePath()))){throw new IllegalArgumentException("Directory invalida");}
            if ((dom== null && !(Files.exists(archivio.get().toPath()))) ){throw new IllegalArgumentException("La directory non contiene un archivio wsa");}
        }
        return new LiteSiteCrawler(dom,dir);
    }
}
