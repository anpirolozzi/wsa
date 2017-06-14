Il progetto consiste nella realizzazione di un'applicazione chiamata Web Site Analyser, in breve WSA, che permette di analizzare siti web e può essere d'aiuto per la loro manutenzione. La WSA permette di eseguire esplorazioni di siti web e di analizzare i dati raccolti. Un sito web è caratterizzato da un dominio che è specificato da un URI assoluto e gerarchico (hierarchical URI) con la seguente sintassi

scheme://authority[path]

La parte path è opzionale e la parte authority consiste solamente nell'host.
Si noti che non è detto che l'URI che specifica un dominio sia l'indirizzo di una pagina scaricabile (ad es. http://twiki.di.uniroma1.it/pub/Metod_prog non corrisponde ad una pagina) e gli schemi possono essere qualsiasi (http,https, ftp, file, ecc.). Oltre a un dominio per poter effettuare un'esplorazione del sito web è necessario specificare uno o più indirizzi URI di pagine web che appartengono al dominio, questi sono detti seed. 
Chiaramente i seed devono corrispondere a pagine web e devono appartenere al dominio del sito web. Se dom è l'URI di un dominio e u è un URI, allora si può determinare se u appartiene al dominio dom controllando che valga o meno la seguente condizione: u è uguale a dom o dom.relativize(u) ritorna un URI diverso da u.

L'applicazione ha un'interfaccia utente grafica che permette di effettuare le seguenti operazioni.

1) L'operazione fondamentale è l'esplorazione di un sito web e la raccolta di informazioni su di esso. L'utente specifica un dominio e uno o più URI seed e può far partire l'eplorazione del sito web. Ovviamente la WSA farà i controlli del caso. L'esplorazione cerca di scaricare le pagine a partire dai seed e per ognuna di quelle appartenenti al dominio estrae i link e scarica le pagine relative e continua l'esplorazione fino a che non ci sono più link da scaricare. Per gli URI non appartenenti al dominio prova solamente se sono scaricabili ma non ne considera i link.

2) Prima d'iniziare l'esplorazione di un sito l'utente può scegliere una directory del file system in cui archiviare i risultati, anche parziali, dell'esplorazione.

3) Durante l'esplorazione di un sito la WSA mostra gli URL delle pagine scaricate e gli eventuali errori incontrati. Inoltre per ogni pagina appartenente al dominio, a richiesta, mostra i link estratti e per ognuno se è già stato seguito o se ha incontrato un errore. L'utente può navigare facilmente questi dati che sono continuamente aggiornati. Per ogni pagina del dominio scaricata mostra il numero di link in essa contenuti e per ogni pagina, anche esterna al dominio, il numero di link finora trovati che puntano ad essa.

4) In ogni momento l'utente può cliccare su uno dei link delle pagine è ottenere la visualizzazione della pagina. Inoltre, può aggiungere nuovi seed URI (appartenenti al dominio) per l'esplorazione anche mentre l'esplorazione è in esecuzione e può sospendere l'esplorazione e può riprenderla a piacere oppure può annullarla in modo definitivo.

5) Se per l'esplorazione è stata decisa l'archiviazione, allora periodicamente (almeno ogni minuto) durante l'esplorazione l'archivio deve essere aggiornato in modo tale che se per qualsiasi motivo l'applicazione si interrompe, l'esplorazione può essere ripresa senza perdere troppo lavoro.

6) L'utente può aprire in ogni momento l'archivio di un sito web ed eventualmente riprenderne l'esplorazione se non era terminata. Ma anche se era terminata l'utente può aggiungere qualche altro seed URI e riprendere l'esplorazione se questi sono diversi da quelli già visitati. Quando un archivio è aperto la WSA permette di navigare facilmente tutti i suoi dati: gli URI delle pagine e dei link classificandoli come interni al dominio, esterni al dominio, scaricati con successo o con errori. La WSA mostra la causa di ogni errore richiesto dall'utente. Per ogni URI dà la possibilità di visualizzare la pagina e mostra la lista dei link che puntano alla pagina (tra quelli esplorati) e per quelli appartenenti al dominio mostra anche la lista dei link contenuti nella pagina. Inoltre per ogni URI scaricato appartenente al dominio mostra anche il numero di immagini, il numero di nodi e l'altezza dell'albero di parsing.

7) Per un sito web aperto la WSA permette di mostrare dati statistici generali come: numero totale URI visitati, numero URI interni al dominio, numero URI che hanno prodotto errori, massimo numero di link in una pagina, numero massimo di link che puntano ad una pagina. Inoltre, la WSA dovrebbe mostrare anche dati di un sito sotto forma di grafici. Ad esempio istogrammi relativi ai link uscenti e a quelli entranti.

8) Per un sito aperto la cui esplorazione sia terminata l'utente può chiedere di calcolare la distanza tra due URI interni al dominio e anche la massima distanza tra tutte le coppie di URI appartenenti al dominio. Durante operazioni come queste che possono prendere tempi lunghi, la WSA mantiene sempre l'interfaccia utente reattiva e permette di cancellarne l'esecuzione.

9) Relativamente a due o più siti aperti la WSA permette di calcolare il numero di link da uno dei siti verso un altro.

10) Per l'implementazione usa solamente la libreria standard di Java. La GUI è implementata tramite JavaFX ed usando appositi file FXML.
