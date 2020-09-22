package lab3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cls {
	String title;
	ArrayList<Document> docs = new ArrayList<Document>();

	 HashMap<String,Double> words=new HashMap<String , Double>();
	public Cls(String title) {
		super();
		this.title = title;
	}

	public double wordCount(String w) {
		double count = 0;
		for (Document x : this.docs) {

			if (x.map.containsKey(w))
				count++;
		}
		return count;
	}

	public void wordList() {

		HashMap<String, Double> wordList = new HashMap<String, Double>();
		for (Document x : this.docs) {
			for (Map.Entry<String, Double> entry : x.map.entrySet()) {
				String s = entry.getKey();
				if (wordList.containsKey(s)) {
					wordList.put(s, wordList.get(s) + x.map.get(s));

				} else {
					wordList.put(s, x.map.get(s));

				}

			}

		}
		this.words=wordList;
		//return wordList;
	}
	
	
	public double totalWordCount(){
		
		double val=0;
		for(Document x: this.docs){
			
			val+=x.wordCount;
		}
		return val;
		
		
	}

}
