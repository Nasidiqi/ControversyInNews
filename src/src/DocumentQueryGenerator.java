package src;

import com.google.common.collect.*;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;
import utils.SimpleFileReader;
import utils.SimpleFileWriter;
import utils.StopWordRemover;

import java.util.List;

/*
 * Created by mhjang on 1/27/15.
 * Generates a query from a given document */

public class DocumentQueryGenerator {
    KrovetzStemmer stemmer = new KrovetzStemmer();
    TagTokenizer tagTokenizer = new TagTokenizer();
    StopWordRemover sr = new StopWordRemover();

    public static void main(String[] args) {
            generateWikiNeighbors(20);

    }


    // TF10 query, #sdm
    public static void generateWikiNeighbors(int k) {
        try {
            SimpleFileReader sr = new SimpleFileReader("testquery.txt");
            SimpleFileWriter sw = new SimpleFileWriter("signal_wiki_neighbors.txt");
            WikiRetrieval wr = new WikiRetrieval();
            StringBuilder sb = new StringBuilder();
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");

                String id = tokens[0];
                String query = tokens[1];

                List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(query, k);
                int rank = 1;
                for (ScoredDocument sd : docs) {
                    sb.append(id + "\t" + sd.documentName + "\t" + "sdm \t" + rank + "\n");
                    rank++;

                }
            }
            sw.write(sb.toString());
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateTF10Query() {
        try {
            SimpleFileReader sr = new SimpleFileReader("/home/mhjang/controversyNews/signalmedia-1m.jsonl");
            SimpleFileWriter sw = new SimpleFileWriter("signal_tf10.txt");
            DocumentQueryGenerator queryGen = new DocumentQueryGenerator();
            WikiRetrieval wr = new WikiRetrieval();

            while (sr.hasMoreLines()) {
                String line = sr.readLine();

                Parameters p = Parameters.parseString(line);
                String id = p.get("id", "");
                String content = p.get("content", "");
                String generatedQuery = queryGen.generateQuerybyFrequency(content, 10);
                sw.writeLine(id + "\t" + generatedQuery);

            }
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }











    // using top K frequency
    public String generateQuerybyFrequency(String text, int k) {
        try {
            text = text.replace("\t", " ");
            text = text.replace("\n", " ");

            org.lemurproject.galago.core.parse.Document queryDoc = tagTokenizer.tokenize(text);
            Multiset<String> docTermBag = HashMultiset.create();

            for (String t : queryDoc.terms) {
                String stemmedWord = stemmer.stem(t);
                if (!sr.stopwords.contains(stemmedWord)) {
                    docTermBag.add(stemmedWord);
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if(term.length() > 1) {
                    query.append(term + " ");
                    if (count++ == k) break;
                }
            }
            //        System.out.println();
            return query.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


}
