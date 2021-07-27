package coef;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLWriter;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.JSBMLValidatorException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;
import utils.Methods;

public class Coeficients {
	
	protected Map<String, Double> macro;
	protected Map<String, Map<String, Double>> single;
	protected Map<String, String> dict;
	protected Map<String, Double> mSingle;
	protected Map<String, String> singleToMacro;
	protected Map<String, Double> balanceSingle;
	
	public Coeficients(Map<String, Double> macro,
			Map<String, Map<String, Double>> single, Map<String, String> dict, Map<String, Double> mSingle) {
		super();
		this.macro = macro;
		this.single = single;
		this.dict = dict;
		this.mSingle = mSingle;
		singleToMacro = new HashMap<String, String>();
		balanceSingle = new HashMap<String, Double>();
		balanceSingle.put("atp", 59.81);
		
		for(String macroId : single.keySet()){
			Map<String, Double> sMap = single.get(macroId);
			for(String singleId : sMap.keySet())
				singleToMacro.put(singleId, macroId);
		}
		
	}
	
	public Set<String> getMacroIds(){
		return new HashSet<String>(macro.keySet());
	}
	
	public Set<String> getSingleIds(){
		return new HashSet<String>(mSingle.keySet());
	}
	
	public ReactionCI changeBiomass(ReactionCI r, String id, double var) throws Exception{
		
		if(macro.containsKey(id))
			return changeMacro(r, id, var);
		
		if(mSingle.containsKey(id))
			return changeSingle(r, id, var);
		
		
		throw new Exception("Algo esta mal :P");
		
		
	}
	
	protected ReactionCI changeMacro(ReactionCI r, String id, double var){
		
		ReactionCI rCI = r.clone();
		Map<String, Double> newMacro = varCoef(macro, id, var);
		
		for(StoichiometryValueCI s : r.getReactants().values()){
			
			StoichiometryValueCI sNew = s.clone();
			String singleId = dict.get(s.getMetaboliteId());
			String macro = singleToMacro.get(singleId);
			
			if(mSingle.containsKey(singleId)){
				double atp = balanceSingle.get(singleId) == null?0:balanceSingle.get(singleId);
				double stoiq = (newMacro.get(macro) * single.get(macro).get(singleId) * 1000 / mw(newMacro)) + atp;
				sNew.setStoichiometryValue(stoiq);
				rCI.getReactants().put(sNew.getMetaboliteId(), sNew);
			}
		}
		
		return rCI;
	}
	
	
	protected Map<String, Double> varCoef(Map<String, Double> base, String toChange, double var){
		Double sum = 0.0;
		for(String  id: base.keySet()){
			sum+=base.get(id);
		}
//		System.out.print(sum);
		
		Map<String, Double> ret = new HashMap<String, Double>();
		for(String  id: base.keySet()){
			
			if(id.endsWith(toChange)) ret.put(id, base.get(id)+(var*base.get(toChange)));
			else ret.put(id, (base.get(id)*(sum-(var*base.get(toChange))))/(sum-(base.get(toChange))));
		}
		
		return ret;
	}
	
	protected Double mw( Map<String, Double> percSingle){
		
		Double ret =0.0;
		
		for(String sId : percSingle.keySet()){
			ret +=  mSingle.get(sId)*percSingle.get(sId);
		}
		return ret;
		
	}
	
	
	
	protected ReactionCI changeSingle(ReactionCI r, String id, double var){
		
		ReactionCI rCI = r.clone();
		String macro = singleToMacro.get(id);
		Map<String, Double> newSingle = varCoef(single.get(macro), id, var);
		for(StoichiometryValueCI s : r.getReactants().values()){
			
			String singleId = dict.get(s.getMetaboliteId());
			StoichiometryValueCI sNew = s.clone();
			
			if(newSingle.containsKey(singleId)){
				double atp = balanceSingle.get(singleId) == null?0.0:balanceSingle.get(singleId);
				
				double stoiq = (this.macro.get(macro) * newSingle.get(singleId) * 1000 / mw(newSingle)) + atp;
				sNew.setStoichiometryValue(stoiq);
				rCI.getReactants().put(sNew.getMetaboliteId(), sNew);
			}
		}
		System.out.print(mw(newSingle));
		
		return rCI;
	}

	public static Coeficients buildCoeficients(String macroFile, String dicFile, String coefFolder) throws IOException{
		
		Map<String, String[]> macroString = FileUtils.readTableFileFormat(macroFile, ";", 0);
		Map<String, Double> macro = Methods.convetToMapStringDouble(macroString, 1);
		
		Map<String, String> dict = Methods.readDic(dicFile);
		Map<String, Double> mSingle = new HashMap<String, Double>();
		
		Map<String, Map<String, Double>> allSingles = new HashMap<String, Map<String, Double>>();
		for(String m : macro.keySet()){
			
			String file = coefFolder + "/"+m+".csv";
			Map<String, String[]> singleString = FileUtils.readTableFileFormat(file, ";", 0);
			Map<String, Double> sV = Methods.convetToMapStringDouble(singleString, 1);
			Map<String, Double> ms = Methods.convetToMapStringDouble(singleString, 2);
			mSingle.putAll(ms);
			allSingles.put(m, sV);
		
			
		}
		
		
		return new Coeficients(macro, allSingles, dict, mSingle);
		
	}

}
