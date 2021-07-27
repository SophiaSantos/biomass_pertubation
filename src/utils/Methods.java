package utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;
public class Methods {

	
	public static Map<String, Double> convetToMapStringDouble(Map<String, String[]> map, int idxDouble){
		
		Map<String, Double> ret = new HashMap<String, Double>();
		for(String id : map.keySet()){
			String dS = map.get(id)[idxDouble];
			ret.put(id, Double.valueOf(dS));
		}
		
		return ret;
	}
	
	public static Map<String, String> readDic(String f) throws IOException{
		
		Map<String, String> t = new HashMap<String, String>();
		
		Map<String, String[]>values=  FileUtils.readTableFileFormat(f, ";", 0);
		
		for(String[] v : values.values())
			t.put(v[0], v[1]);
		
		return t;
	}
}
