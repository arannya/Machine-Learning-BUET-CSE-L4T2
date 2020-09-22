package knn;

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

	public static void main(String[] args) {
		ArrayList<Document> doc = new ArrayList<Document>();
		HashMap<String, Double> a = new HashMap<String, Double>();
		HashMap<String, Integer> ignoreSet = new HashMap<String, Integer>();
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
				d.title = scanner.next();
				String t=scanner.nextLine();
				while (scanner.hasNextLine()) {
					String s2 = scanner.nextLine();
					if (s2.isEmpty()) {
						if (scanner.hasNextLine() && (s2=scanner.nextLine()).isEmpty())
							{
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
					//	System.out.print(" "+ s);
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

			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		ArrayList<Document> testDoc = new ArrayList<Document>();
		Scanner scn;
		double H_correct = 0, E_correct = 0, T_correct = 0;
		int size = 0, testLength = 0;
		try {
		
			scn = new Scanner(new File("test.data"));

			size = Integer.parseInt(scn.nextLine());
			while (scn.hasNextLine()) {
				Document d = new Document();
				d.title = scn.next();
				String t=scn.nextLine();
				while (scn.hasNextLine()) {
				
					String s2 = scn.nextLine();
					if (s2.isEmpty()) {
						if (scn.hasNextLine() && (s2=scn.nextLine()).isEmpty())
							{
							break;
							}
						//continue;
					}
					String[] words = s2.split(" ");
					for (String s : words) {
					
						s = s.toLowerCase().replace('"', ' ').trim();
					
						if (s.length() == 0)
							continue;
						

						else if (ignoreSet.containsKey(s))
							continue;
						//System.out.print(" "+ s);
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
				//	System.out.println();
					

				}
				testDoc.add(d);
				testLength++;

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Map.Entry<String, Double> entry : a.entrySet()) {

			String s = entry.getKey();
			double IDF = Math.log(doc.size() * 1.0 / entry.getValue());
			for (int i = 0; i < testDoc.size(); i++) {
				if (doc.get(i).map.containsKey(s)) {

					double t = doc.get(i).map.get(s) / doc.get(i).wordCount * IDF;
					doc.get(i).t.put(s, t);
					doc.get(i).t_length += t * t;

				}
				if (testDoc.get(i).map.containsKey(s)) {

					Double t = testDoc.get(i).map.get(s) / testDoc.get(i).wordCount * IDF;
					testDoc.get(i).t.put(s, t);
					testDoc.get(i).t_length += t * t;

				}

			}
			for (int i = testDoc.size(); i < doc.size(); i++) {
				if (doc.get(i).map.containsKey(s)) {

					double t = doc.get(i).map.get(s) / doc.get(i).wordCount * IDF;
					doc.get(i).t.put(s, t);
					doc.get(i).t_length += t * t;

				}

			}

		}
		
		int c = 0;
		System.out.println(testDoc.size());
		for (Document d : testDoc) {
			System.out.println(c++);

			if (determiner(d.title, KNN(a, d, doc, "Hamming", size))) {
				H_correct++;

			}

			if (determiner(d.title, KNN(a, d, doc, "Euclidean", size))) {
				E_correct++;

			}

			if (determiner(d.title, KNN(a, d, doc, "TF-IDF", size)))
				T_correct++;

		}

		System.out.println("Accuracy by Hamming Method : " + H_correct * 100 / (testLength) + " percent");
		System.out.println("Accuracy by Euclidean Method : " + E_correct * 100 / (testLength) + " percent");
		System.out.println("Accuracy by Cosine Similarity Method : " + T_correct * 100 / (testLength) + " percent");
	}

	static int hamming(HashMap<String, Double> x, HashMap<String, Double> y) {
		int val = 0;
		if (x.size() < y.size())
			for (Map.Entry<String, Double> entry : x.entrySet()) {

				if (y.containsKey(entry.getKey()))
					val++;
			}
		else {
			for (Map.Entry<String, Double> entry : y.entrySet()) {

				if (x.containsKey(entry.getKey()))
					val++;
			}
		}
		val = x.size() + y.size() - 2 * val;
		return val;
	}

	static double Euclidean(HashMap<String, Double> x, HashMap<String, Double> y) {
		double val = 0;
		for (Map.Entry<String, Double> entry : x.entrySet()) {
			String s = entry.getKey();
			if (y.containsKey((s))) {
				val = val + (entry.getValue() - y.get(s)) * (entry.getValue() - y.get(s));

			} else
				val = val + entry.getValue() * entry.getValue();
		}
		for (Map.Entry<String, Double> entry : y.entrySet()) {

			if (!x.containsKey(entry.getKey()))
				val = val + entry.getValue() * entry.getValue();
		}
		return Math.sqrt(val);
	}

	static double TF_IDF(HashMap<String, Double> a, HashMap<String, Double> x, HashMap<String, Double> y,
			ArrayList<Document> doc, double count1, double count2) {

		double dot = 0;
		double val_x = 0;
		double val_y = 0;

		for (Map.Entry<String, Double> entry : x.entrySet()) {

			String s = entry.getKey();

			double IDF = Math.log(doc.size() * 1.0 / a.get(s));
			if (y.containsKey(s)) {

				dot = dot + entry.getValue() / count1 * IDF * (y.get(s) / count2) * IDF;

			}
			val_x += (entry.getValue() / count1 * IDF) * (entry.getValue() / count1 * IDF);
		}

		if (dot == 0)
			return 0;
		for (Map.Entry<String, Double> entry : y.entrySet()) {
			String s = entry.getKey();

			val_y += (entry.getValue() * 1.0 / count2 * Math.log(doc.size() * 1.0 / a.get(s)))
					* (entry.getValue() * 1.0 / count2 * Math.log(doc.size() * 1.0 / a.get(s)));

		}

		return dot / (Math.sqrt(val_x) * Math.sqrt(val_y));
	}

	static ArrayList<Document> KNN(HashMap<String, Double> a, Document test, ArrayList<Document> doc, String criteria,
			int size) {

		ArrayList<Document> result = new ArrayList<Document>();
		if (criteria.equals("Hamming")) {
			for (Document x : doc) {
				double temp = hamming(x.map, test.map);
				for (int i = 0; i < size; i++) {

					if (result.size() == i) {
						x.HamDistance = temp;
						result.add(i, x);
						break;
					} else if (temp > result.get(i).HamDistance)
						continue;
					else {
						result.add(i, x);
						x.HamDistance = temp;
						if (result.size() > size)
							result.remove(size);
						break;
					}
				}

			}

		} else if (criteria.equals("Euclidean")) {

			for (Document x : doc) {
				double temp = Euclidean(x.map, test.map);
				for (int i = 0; i < size; i++) {

					if (result.size() == i) {
						x.E_Distance = temp;
						result.add(i, x);
						break;
					} else if (temp > result.get(i).E_Distance)
						continue;
					else {
						result.add(i, x);
						x.E_Distance = temp;
						if (result.size() > size)
							result.remove(size);
						break;
					}
				}

			}

		} else if (criteria.equals("TF-IDF")) {

			for (Document x : doc) {

				double temp = TF_IDF(x.t, test.t, x.t_length, test.t_length);

				for (int i = 0; i < size; i++) {

					if (result.size() == i) {
						x.TF_IDF = temp;
						result.add(i, x);
						break;
					} else if (temp < result.get(i).TF_IDF)
						continue;
					else {
						result.add(i, x);
						x.TF_IDF = temp;
						if (result.size() > size)
							result.remove(size);
						break;
					}
				}

			}

		}

		return result;
	}

	static double TF_IDF(HashMap<String, Double> x, HashMap<String, Double> y, double len1, double len2) {

		double val = 0;
		if (x.size() < y.size())
			for (Map.Entry<String, Double> entry : x.entrySet()) {
				String s = entry.getKey();
				if (y.containsKey((s))) {
					val = val + entry.getValue() * y.get(s);

				}

			}
		else {
			for (Map.Entry<String, Double> entry : y.entrySet()) {
				String s = entry.getKey();
				if (x.containsKey((s))) {
					val = val + entry.getValue() * x.get(s);

				}

			}
		}

		return val / (Math.sqrt(len1) * Math.sqrt(len2));
	}

	static boolean determiner(String real, ArrayList<Document> d) {

		int maxFreq = 0;
		if (d.size() == 1 || d.size() == 2) {
			if (d.get(0).title.equals(real))
				return true;
			else
				return false;
		}
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		for (int i = 0; i < d.size(); i++) {
			if (!temp.containsKey(d.get(i).title))
				temp.put(d.get(i).title, 1);
			else
				temp.put(d.get(i).title, temp.get(d.get(i).title) + 1);
			if (temp.get(d.get(i).title) > maxFreq)
				maxFreq = temp.get(d.get(i).title);
		}

		if (!temp.containsKey(real) || temp.get(real) != maxFreq)
			return false;
		return true;
	}
}
