package lab3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Solver {
	
	static int[] result=new int[100000];
	static /* 
	 * Find Factorial of a Number using Dynamic Programming 
	 */
	int fact_dp(int n)
	{	
	    if (n >= 0) 
	    {
	        result[0] = 1;
	        for (int i = 1; i <= n; ++i) 
	        {
	            result[i] = i * result[i - 1];
	        }
	        return result[n];
	    }
		return result[n];
	}

	public static void main(String[] args) {
		//fact_dp(5000);
		ArrayList<Document> doc = new ArrayList<Document>();
		HashMap<String, Double> a = new HashMap<String, Double>();
		HashMap<String, Integer> ignoreSet = new HashMap<String, Integer>();
		HashMap<String, Double> titleMap = new HashMap<String, Double>();
		ArrayList<Cls> classes = new ArrayList<Cls>();
		try {

			Scanner s1 = new Scanner(new File("ignore.data"));
			while (s1.hasNext()) {
				String s = s1.next();
				ignoreSet.put(s, 1);
			}
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		}

		try {
			Scanner scanner = new Scanner(new File("training.data"));

			while (scanner.hasNextLine()) {
				Document d = new Document();
				Cls temp;
				boolean flag_oldClass = false;
				d.title = scanner.next();
				if (titleMap.containsKey(d.title)) {
					titleMap.put(d.title, titleMap.get(d.title) + 1);
					flag_oldClass = true;

				} else {
					titleMap.put(d.title, 1.0);
					temp = new Cls(d.title);
				}
				String t = scanner.nextLine();
				while (scanner.hasNextLine()) {
					String s2 = scanner.nextLine();
					if (s2.isEmpty()) {
						if (scanner.hasNextLine() && (s2 = scanner.nextLine()).isEmpty()) {
							break;
						}
						// continue;
					}
					String[] words = s2.split(" ");
					for (String s : words) {
						//HashMap<String, Double>p = new HashMap();
						s = s.toLowerCase().replace('"', ' ').trim();

						if (s.length() == 0)
							continue;

						else if (ignoreSet.containsKey(s))
							continue;

						if (d.map.containsKey(s)) {
							d.map.put(s, d.map.get(s) + 1);
						} else {
							d.map.put(s, 1.0);
							if (a.containsKey(s)) {
								a.put(s, a.get(s) + 1);
							} else
								a.put(s, 1.0);
						}
						d.wordCount++;

					}

				}
				doc.add(d);
				if (flag_oldClass == true) {
					for (Cls x : classes) {
						if (x.title.equals(d.title)) {

							x.docs.add(d);
						}
					}

				} else {
					Cls x = new Cls(d.title);
					x.docs.add(d);
					classes.add(x);
				}

			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		for (Cls x : classes)
			x.wordList();
		ArrayList<Document> testDoc = new ArrayList<Document>();
		Scanner scn;
		double B_correct = 0, M_correct = 0, T_correct = 0;
		int size = 0, testLength = 0;
		try {

			scn = new Scanner(new File("test.data"));

			while (scn.hasNextLine()) {
				Document d = new Document();
				d.title = scn.next();
				String t = scn.nextLine();
				while (scn.hasNextLine()) {

					String s2 = scn.nextLine();
					if (s2.isEmpty()) {
						if (scn.hasNextLine() && (s2 = scn.nextLine()).isEmpty()) {
							break;
						}

					}
					String[] words = s2.split(" ");
					for (String s : words) {

						s = s.toLowerCase().replace('"', ' ').trim();

						if (s.length() == 0)
							continue;

						else if (ignoreSet.containsKey(s))
							continue;

						if (d.map.containsKey(s)) {
							d.map.put(s, d.map.get(s) + 1);
						} else {
							d.map.put(s, 1.0);
							if (a.containsKey(s)) {
								a.put(s, a.get(s) + 1);
							} else
								a.put(s, 1.0);
						}
						d.wordCount++;
					}

				}
			
				if (NaiveBayes_Binomial(classes, d, doc,a.size(),0.01))
					
					B_correct++;
				if (NaiveBayes_Multinomial(classes, d, doc,a.size(),1.0))
					M_correct++;
				// testDoc.add(d);
				testLength++;
				System.out.println(testLength);

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("\n \nAccuracy by Binomial Model " + B_correct * 1.0 / testLength * 100 + " percent");
		System.out.println("\n \nAccuracy by Multinomial Model " + M_correct * 1.0 / testLength * 100 + " percent");
		int c = 0;
		// System.out.println(testDoc.size());

		/*
		 * for(Cls x: classes){
		 * 
		 * for (Map.Entry<String, Double> entry : x.wordList().entrySet()){
		 * String s = entry.getKey(); System.out.println(s+ ", value: "+
		 * entry.getValue()+ " #"); }
		 * System.out.println("================\n\n"); }
		 * 
		 */

	}

	public static boolean NaiveBayes_Binomial(ArrayList<Cls> classes, Document test, ArrayList<Document> doc,double dictionarySize, double factor) {
		boolean state = false;
		double prob = -100000;
		String finalState = "";
		for (Cls c : classes) {

			double clsProb = Math.log(c.docs.size() * 1.0 / doc.size());
			for (Map.Entry<String, Double> entry : c.words.entrySet()) {
				String s = entry.getKey();
				if (test.map.containsKey(s))
					clsProb += Math.log((entry.getValue() * 1.0+ factor) /(c.words.size()+ dictionarySize*factor));
				else
					clsProb += Math.log((1 - (entry.getValue() * 1.0+ factor) / (c.words.size()+ dictionarySize*factor)));
				// System.out.println(s+ ", value: "+ entry.getValue()+ " #");
			}
			for (Map.Entry<String, Double> entry : test.map.entrySet()) {
				String s = entry.getKey();
				if (!c.words.containsKey(s)) {
					clsProb += Math.log(factor / (c.totalWordCount()+dictionarySize*factor));

				}

			}

			if (clsProb >= prob) {
				prob = clsProb;
				if (c.title.equals(test.title)) {
					state = true;

				} else
					state = false;
				finalState=c.title;
			}

		}
		//
		//System.out.println(state+" "+finalState);
		return state;
	}
	public static boolean NaiveBayes_Multinomial(ArrayList<Cls> classes, Document test, ArrayList<Document> doc,double dictionarySize,double factor){
		
		
		boolean state = false;
		double prob = -100000;
		String finalState = "";
		for (Cls c : classes) {

			double clsProb = Math.log(c.docs.size() * 1.0 / doc.size());
			for (Map.Entry<String, Double> entry : c.words.entrySet()) {
				String s = entry.getKey();
				if (test.map.containsKey(s))
					clsProb += test.map.get(s)*Math.log((c.wordCount(s) + factor )/ (c.totalWordCount()+factor*dictionarySize));
			
			}
			for (Map.Entry<String, Double> entry : test.map.entrySet()) {
				String s = entry.getKey();
				if (!c.words.containsKey(s)) {
					clsProb += test.map.get(s)* Math.log(factor/ (c.totalWordCount()+dictionarySize*factor));

				}

			}

			if (clsProb >= prob) {
				prob = clsProb;
				if (c.title.equals(test.title)) {
					state = true;

				} else
					state = false;
				finalState=c.title;
			}

		}
	//	System.out.println(state+" "+finalState);
		return state;
		
		
		
		
	}
}
