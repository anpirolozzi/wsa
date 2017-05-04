package wsa.web.html;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/** Rappresenta l'albero di analisi sintattica (parsing) di una pagina web.*/
public  class LiteParsed implements Parsed{
    private final NodoFiglio albero;
    public LiteParsed(Document doc){
        albero = new NodoFiglio(doc); //crea un albero che parte dalla radice
    }

    /**Verifica che il nodo sia un elemento.
     * @param nodo il nodo da verificare.
     * @return se il nodo è un elemento.*/
    private static boolean isElement(org.w3c.dom.Node nodo){
        return nodo.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE;
    }

    /**Ritorna il tag di un nodo.
     * @param nodo il nodo interessato.
     * @return il tag del nodo.*/
    private static String tag(org.w3c.dom.Node nodo) {
        return isElement(nodo) ? nodo.getNodeName() : null;
    }

    /**Ritorna la mappa degli attributi del nodo.
     * @param nodo il nodo interessato.
     * @return la mappa degli attributi del nodo.*/
    private static Map<String,String> attributi(org.w3c.dom.Node nodo){
        if (!isElement(nodo)) return null;
        Map<String, String> map = new HashMap<>();
        NamedNodeMap attr = nodo.getAttributes();
        for(int i=0; i<attr.getLength(); i++){
            org.w3c.dom.Node a = attr.item(i);
            map.put(a.getNodeName(),a.getNodeValue());
        }
        return map;
    }

    /**Ritorna il contenuto di un nodo.
     * @param nodo il nodo interessato.
     * @return il contenuto di un nodo. */
    private static String contenuto(org.w3c.dom.Node nodo) {
        return isElement(nodo) ? null : nodo.getNodeValue();
    }

    /** Esegue la visita dell'intero albero di parsing.
     * @param visitor  visitatore invocato su ogni nodo dell'albero.*/
    @Override
    public void visit (Consumer<Node> visitor){
        DFS(albero, visitor); //chiamata ricorsiva sul figlio di albero per fare DFS
    }

    /** Ritorna la lista (possibilmente vuota) dei links contenuti nella pagina.
     * @return la lista dei links (mai null) */
    @Override
    public List<String> getLinks(){
        List<String> lstLinks = new ArrayList<>();
        DFS(albero, nodo -> {
            if ("a".equalsIgnoreCase(nodo.tag))
                for (String e : nodo.attr.keySet())
                    if ("href".equalsIgnoreCase(e))
                        lstLinks.add(nodo.attr.get(e));
        });
        return lstLinks;
    }

    /** Ritorna la lista (possibilmente vuota) dei nodi con lo specificato tag.
     * @param tag  un nome di tag
     * @return la lista dei nodi con il dato tag (mai null) */
    @Override
    public List<Node> getByTag(String tag){
        List<Node> lstNodi= new ArrayList<>();
        DFS(albero, nodo -> {
            if (tag.equalsIgnoreCase(nodo.tag))
                lstNodi.add(nodo);
        });
        return lstNodi;
    }

    /**Effettua la visità in profondità DFS facendo ricorsione sul figlio
     * @param nodo il nodo di partenza.
     * @param visitor il consumer per effettuare la visita.*/
    private void DFS(NodoFiglio nodo, Consumer<Node> visitor){
        visitor.accept(nodo);
        for (NodoFiglio c : nodo.lstFigli)
            DFS(c, visitor);
    }

    /** Un nodo dell'albero di parsing della pagina */
    private static class NodoFiglio extends Node{
        final NodoFiglio[] lstFigli;
        NodoFiglio(org.w3c.dom.Node nodo){//Come argomento ho il nodo su cui costruire i figli
            super(tag(nodo), attributi(nodo), contenuto(nodo));
            NodeList lstNodi = nodo.getChildNodes();
            lstFigli = new NodoFiglio[lstNodi.getLength()];
            for (int i=0; i<lstNodi.getLength();i++)
                lstFigli[i] = new NodoFiglio(lstNodi.item(i));
        }
    }
}