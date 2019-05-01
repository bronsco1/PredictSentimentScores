import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Bayes {

	public static void main(String[] args) throws IOException {
		ArrayList<String> userIds = new ArrayList<String>();
		// 1. read in a movie reviews file

		InputStream is = new FileInputStream("078062565X.txt");
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));

		String line = buf.readLine();

		StringBuilder sb = new StringBuilder();

		while (line != null) {
			if (line.matches("(?s)^[A-Z0-9]{13,14}$")) {
				userIds.add(line);
			}
			sb.append(line).append("\n");
			line = buf.readLine();
		}

		String fileAsString = sb.toString();
		buf.close();

		// 2. split file string into array of strings by \n\n character
		Pattern ptn = Pattern.compile("^[A-Z0-9]{13,14}$",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		String reviews[] = ptn.split(fileAsString, 0);
		for (String review : reviews) {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			int score = 2; // Default is Neutral. 1 = Negative, 2 = Neutral, 3 = Positive
			String scoreStr;
			Annotation annotation = pipeline.process(review);
			int posScore = 0, negScore = 0, neutralScore = 0;

			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				scoreStr = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
				Tree tree = sentence.get(SentimentAnnotatedTree.class);
				score = RNNCoreAnnotations.getPredictedClass(tree);
//				avgScore += score;
				if (scoreStr.equalsIgnoreCase("positive")) {
					posScore += score;
//					System.out.println("posscore " + posScore);
				} else if (scoreStr.equalsIgnoreCase("negative")) {
					negScore += score;
//					System.out.println("negscore " + negScore);
				} else { // neutral
					neutralScore += score;
//					System.out.println("neturalscore " + neutralScore);
				}
//				System.out.println(scoreStr + "\t" + score + "\t" + sentence);
			}
			System.out.println(review + "\n" + "Positive" + "\t" + posScore + "\n" + "Negative" + "\t" + negScore + "\n"
					+ "Neutral" + "\t\t" + neutralScore + "\n");
		}

	}
}
