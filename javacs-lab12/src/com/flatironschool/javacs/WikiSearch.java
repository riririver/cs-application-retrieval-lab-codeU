package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		
		Map<String, Integer> unionMap = new HashMap<String, Integer>();
		
	
		unionMap.putAll(this.map);
		
		Set<String> thatKeys = that.map.keySet();
		
		Iterator<String> ii =thatKeys.iterator();
		
		while(ii.hasNext())
		{
			String thatKey = ii.next();
			Integer thatRelevance = that.map.get(thatKey);
			Integer thisRelevance = unionMap.get(thatKey);
			 
			if (thisRelevance != null)
				thatRelevance = thatRelevance + thisRelevance;
			
			unionMap.put(thatKey, thatRelevance);
		}
		
		return new WikiSearch (unionMap);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		Map<String, Integer> andMap = new HashMap<String, Integer>();
		
			
		Set<String> thisKeys = this.map.keySet();
		
		Iterator<String> ii =thisKeys.iterator();
		
		while(ii.hasNext())
		{
			String thisKey = ii.next();
			Integer thatRelevance = that.map.get(thisKey);
						 
			if (thatRelevance != null)
			{
				Integer thisRelevance = this.map.get(thisKey);
				thisRelevance = thatRelevance + thisRelevance;
				andMap.put(thisKey, thisRelevance);
			}
			
			
		}
		
		return new WikiSearch (andMap);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Map<String, Integer> minusMap = new HashMap<String, Integer>();
		
		
		Set<String> thisKeys = this.map.keySet();
		
		Iterator<String> ii =thisKeys.iterator();
		
		while(ii.hasNext())
		{
			String thisKey = ii.next();
			Integer thatRelevance = that.map.get(thisKey);
						 
			if (thatRelevance == null)
			{
				Integer thisRelevance = this.map.get(thisKey);
				minusMap.put(thisKey, thisRelevance);
			}
			
			
		}
		
		return new WikiSearch (minusMap);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
		
		List<Entry<String, Integer>> listOfRel = new ArrayList<>();
		listOfRel.addAll(this.map.entrySet());
		 Collections.sort(listOfRel, new Comparator<Entry<String, Integer>>() {
    			@Override
				public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1) {
					
					return ((Integer)arg0.getValue()).compareTo((Integer)arg1.getValue());
				}
		 });
		 
		 return listOfRel;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
