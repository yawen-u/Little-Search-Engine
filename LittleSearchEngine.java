package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		/** COMPLETE THIS METHOD **/
		HashMap<String, Occurrence> map = new HashMap<String, Occurrence>(100,0.75f);
		Scanner sc = null;
		try {
			sc = new Scanner(new File(docFile));
			while (sc.hasNext()){
				String word = sc.next();
				word = this.getKeyword(word);
				if (word == null) {
					continue;
				}
				
				if (!map.containsKey(word)) {
					Occurrence occur = new Occurrence(docFile, 1);
					map.put(word, occur);
				} else {
					Occurrence temp = map.get(word);
					temp.frequency += 1;
					map.put(word, temp);
				}
			}
		}
		finally {
		    if(sc!=null)
		        sc.close();
		}
		return map;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		/** COMPLETE THIS METHOD **/
		
		for(String key : kws.keySet()){

			ArrayList<Occurrence> l = keywordsIndex.get(key);

			if(l == null){
				l = new ArrayList<>();
				keywordsIndex.put(key, l);
			}

			l.add(kws.get(key));

			insertLastOccurrence(l);

		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		/** COMPLETE THIS METHOD **/
		if (word == null) {
			return null;
		}
		
		word = word.toLowerCase();
		String w = word;
		for (int j = word.length()-1;j >= 0; j--) {
			if (!Character.isLetter(word.charAt(j))) {
				w = word.substring(0, j);
			} else {
				break;
			}
		}
		
		for (int i = 0; i < w.length(); i++) {
			if ((Character.isLetter(word.charAt(i)))== false) {
				return null;
			}
		}
		
		if (noiseWords.contains(w) == false) {
			return w;
		} else {
			return null;
		}
		
		
		// following line is a placeholder to make the program compile
		// you should modify it as needed when you write your code
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		/** COMPLETE THIS METHOD **/
		int size = occs.size();

		if(size<=1)
			return null;

		ArrayList<Integer> trace = new ArrayList<>();

		Occurrence last = occs.remove(occs.size()-1);
		int lastFrequency = last.frequency;

		int left = 0;
		int right = size-2;
		int mid = (left+right)/2;

		while(mid!=left){

			trace.add(mid);

			int midFrequency = occs.get(mid).frequency;

			if(midFrequency == lastFrequency){
				break;
			}else if(midFrequency<lastFrequency){
				right = mid;
			}else{
				left = mid;
			}
			mid = (left+right)/2;
		}

		int midFrequency = occs.get(mid).frequency;
		int rightFrequency = occs.get(right).frequency;

		if(midFrequency<=lastFrequency){
			occs.add(mid, last);
		}else{
			if(rightFrequency<lastFrequency){
				occs.add(right, last);
			}else{
				occs.add(right+1, last);
			}
		}

		return trace;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		/** COMPLETE THIS METHOD **/
	
		
		ArrayList<String> top5Output = new ArrayList<>();
		ArrayList<Occurrence> l1 = keywordsIndex.get(kw1);
		if (l1 == null) {
			l1 = new ArrayList<Occurrence>();
		}
		
		ArrayList<Occurrence> l2 = keywordsIndex.get(kw2);
		if (l2 == null) {
			l2 = new ArrayList<Occurrence>();
		}

		int idx1 = 0;
		int idx2 = 0;

		while(top5Output.size()<5 && idx1<l1.size() && idx2<l2.size()){
			Occurrence o1 = l1.get(idx1);
			Occurrence o2 = l2.get(idx2);

			if(o1.frequency >= o2.frequency){
				if (!top5Output.contains(o1.document)){
					top5Output.add(o1.document);
				}
				idx1++;
			}else{
				if (!top5Output.contains(o2.document)){
					top5Output.add(o2.document);
				}
				idx2++;
			}
		}
		
		if(top5Output.size()<5){
			ArrayList<Occurrence> unfinished = null;
			int idx = -1;

			if (idx1 < l1.size()){
				unfinished = l1;
				idx = idx1;
			}

			if(idx2<l2.size()){
				unfinished = l2;
				idx = idx2;
			}

			while (top5Output.size()<5 && unfinished != null && idx<unfinished.size()){
				Occurrence o = unfinished.get(idx);
				if(!top5Output.contains(o.document)){
					top5Output.add(o.document);
				}
				idx++;
			}
		}

		return top5Output;
	
	}
}
