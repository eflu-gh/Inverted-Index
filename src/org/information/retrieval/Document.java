package org.information.retrieval;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

public class Document {

    private SnowballStemmer stemmer;
    private List<String> stopWords;
    private String LANGUAGE = "english"; //Setting english language in Porter's algorithm
    private final String CORPUS_FILE = System.getProperty("user.dir") + "/corpus.txt";
    private final String DOCUMENTS = System.getProperty("user.dir") + "/documents/";
    private final String STOPWORDS_FILE = System.getProperty("user.dir") + "/stopWords.txt";
    private static ArrayList<String> query;

    public Document() throws Throwable {
        // Load the language Stemmer.
        // Library dowloaded from https://snowballstem.org/download.html
        Class stemClass = Class.forName("org.tartarus.snowball.ext." + LANGUAGE + "Stemmer");
        stemmer = (SnowballStemmer) stemClass.newInstance();
    }
    //List of queries to test precision and recall.
    public void initializeQueries() {
        query = new ArrayList();
        query.add("what are the seven wonders of the world");
        query.add("how many theaters are in broadway");
        query.add("what is the oldest language in the world");
        query.add("What is the real meaning of Google in English?");
        query.add("where the nasa launch rockets");
        query.add("List of the most expensive watches in the world");
        query.add("What is Watson");
        query.add("How many pictures have the louvre museum");
        query.add("how many data server does Facebook have");
        query.add("how is mapped the internet in the ocean");
    }

    public ArrayList<String> getQuery() {
        return query;
    }

    //Set a word in stemmer form.
    public String setStemmer(String word) {
        stemmer.setCurrent(word);
        stemmer.stem();
        word = stemmer.getCurrent();
        return word;
    }

    //Load a list of stop words in stopWords.txt file.
    public void loadStopWords() {
        BufferedReader reader;
        try {
            this.stopWords = new ArrayList<String>();
            reader = new BufferedReader(new FileReader(STOPWORDS_FILE));
            String line = reader.readLine();
            while (line != null) {
                this.stopWords.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException a) {
            System.err.print("File " + STOPWORDS_FILE + " not found");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Processing a word.
    public String processWord(String word) {
        loadStopWords();
        if (stopWords.contains(word))
            return "-1";
        Pattern p = Pattern.compile("([0-9]*|[a-z]|[a-z].|[0-9]*[a-z]{1,2})");// . represents single character
        // No numbers, no single letters, no a letter follow by any character, no
        // number(s) follow by one to two letters (e.g. 15th, 1900s, etc.)
        // If a words matches with regex above in Pattern p.
        Matcher m = p.matcher(word);
        boolean b = m.matches();
        if (b)
            return "-1";
        // Converting a word to a stem (stemming algorithm)
        word = setStemmer(word);
        return word;
    }

    /**
     * preProcess data in terms of: convert each word to lower case, verify if a
     * word is a stop word in order to not consider it. This function also creates a
     * HashMap <K,V> where K is a key or word and V is a value (ArrayList of
     * Integers). The ArrayList saves a posting list. A posting list is the set of
     * documents where a term K appears.
     *
     * @param index the index where a term K contains a value V (arrayList of
     *              Integers that refers to a document)
     * @return a processed index
     */
    public HashMap<String, List<Integer>> processCorpus(HashMap<String, List<Integer>> index) throws Throwable {
        try {
            File fileDir = new File(CORPUS_FILE);
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));

            String str;
            int countDoc = 1;
            boolean flag = true;

            String txt = "";
            while ((str = in.readLine()) != null) {
                if (!str.equals("")) {
                    txt += str;
                    txt += '\n';
                }

                String[] words_in_line = str.split("\\W+");
                flag = true;
                for (int k = 0; k < words_in_line.length; k++) {
                    flag = true;
                    String word;
                    word = processWord(words_in_line[k].toLowerCase());
                    // if word belongs to stop words list or a pattern not allowed, then, do not
                    // create an index.
                    if (word.equals("-1"))
                        continue;

                    List<Integer> posting_list = index.get(word);// Getting the value of index key
                    // (index.get(word[0]))
                    if (posting_list == null) { // if there is not a value in a respective key, then
                        posting_list = new ArrayList<Integer>();
                        posting_list.add(countDoc);
                        index.put(word, posting_list);
                    } else {
                        if (posting_list.contains(countDoc)) {// If the value was added to the list.
                            flag = false;
                            continue;
                        }
                        if (flag) // if the value was already added to the list, then do not add again.
                            posting_list.add(countDoc);
                    }
                }

                if (str.equals("")) {// It is used to know the number of document. Each document is separated by a
                    // new line of empty string ""
                    File file = new File(DOCUMENTS + "doc" + countDoc + ".txt");
                    BufferedWriter output = null;
                    output = new BufferedWriter(new FileWriter(file));
                    output.write(txt);
                    output.close();
                    txt = "";
                    countDoc++;// Count the number of documents
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.err.print("File " + CORPUS_FILE + " not found");
            System.exit(1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return index;
    }

    public String getDocument(int numDoc) throws Throwable {
        String textDocument = "";
        String str;

        File file = new File(DOCUMENTS + "doc" + numDoc + ".txt");
        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        while ((str = in.readLine()) != null) {
            textDocument += str;
            textDocument += '\n';
        }
        return textDocument;
    }
}