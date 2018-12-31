package com.spenserkao.addressbook.utility;

/**
* <h1>JSON2HTMLTable</h1>
* <p>
* The class that takes various forms of JSON input and converts into two-column HTML table.
* 
* @author  Spenser Kao (SpenserKao@optusnet.com.au)
* @version 1.3.0
* @since   2018-12-10
*/

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.IllegalStateException;
import java.lang.Character;

public class JSON2HTML{

	JSONParser parser = new JSONParser();
	Object parserObj;
	String headLabel; 		// the head label of structure to convert

	public JSON2HTML() {}

	/**
	 * <p>The menthod takes the name of JSON file as input before conversion.</p>
	 * <p>A bit more context dependent, it's mainly for rendering (a) release note, and (b) registered routes (endpoints).</p>
	 *	
	 * @param filenameJSON the name of JSON file
	 * @param summary message summarising the application
	 * @param label the head label of structure to convert, The label will also be used as table caption.
	 * @param takeLatestVersion boolean. If true, take the very first (0) column of latest entry. Othrwise, ignore it. 
	 * @return expected HTML table result
	 */
	public String convert (String filenameJSON, String summary, String label, boolean takeLatestVersion) {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filenameJSON); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));		
		headLabel = label;
		try {
			parserObj = parser.parse(reader);
			return performRest(summary, label, takeLatestVersion);
		} catch (FileNotFoundException fnfe) {
			 fnfe.printStackTrace();
		} catch (IOException ioe) {
			 ioe.printStackTrace();
		} catch (Exception e) {
			 e.printStackTrace();
		}
		return "";
	}

	/**
	 * <p>The method takes the string of JSON as input before conversion.</p>
	 * <p>A bit more context dependent, it's mainly for the return of full list or search result.</p>	 
	 * @param strJSON JSON str
	 * @param label the head label of structure to convert	 
	 * @return expected HTML table result
	 */
	public String convert (String strJSON, String label) {
		headLabel = label;
		try {
			parserObj = parser.parse(strJSON);
			return performRest(null, label, false);		
		} catch (Exception e) {
			 e.printStackTrace();
		}
		return "";
	}

	/**
	 * <p>The constructor to take name of JSON file as input before conversion.
	 * @param label the head label of structure to convert	 	
	 * @param takeLatestVersion boolean. If true, take the very first (0) column of latest entry. Othrwise, ignore it. 
	 * @return resultant HTML table converted from JSON input
	 */
	private String performRest(String summary, String label, boolean takeLatestVersion) {
		Map<String, Object> model = new HashMap<>();		
		JSONObject jsonObject = (JSONObject) parserObj;
		JSONArray releaseNote = (JSONArray) jsonObject.get(label);	
		Iterator<?> verDescArray = releaseNote.iterator();		

		if (summary != null || summary != "") {
			model.put("summary", summary);
		}
		model.put("tableCaption", label);
		String[] columnHeadings = getColumnHeadings(releaseNote);
		model.put("columnHeadings", filterPrefixNumberAndSpace(columnHeadings));

		// The use of LInkedHashMap assures order of creation of key-value pair
		LinkedHashMap<String, String> keyVal_Pairs = new LinkedHashMap<>();

        while (verDescArray.hasNext()) {
        	JSONObject verDescPair = (JSONObject) verDescArray.next();

        	String key = (String) verDescPair.get( columnHeadings[0] );
        	String value = (String) verDescPair.get( columnHeadings[1] );
        	keyVal_Pairs.put(key, value);
            System.out.println("DBG: key=" + key + " value=" + value);
        }			

		// Note: the templates must resides under ~src/main/resources
		if (takeLatestVersion) {
			model.put("latestVersion", findLatestVersion(keyVal_Pairs));
			System.out.println(label + ": " + findLatestVersion(keyVal_Pairs));
		}
		model.put("keyVal_Pairs", keyVal_Pairs);
		return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/vm2html.vm"));
	}

	/**
	 * <p>Filter out the first character, if a number, of each element of the input string array.</p>
	 * @param sArray the JSONArray instance containing key-value pairs 	
	 * @return post-filtering string array
	 */
	private String[] filterPrefixNumberAndSpace (String[] sArray) {
		String [] filteredArray = new String[sArray.length];
		for (int i=0; i<sArray.length; i++){
			filteredArray[i] = sArray[i].replaceAll("@", "").trim();
			System.out.println("DBG: filteredArray[" + i + "]=" + filteredArray[i]);
		};
		return filteredArray;
	}

	/**
	 * <p>Extract heading of key and value columns of input JSONArray instance.</p>
	 * @param jsonarray the JSONArray instance containing key-value pairs 	
	 * @return an array containing first the heading of the key, and then that of the value
	 */
	private String[] getColumnHeadings(JSONArray jsonarray) {
		String firstTwoKeyValuePairs = (String) jsonarray.get(0).toString();
		System.out.println("firstTwoKeyValuePairs=" + firstTwoKeyValuePairs);
		
		String openingCurlyBrace = "\\{[\\s]*";
		String closingCurlyBrace = "[\\s]*\\}";
		String keyLabelCore = "(.*)";	// [a-zA-Z\\s]*
		String keyLabel = "\"" + keyLabelCore + "\"";
		String valueLabelcore = ".*";
		String valueLabel = "\"" + valueLabelcore + "\"";
		String keyValuePair = keyLabel + "[\\s]*:[\\s]*" + valueLabel;
		String delimeter = "[,\\s]*";
		String keyValuePairsPtn = openingCurlyBrace + keyValuePair + delimeter + keyValuePair + closingCurlyBrace;

		System.out.println("keyValuePairsPtn=" + keyValuePairsPtn);

		String[] result;
		try {
			Pattern ptrn = Pattern.compile(keyValuePairsPtn);
			Matcher m = ptrn.matcher(firstTwoKeyValuePairs);	
			if (m.matches()) {
				System.out.println("DBG: Found matching ...");
				System.out.println("DBG: group(0)=" + m.group(0));
				System.out.println("DBG: group(1)=" + m.group(1));
				System.out.println("DBG: group(2)=" + m.group(2));	
				result = new String[] {m.group(1), m.group(2)};
			} else {
				System.out.println("DBG: Found no matching ...");
				result = new String[2];
			}
		} catch (IllegalStateException ise) {
			ise.printStackTrace();	
			result = new String[2];		
		}
		
		return result;
	} 

	/**
	 * <p>Extract number of latest version.</p>
	 * @param verDescArray An array of version-description pairs 	
	 * @return expected number of version
	 */
	private Object findLatestVersion(LinkedHashMap verDescArray) {
		Object[] vers = verDescArray.keySet().toArray();
		return vers[0];
	}
}