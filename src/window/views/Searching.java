package window.views;

import org.information.retrieval.Document;
import org.information.retrieval.InvertedIndex;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Searching {
    private JPanel searchPanel;
    private JTextField searchField;
    private JTextArea textResult;
    private JButton searchButton;
    private JScrollPane scrollPanel;
    private JLabel counterLabel;

    public void showNotification() {
        String notification = "Your search " + searchField.getText() + " - did not watch any documents" + '\n' + '\n' +
                "Suggestions:" + '\n' + '\n' +
                "Make sure all words are spelled correctly." + '\n' +
                "Try different keywords." + '\n' + "Try more general keywords.";
        textResult.setText(notification);
    }

    public void searchQuery(String query) {
        try {
            if (!query.equals("")) {
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
                if (result_words.size() != 0) { //There are elements in result list.
                    InvertedIndex invIndex = new InvertedIndex();
                    HashMap<String, List<Integer>> index = invIndex.getIndex();
                    // Getting the first list of the first KEY in index.
                    List<Integer> retrievedPages = index.get(result_words.get(0));
                    // If there are not any match between query document and corpus (inverted index).
                    if (retrievedPages == null) {
                        showNotification();
                        counterLabel.setText("");

                    } else {
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
                        counterLabel.setText("About: " + String.valueOf(retrievedPages.size()) + " results");
                        textResult.setText(result);
                    }
                } else {
                    showNotification();
                    counterLabel.setText("");
                }
                //There is nothing in query document.
            } else {
                counterLabel.setText("");
                textResult.setText("");
            }


        } catch (Throwable e1) {
            // TODO Auto-generated catch block
        }

    }

    public Searching() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input_line = "";
                input_line = searchField.getText();
                searchQuery(input_line);
            }
        });
    }

    public static void main(String[] args) {
        new Searching();
        JFrame frame = new JFrame("Searching");
        frame.setContentPane(new Searching().searchPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("Inverted Index");
        frame.setBounds(100, 100, 1000, 600);
        ImageIcon img = new ImageIcon("search.png");
        frame.setIconImage(img.getImage());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        try {
            InvertedIndex inverted_index = new InvertedIndex();
            //Creating inverted index.
            inverted_index.create_InvertedIndex();
            //Creating table for posterior analysis of precision and recall.
            inverted_index.create_TableMapping();

            Document doc = new Document();
            //Initialize queries for obtaining results in output.txt file
            doc.initializeQueries();
            ArrayList<String> query = doc.getQuery();
            String outputFile = "";
            for (int i = 0; i < query.size(); i++) {
                outputFile += inverted_index.processResults(query.get(i), i + 1);
                //Create an output text file.
            }
            System.out.println("Testing output.txt created...");
            File file = new File("output.txt");
            BufferedWriter output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(outputFile);
            output.close();
            System.out.println("Releasing search engine...");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Fail to release search engine...");
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
