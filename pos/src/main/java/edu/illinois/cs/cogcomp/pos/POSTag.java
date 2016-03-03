package edu.illinois.cs.cogcomp.pos;

import java.util.*;
import java.io.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.lbjava.*;


/**
 * This program uses {@link POSTagger} to tag a test set with its predictions, indicating which
 * predictions were right and wrong. The accuracy of the learned classifier on the test set and the
 * time taken to perform various operations are also reported. All output is sent to
 * <code>STDOUT</code>.
 *
 * <h4>Usage</h4> <blockquote><code>
 *   java edu.illinois.cs.cogcomp.lbj.pos.POSTag [-q] &lt;testing set&gt;
 * </code></blockquote>
 *
 * <h4>Input</h4> <code>-q</code> mutes the tagged output, leaving only the accuracy report.
 *
 * <h4>Output</h4> One sentence per line, words surrounded by parentheses accompanied by the
 * predicted POS tag, and, immediately preceding each word, either a dash followed by the true label
 * surrounded in square brackets or a plus sign if the prediction was incorrect or correct
 * respectively.
 *
 * @author Nick Rizzolo
 **/
public class POSTag {
    /** The name of the file containing testing data. */
    private static String testingFile;


    /**
     * Implements the program described above.
     *
     * @param args The command line parameters.
     **/
    public static void main(String[] args) {
        // Parse the command line
        if (!(args.length == 1 && !args[0].startsWith("-") || args.length == 2
                && (args[0].equals("-q") || args[0].equals("--quiet")) && !args[1].startsWith("-"))) {
            System.err
                    .println("usage: java edu.illinois.cs.cogcomp.lbj.pos.POSTag [-q] <testing set>\n"
                            + "       If -q is specified, the only output is timing and accuracy\n"
                            + "       information.  Otherwise, the testing set is output with\n"
                            + "       extra tags indicating whether each prediction was correct.");
            System.exit(1);
        }

        boolean quiet = args.length == 2;
        testingFile = args[args.length - 1];

        POSTagger tagger = new POSTagger();
        BufferedReader in = open();
        int correct = 0, incorrect = 0;

        for (String line = readLine(in); line != null; line = readLine(in)) {
            LinkedVector sentence = POSBracketToVector.parsePOSBracketForm(line);
            for (Word word = (Word) sentence.get(0); word != null; word = (Word) word.next) {
                String label = word.partOfSpeech;
                word.partOfSpeech = null;
                String prediction = tagger.discreteValue(word);

                if (prediction.equals(label)) {
                    ++correct;
                    if (!quiet)
                        System.out.print("+");
                } else {
                    ++incorrect;
                    if (!quiet)
                        System.out.print("-[" + label + "]");
                }

                if (!quiet)
                    System.out.print(word + " ");
            }

            if (!quiet)
                System.out.println();
        }

        System.out.println("Accuracy: " + (100 * correct / (double) (correct + incorrect)) + "%");
    }


    /**
     * Opens the file whose name is stored in {@link #testingFile}.
     *
     * @return A stream providing access to the contents of the testing file.
     **/
    private static BufferedReader open() {
        BufferedReader result = null;

        try {
            result = new BufferedReader(new FileReader(testingFile));
        } catch (Exception e) {
            System.err.println("Can't open " + testingFile + " for input: " + e);
            System.exit(1);
        }

        return result;
    }


    /**
     * Reads a line from the specified file stream.
     *
     * @param in The stream to read from.
     * @return The line read from the file.
     **/
    private static String readLine(BufferedReader in) {
        String result = null;

        try {
            result = in.readLine();
        } catch (Exception e) {
            System.err.println("Can't read from " + testingFile + ": " + e);
            System.exit(1);
        }

        return result;
    }
}