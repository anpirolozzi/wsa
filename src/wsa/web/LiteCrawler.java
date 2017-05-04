package wsa.web;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Un web crawler che partendo da uno o più URI scarica le relative pagine e
 * poi fa lo stesso per i link contenuti nelle pagine scaricate. Però il crawler
 * può essere creato in modo tale che solamente le pagine di URI selezionati
 * sono usate per continuare il crawling. */
public class LiteCrawler implements Crawler {
    private AtomicReference<AsyncLoader> asyncLoader;
    private BlockingQueue<Future<LoadResult>> risultatiAsync;
    private Predicate<URI> predicatoLink = null;
    private URI uri;
    private URI link;
    private LinkedBlockingQueue<URI> codaLoaded;
    private LinkedBlockingQueue<URI> codaToLoad;
    private LinkedBlockingQueue<URI> codaError;
    private LinkedBlockingQueue<Optional<CrawlerResult>> codaCrawlerResult;
    private AtomicBoolean esecuzione;
    private AtomicBoolean cancellato;

    public LiteCrawler(Collection<URI> loaded, Collection<URI> toLoad, Collection<URI> error, Predicate<URI> pageLink){
        this.risultatiAsync = new LinkedBlockingQueue<Future<LoadResult>>();
        this.codaLoaded = new LinkedBlockingQueue<URI>();
        this.codaToLoad = new LinkedBlockingQueue<URI>();
        this.codaError = new LinkedBlockingQueue<URI>();
        this.codaCrawlerResult = new LinkedBlockingQueue<Optional<CrawlerResult>>();
        esecuzione = new AtomicBoolean(false);
        cancellato = new AtomicBoolean(false);

        if (loaded != null) codaLoaded.addAll(loaded);
        if (toLoad != null) codaToLoad.addAll(toLoad);
        if (error != null) codaError.addAll(error);
        if(pageLink==null){
            this.predicatoLink =  (uri) -> {return true;};
        }
        else{
            this.predicatoLink = pageLink;
        }
    }

