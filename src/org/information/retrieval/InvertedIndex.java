package org.information.retrieval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;

public class InvertedIndex {

    private static HashMap<String, List<Integer>> index;
    private static HashMap<Integer, List<Integer>> tableMapping;//Table used to compare search results for precision and recall.
    public HashMap<String, List<Integer>> getIndex() {
        return index;
    }
    public HashMap<Integer, List<Integer>> getTableMapping() {
        return tableMapping;
    }

    public void create_InvertedIndex() {
        try {
            // Create a document for processing the index.
            Document doc;
            doc = new Document();
            // Initialize the index
            index = new HashMap<>();
            index = doc.processCorpus(getIndex());
            System.out.println("Inverted Index successfully created...");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Fail to create inverted index");
        }
    }

    /*Creating a table of queries and their documents.
    //This is created in order to obtain matches for recall and precision evaluation.*/
    public void create_TableMapping() {
        try {
            tableMapping = new HashMap<>();
            int queryNumber = 0;
            for (int j = 1; j <= 200; j = j + 20) {
                queryNumber++;
                List<Integer> listDocs;
                listDocs = new ArrayList<Integer>();
                for (int i = 0; i < 20; i++) {
                    int docNumber = i + j;
                    listDocs.add(docNumber);
                    tableMapping.put(queryNumber, listDocs);
                }
            }
            System.out.println("Table mapping successfully created...");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Fail to create table mapping");
        }
    }

    public String processResults(String query, int docNumber) throws Throwable {
        // Initializing an ArrayList to save words of query document.
        ArrayList<String> result_words = new ArrayList<>();
        Document doc = new Document();

        String[] words_in_line = query.split("\\W+");
        for (int k = 0; k < words_in_line.length; k++) {
            String word;
            word = doc.processWord(words_in_line[k].toLowerCase());
            if (word == "-1")
                continue;
            result_words.add(word);
        }

        InvertedIndex invIndex = new InvertedIndex();
        HashMap<String, List<Integer>> index = invIndex.getIndex();
        // Getting the first list of the first KEY in index.
        List<Integer> retrievedPages = index.get(result_words.get(0));
        // If there are not any match between query document and corpus (inverted index).

        for (int i = 1; i < result_words.size(); i++) {
            // Intersecting lists.
            retrievedPages.retainAll(index.get(result_words.get(i)));
        }

        String result = "";
        //Get documents
        for (int i = 0; i < retrievedPages.size(); i++) {
            result += doc.getDocument(retrievedPages.get(i));
            result += '\n';
        }
        int countRetrievedPages = retrievedPages.size();
        //Retrieved and relevant pages
        HashMap<Integer, List<Integer>> tableMapping = invIndex.getTableMapping();
        List<Integer> relevantDocuments = tableMapping.get(docNumber);
        //Match a relevant and retrieved document.
        List<Integer> relevant_and_retrieved_documents = new ArrayList<>(relevantDocuments);
        relevant_and_retrieved_documents.retainAll(retrievedPages);

        //PRECISION
        double precision = 0;
        double recall = 0;
        if (retrievedPages.size() != 0)
            precision = (Double.valueOf(relevant_and_retrieved_documents.size()) / Double.valueOf(retrievedPages.size())) * 100.0;
        //RECALL
        if (relevantDocuments.size() != 0)
            recall = (Double.valueOf(relevant_and_retrieved_documents.size()) / Double.valueOf(relevantDocuments.size())) * 100.0;

        String txt = "";
        txt += "QUERY: " + query + '\n';
        txt += '\n';
        //Get documents
        for (int i = 0; i < retrievedPages.size(); i++) {
            txt += doc.getDocument(retrievedPages.get(i));
            txt += '\n';
        }

        txt += "PRECISION: " + precision + " %" + '\n';
        txt += "RECALL: " + recall + "%" + '\n';
        txt += '\n';
        return txt;
    }
}