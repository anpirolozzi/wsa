package wsa.web;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/** Un crawler specializzato per siti web */
public class LiteSiteCrawler implements SiteCrawler {
    private URI dominio;
    private Path directory;
    private AtomicReference<Crawler> crawler = new AtomicReference<Crawler>();
    private Predicate<URI> predicatoLink;
    private Map<URI,CrawlerResult> crawlResultMap;
    private HashSet<URI> codaLoaded = new HashSet<>();
    private HashSet<URI> codaToLoad = new HashSet<>();
    private HashSet<URI> codaError = new HashSet<>();
    private AtomicBoolean esecuzione;
    private AtomicBoolean cancellato;

    LiteSiteCrawler(URI dom, Path dir) {
        this.dominio = dom;
        this.directory = dir;
        crawlResultMap = new HashMap<>();
        cancellato = new AtomicBoolean(false);
        esecuzione = new AtomicBoolean(false);
        if(dom==null && dir!=null){apriArchivio();}
        predicatoLink = (s) -> (s.equals(dominio) || !(dominio.relativize(s).equals(s)));
    }

    /** Aggiunge un seed URI. Se però è presente tra quelli già scaricati,
     * quelli ancora da scaricare o quelli che sono andati in errore,
     * l'aggiunta non ha nessun effetto. Se invece è un nuovo URI, è aggiunto
     * all'insieme di quelli da scaricare.
     * @throws IllegalArgumentException se uri non appartiene al dominio di
     * questo SuteCrawlerrawler
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @param uri  un URI */
    @Override
    public void addSeed(URI uri) {
        if(!isCancelled()){
            if (SiteCrawler.checkSeed(dominio,uri)){
                if (isRunning()&&(crawler.get() != null) && ((!getLoaded().contains(uri)) && (!getToLoad().contains(uri)) && (!getErrors().contains(uri)))) {
                    crawler.get().add(uri);
                } else if ((!codaLoaded.contains(uri)) && (!codaToLoad.contains(uri)) && (!codaError.contains(uri))) {
                    codaToLoad.add(uri);
                }
            }
            else throw new IllegalArgumentException("Uri non appartenente al dominio del SiteCrawler");
        }
        else {throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Inizia l'esecuzione del SiteCrawler se non è già in esecuzione e ci sono
     * URI da scaricare, altrimenti l'invocazione è ignorata. Quando è in
     * esecuzione il metodo isRunning ritorna true.
     * @throws IllegalStateException se il SiteCrawler è cancellato */
    @Override
    public void start() {
        if(!isCancelled()){
            if ((!isRunning()) && (!codaToLoad.isEmpty())) {
                esecuzione.set(true);
                crawler = new AtomicReference<Crawler>(WebFactory.getCrawler(codaLoaded, codaToLoad, codaError, predicatoLink));
                Thread threadSiteCrawling = new Thread(() -> {
                    crawler.get().start();
                    while(!isCancelled()&& directory!=null){
                        try {
                            Runnable taskSalva = () -> {archivia();};
                            Thread threadSalva = new Thread(taskSalva);
                            threadSalva.start();
                            threadSalva.sleep(50000);
                        }catch (Exception e){}
                    }
                });
                threadSiteCrawling.start();
            }
        }
        else {throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Sospende l'esecuzione del SiteCrawler. Se non è in esecuzione, ignora
     * l'invocazione. L'esecuzione può essere ripresa invocando start. Durante
     * la sospensione l'attività dovrebbe essere ridotta al minimo possibile
     * (eventuali thread dovrebbero essere terminati). Se è stata specificata
     * una directory per l'archiviazione, lo stato del crawling è archiviato.
     * @throws IllegalStateException se il SiteCrawler è cancellato */
    @Override
    public void suspend() {
        if(!isCancelled()){
            if (isRunning()) {
                if(directory!=null){archivia();}
                esecuzione.set(false);
                crawler.get().suspend();
            }
        }
        else{throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Cancella il SiteCrawler per sempre. Dopo questa invocazione il
     * SiteCrawler non può più essere usato. Tutte le risorse sono
     * rilasciate. */
    @Override
    public void cancel() {
        suspend();
        cancellato.set(true);
        codaLoaded =null;
        codaToLoad =null;
        codaError =null;
        crawler.get().cancel();
    }

    /** Ritorna il risultato relativo al prossimo URI. Se il SiteCrawler non è
     * in esecuzione, ritorna un Optional vuoto. Non è bloccante, ritorna
     * immediatamente anche se il prossimo risultato non è ancora pronto.
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @return  il risultato relativo al prossimo URI scaricato */
    @Override
    public Optional<CrawlerResult> get() {
        if(!isCancelled()){
            if(isRunning()){
                Optional<CrawlerResult> crawlerResult = crawler.get().get();
                if (crawlerResult.isPresent()) {
                    crawlResultMap.put(crawlerResult.get().uri,crawlerResult.get());
                }
                return crawlerResult;
            }
            else{return Optional.of(new CrawlerResult(null, false, null , null, null));}
        }
        else{throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Ritorna il risultato del tentativo di scaricare la pagina che
     * corrisponde all'URI dato.
     * @param uri  un URI
     * @throws IllegalArgumentException se uri non è nell'insieme degli URI
     * scaricati né nell'insieme degli URI che hanno prodotto errori.
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @return il risultato del tentativo di scaricare la pagina */
    @Override
    public CrawlerResult get(URI uri) {
        if(!isCancelled()){
            if(isRunning()){
                Optional<CrawlerResult> crawlerResultOptional;
                CrawlerResult crawlerResult = crawlResultMap.get(uri);
                if (crawlerResult != null) {return crawlerResult;}
                do {
                    crawlerResultOptional=get();
                }
                while ((crawlerResultOptional.isPresent()) && ((crawlerResultOptional.get().uri==null) || !(crawlerResultOptional.get().uri.equals(uri))));
                if (crawlerResultOptional.isPresent() && (crawlerResultOptional.get().uri.equals(uri))) return crawlerResultOptional.get();
                else  throw new IllegalArgumentException("URI non presente negli insiemi scaricati o con errori");
            }
            else{return null;}
        }
        else{throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @return l'insieme di tutti gli URI scaricati (mai null) */
    @Override
    public Set<URI> getLoaded() {
        if(!isCancelled()){
            if (isRunning()){return (crawler.get()!=null ? crawler.get().getLoaded():new HashSet<URI>());}
            else{return codaLoaded;}
        }
        else {throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che devono essere
     * ancora scaricati. Quando l'esecuzione del crawler termina normalmente
     * l'insieme è vuoto.
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @return l'insieme degli URI ancora da scaricare (mai null) */
    @Override
    public Set<URI> getToLoad() {
        if(!isCancelled()){
            if (isRunning()){return (crawler.get()!=null ? crawler.get().getToLoad():new HashSet<URI>());}
            else{return codaToLoad;}
        }
        else {throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che non è stato
     * possibile scaricare a causa di errori.
     * @throws IllegalStateException se il SiteCrawler è cancellato
     * @return l'insieme degli URI che hanno prodotto errori (mai null) */
    @Override
    public Set<URI> getErrors() {
        if(!isCancelled()){
            if (isRunning())
            {return (crawler.get()!=null ? crawler.get().getErrors():new HashSet<URI>());}
            else{return codaError;}
        }
        else {throw new IllegalStateException("SiteCrawler cancellato");}
    }

    /** Ritorna true se il SiteCrawler è in esecuzione.
     * @return true se il SiteCrawler è in esecuzione */
    @Override
    public boolean isRunning() {
        return esecuzione.get();
    }

    /** Ritorna true se il SiteCrawler è stato cancellato. In tal caso non può
     * più essere usato.
     * @return true se il SiteCrawler è stato cancellato */
    @Override
    public boolean isCancelled() {
        return cancellato.get();
    }

    /**Permette l'apertura di un archivio .wsa precedentemente salvato.*/
    private void apriArchivio(){
        try {
            BufferedReader lettore = new BufferedReader(new FileReader(directory.toAbsolutePath()+"/Archivio.wsa"));
            String lettura = lettore.readLine();
            String[] liste= lettura.split("\\[",-1);
            dominio=URI.create(liste[1].toString());
            String rawtoload=liste[2].toString();
            String[]toload=rawtoload.split(", ");
            String rawloaded=liste[3].toString();
            String[]loaded=rawloaded.split(", ");
            String rawerr=liste[4].toString();
            String[]error=rawerr.split(", ");
            for(String struri :toload){
                if(struri!=null && struri!=""&&(!struri.isEmpty())){
                    URI newuri= URI.create(struri);
                    codaToLoad.add(newuri);
                }
            }
            for(String struri :loaded){
                if(struri!=null && struri!=""&&(!struri.isEmpty())) {
                    URI newuri = URI.create(struri);
                    codaLoaded.add(newuri);
                }
            }
            for(String struri :error){
                if(struri!=null && struri!=""&&(!struri.isEmpty())) {
                    URI newuri = URI.create(struri);
                    codaError.add(newuri);
                }
            }
        } catch (IOException e) {}
    }

    /**Permette l'archiviazione su file .wsa delle informazioni per riaprirle in un secondo momento.*/
    private void archivia() {
        FileWriter fileWriter;
            try {
                if (!(Files.exists(directory))) {
                    Files.createDirectory(directory.toAbsolutePath());
                }
                fileWriter = new FileWriter(directory.toAbsolutePath() + "/Archivio.wsa");
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                AtomicReference<String> s=new AtomicReference<>();
                s.set("["+dominio.toString()+"]");
                s.set(s+getToLoad().toString());
                s.set(s+getLoaded().toString());
                s.set(s+getErrors().toString());
                s.set(s.get().replaceAll("]",""));
                bufferedWriter.write(s.get());
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception e) {}
    }
}
