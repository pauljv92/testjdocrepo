package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Generator of dummy {@link TextAnnotation}s and their {@link View}s, used for testing.
 *
 * @author Daniel Khashabi
 * @author Christos Christodoulopoulos
 */
public class DummyTextAnnotationGenerator {
    static Logger logger = LoggerFactory.getLogger(DummyTextAnnotationGenerator.class);

    static String basicString = "To annotate or not ; that is the question .";

    static String annotatedString = "The construction of the library finished on time .";
    static String[] pos = {"DT", "NN", "IN", "DT", "NN", "VBD", "IN", "NN", "."};
    static String[] lemmas = {"the", "construction", "of", "the", "library", "finish", "on", "time", "."};
    static String tree = "(S1 (S (NP (NP (DT The) (NN construction)) (PP (IN of) (NP (DT the) (NN library)))) "
            + "(VP (VBD finished) (PP (IN on) (NP (NN time)))) (. .)))";
    static Map<IntPair, String> chunks = new HashMap<>();
    static {
        chunks.put(new IntPair(0,2), "NP");
        chunks.put(new IntPair(2,3), "PP");
        chunks.put(new IntPair(3,5), "NP");
        chunks.put(new IntPair(5,6), "VP");
        chunks.put(new IntPair(6,7), "PP");
        chunks.put(new IntPair(7,8), "NP");
    }
    static IntPair verbSRLPredicate = new IntPair(5, 6);
    static String verbSRLPredicateSense = "01";
    static Map<IntPair, String> verbSRLArgs = new HashMap<>();
    static {
        verbSRLArgs.put(new IntPair(0, 5), "A0");
        verbSRLArgs.put(new IntPair(6, 8), "AM-TMP");
    }

    /**
     * Generate a basic {@link TextAnnotation} (containing just SENTENCE and TOKENS {@link View}s)
     * with the same dummy text repeated over a number of sentences.
     *
     * @param numSentences The number of sentences that the {@link TextAnnotation} will contain
     * @return A dummy basic {@link TextAnnotation}
     */
    public static TextAnnotation generateBasicTextAnnotation(int numSentences) {
        int i = 0;
        List<String[]> docs = new ArrayList<>();
        while (i < numSentences) {
            docs.add(basicString.split(" "));
            i++;
        }
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(docs);
    }

    public static TextAnnotation generateAnnotatedTextAnnotation(String[] viewsToAdd) {
        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(annotatedString);

        for (String viewName : viewsToAdd) {
            switch (viewName) {
                case ViewNames.POS:
                    TokenLabelView posView = new TokenLabelView(viewName, ta);
                    for (int i = 0; i < pos.length; i++) posView.addTokenLabel(i, pos[i], 1.0);
                    ta.addView(viewName, posView);
                    break;
                case ViewNames.LEMMA:
                    TokenLabelView lemmaView = new TokenLabelView(ViewNames.LEMMA, ta);
                    for (int i = 0; i < lemmas.length; i++) lemmaView.addTokenLabel(i, lemmas[i], 1.0);
                    ta.addView(viewName, lemmaView);
                    break;
                case ViewNames.SHALLOW_PARSE:
                    SpanLabelView chunkView = new SpanLabelView(ViewNames.SHALLOW_PARSE, ta);
                    for (IntPair span : chunks.keySet())
                        chunkView.addSpanLabel(span.getFirst(), span.getSecond(), chunks.get(span), 1.0);
                    ta.addView(ViewNames.SHALLOW_PARSE, chunkView);
                    break;
                case ViewNames.PARSE_GOLD:
                case ViewNames.PARSE_STANFORD:
                case ViewNames.PARSE_BERKELEY:
                case ViewNames.PARSE_CHARNIAK:
                    TreeView parseView = new TreeView(viewName, ta);
                    parseView.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(tree));
                    ta.addView(viewName, parseView);
                    break;
                case ViewNames.SRL_VERB:
                    PredicateArgumentView verbSRLView = new PredicateArgumentView(viewName, ta);
                    Constituent predicate = new Constituent("predicate", viewName, ta,
                            verbSRLPredicate.getFirst(), verbSRLPredicate.getSecond());
                    predicate.addAttribute(CoNLLColumnFormatReader.LemmaIdentifier, lemmas[verbSRLPredicate.getFirst()]);
                    predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer, verbSRLPredicateSense);
                    List<Constituent> args = new ArrayList<>();
                    List<String> tempArgLabels = new ArrayList<>();
                    for (IntPair span : verbSRLArgs.keySet()) {
                        args.add(new Constituent("argument", viewName, ta, span.getFirst(), span.getSecond()));
                        tempArgLabels.add(verbSRLArgs.get(span));
                    }
                    String[] argLabels = tempArgLabels.toArray(new String[args.size()]);
                    double[] scores = new double[args.size()];
                    verbSRLView.addPredicateArguments(predicate, args, argLabels, scores);
                    ta.addView(viewName, verbSRLView);
                    break;
                default:
                    logger.error("Cannot provide annotation for {}", viewName);
            }
        }

        return ta;
    }

}