    /** Aggiunge un URI all'insieme degli URI da scaricare. Se però è presente
     * tra quelli già scaricati, quelli ancora da scaricare o quelli che sono
     * andati in errore, l'aggiunta non ha nessun effetto. Se invece è un nuovo
     * URI, è aggiunto all'insieme di quelli da scaricare.
     * @throws IllegalStateException se il Crawler è cancellato
     * @param uri  un URI che si vuole scaricare */
    @Override
    public void add(URI uri) {
        if(!isCancelled()){
            if((!codaLoaded.contains(uri))&&(!codaToLoad.contains(uri))&&(!codaError.contains(uri))){
                codaToLoad.add(uri);
            }
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Inizia l'esecuzione del Crawler se non è già in esecuzione e ci sono URI
     * da scaricare, altrimenti l'invocazione è ignorata. Quando è in esecuzione
     * il metodo isRunning ritorna true.
     * @throws IllegalStateException se il Crawler è cancellato */
    @Override
    public void start() {
        if(!isCancelled()){
            if (!isRunning() &&(!codaToLoad.isEmpty())) {
                esecuzione.set(true);
                asyncLoader = new AtomicReference<AsyncLoader>(WebFactory.getAsyncLoader());
                Thread threadCrawling = new Thread(()-> {
                    while (esecuzione.get()&&!cancellato.get()){
                        try {
                            for (URI uri : codaToLoad) {
                                try {
                                    risultatiAsync.add(asyncLoader.get().submit(uri.toURL()));
                                } catch (MalformedURLException e) {
                                    codaError.add(uri);
                                }
                            }
                            codaToLoad.removeAll(codaError);
                            while (codaToLoad != null && codaToLoad.isEmpty()) {
                                try {
                                    Thread.currentThread().sleep(50);
                                } catch (InterruptedException intexc) {
                                    System.out.println("InterruptedException in LiteCrawler.start()");
                                }
                            }
                            LoadResult loadResult = null;
                            Future<LoadResult> loadResultFuture= null;
                            while ((!risultatiAsync.isEmpty()) && (loadResultFuture = risultatiAsync.take()) != null) {
                                while (!(loadResultFuture.isDone()));
                                try {
                                    loadResult = loadResultFuture.get();
                                    URL url = loadResult.url;
                                    uri = url.toURI();
                                    if(loadResult.exc == null) {
                                        List<URI> listUri = new ArrayList<URI>();
                                        List<String> listError = new ArrayList<String>();
                                        codaToLoad.remove(uri);
                                        codaLoaded.add(uri);
                                        for (String str : loadResult.parsed.getLinks()) {
                                            link = new URI(str);
                                            if (!link.isAbsolute()) {
                                                url = loadResult.url.toURI().resolve(str).toURL();
                                            } else {
                                                url = link.toURL();
                                            }
                                            Exception excDownload = check(url);
                                            link = url.toURI();
                                            if (predicatoLink.test(link)) {
                                                if (excDownload == null) {
                                                    if ((!codaLoaded.contains(link)) && (!codaToLoad.contains(link)) && (!codaError.contains(link))) {
                                                        risultatiAsync.add(asyncLoader.get().submit(link.toURL()));
                                                        add(link);
                                                        listUri.add(link);
                                                    }
                                                } else {
                                                    listError.add(str);
                                                }
                                                codaCrawlerResult.put(Optional.of(new CrawlerResult(link, true, listUri, listError, excDownload)));
                                            } else {
                                                if (excDownload == null) {
                                                    codaLoaded.add(link);
                                                    codaCrawlerResult.put(Optional.of(new CrawlerResult(link, false, null, null, excDownload)));
                                                } else {
                                                    codaError.add(link);
                                                    codaCrawlerResult.put(Optional.of(new CrawlerResult(link, false, null, null, excDownload)));
                                                }
                                            }
                                        }
                                    } else {
                                        codaError.add(uri);
                                        codaToLoad.remove(uri);
                                        codaCrawlerResult.put(Optional.of(new CrawlerResult(uri, false, null, null, loadResult.exc)));
                                    }
                                } catch (Exception e) {}
                                risultatiAsync.remove(loadResultFuture);
                            }
                        } catch (InterruptedException exc) {}
                    }
                });
                threadCrawling.start();
            }
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Sospende l'esecuzione del Crawler. Se non è in esecuzione, ignora
     * l'invocazione. L'esecuzione può essere ripresa invocando start. Durante
     * la sospensione l'attività del Crawler dovrebbe essere ridotta al minimo
     * possibile (eventuali thread dovrebbero essere terminati).
     * @throws IllegalStateException se il Crawler è cancellato */
    @Override
    public void suspend() {
        if(!isCancelled()){
            esecuzione.set(false);}
        else {
            throw new IllegalStateException("Crawler cancellato");
        }
    }

    /** Cancella il Crawler per sempre. Dopo questa invocazione il Crawler non
     * può più essere usato. Tutte le risorse devono essere rilasciate. */
    @Override
    public void cancel() {
        esecuzione.set(false);
        asyncLoader.get().shutdown();
        cancellato.set(true);
        codaLoaded =null;
        codaToLoad =null;
        codaError =null;
        codaCrawlerResult =null;
        risultatiAsync.clear();
    }

    /** Ritorna il risultato relativo al prossimo URI. Se il Crawler non è in
     * esecuzione, ritorna un Optional vuoto. Non è bloccante, ritorna
     * immediatamente anche se il prossimo risultato non è ancora pronto.
     * @throws IllegalStateException se il Crawler è cancellato
     * @return  il risultato relativo al prossimo URI scaricato */
    @Override
    public Optional<CrawlerResult> get() {
        if(!isCancelled()){
            if((!isRunning()) || (codaCrawlerResult.isEmpty())){
                return Optional.of(new CrawlerResult(null, false, null , null, null));
            }
            else{return codaCrawlerResult.poll();}
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     * @throws IllegalStateException se il Crawler è cancellato
     * @return l'insieme di tutti gli URI scaricati (mai null) */
    @Override
    public Set<URI> getLoaded() {
        if(!isCancelled()){
            if(isRunning()&& codaLoaded!=null){return codaLoaded.stream().collect(Collectors.toSet());}
            else {return new HashSet<URI>();}
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che devono essere
     * ancora scaricati. Quando l'esecuzione del crawler termina normalmente
     * l'insieme è vuoto.
     * @throws IllegalStateException se il Crawler è cancellato
     * @return l'insieme degli URI ancora da scaricare (mai null) */
    @Override
    public Set<URI> getToLoad() {
        if(!isCancelled()){
            if(isRunning()&& codaToLoad!=null){return codaToLoad.stream().collect(Collectors.toSet());}
            else {return new HashSet<URI>();}
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che non è stato
     * possibile scaricare a causa di errori.
     * @throws IllegalStateException se il crawler è cancellato
     * @return l'insieme degli URI che hanno prodotto errori (mai null) */
    @Override
    public Set<URI> getErrors() {
        if(!isCancelled()){
            if(isRunning()&& codaError!=null){return codaError.stream().collect(Collectors.toSet());}
            else {return new HashSet<URI>();}
        }
        else{throw new IllegalStateException("Crawler cancellato");}
    }

    /** Ritorna true se il Crawler è in esecuzione.
     * @return true se il Crawler è in esecuzione */
    @Override
    public boolean isRunning() {
        return esecuzione.get();
    }

    /** Ritorna true se il Crawler è stato cancellato. In tal caso non può più
     * essere usato.
     * @return true se il Crawler è stato cancellato */
    @Override
    public boolean isCancelled() {return cancellato.get();}

    /** Controlla che l'URL sia scaricato con successo.
     * @param url l'URL da controllare.
     * @return l'eccezione relativa al controllo dell'URL.*/
    private Exception check(URL url) {
        return(WebFactory.getLoader().check(url));
    }
}

