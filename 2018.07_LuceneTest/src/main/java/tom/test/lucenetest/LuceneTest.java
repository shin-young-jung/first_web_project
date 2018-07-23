package tom.test.lucenetest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;

public class LuceneTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		try {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			Directory index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			
			IndexWriter w = new IndexWriter(index, config);
			addDoc(w, "Lucene in Action", "193398817");
			addDoc(w, "Lucene for Dummies", "55320055Z");
			addDoc(w, "Managing Gigabytes", "55063554A");
			addDoc(w, "The Art of Computer Science", "9900333X");
			
			System.out.println(">>>>> IndexWriter ["+w.numDocs()+"]");
			
			w.close();	
			
			System.out.println(">>>>> IndexWriter ["+w+"]");
			
			
			Query q = (new QueryBuilder(analyzer)).createPhraseQuery("title", "lucene");
			
			int hitsPerPage = 10;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        
	        List<LeafReaderContext> leafList = reader.leaves();
	        for(int i=0; i<leafList.size(); i++) {
	        	LeafReaderContext leafCtx = leafList.get(i);
	        	//leafCtx.children().get(0).
	        	
	        	System.out.println("LeafList ["+leafCtx+"] ");
	        }
	        
	        
	        final int cappedNumHits = Math.min(10, 10);
	        final ScoreDoc after = null;
	        final CollectorManager<TopScoreDocCollector, TopDocs> manager = new CollectorManager<TopScoreDocCollector, TopDocs>() {

	            @Override
	            public TopScoreDocCollector newCollector() throws IOException {
	              return TopScoreDocCollector.create(cappedNumHits, after);
	            }

	            @Override
	            public TopDocs reduce(Collection<TopScoreDocCollector> collectors) throws IOException {
	              final TopDocs[] topDocs = new TopDocs[collectors.size()];
	              int i = 0;
	              for (TopScoreDocCollector collector : collectors) {
	                topDocs[i++] = collector.topDocs();
	                
	                
	                System.out.println(">>> ["+collector.topDocs()+"] ");
	                
	              }
	              return TopDocs.merge(0, cappedNumHits, topDocs, true);
	            }

	          };
	        
	          
	        searcher.search(q, manager);
	          
	        
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Document d = searcher.doc(docId);
	            System.out.println(">>["+docId+"]["+hits[i].score+"] " + (i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
	        }
	        
	        
	        reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	public static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
		  Document doc = new Document();
		  doc.add(new TextField("title", title, Store.YES));
		  doc.add(new StringField("isbn", isbn, Store.YES));
		  w.addDocument(doc);
	}

}
