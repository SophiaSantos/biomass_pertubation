package coef.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.core.criticality.*;


import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;
import coef.Coeficients;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.JSBMLValidatorException;

public class Study {
	
	public void convertModels() throws JSBMLValidatorException, TransformerException, ParserConfigurationException, SAXException, IOException{

		String folder = "C:\\Users\\Sophia Santos\\Dropbox\\SBML\\coeficients\\files\\";
		String filePath = folder + "sbml_ecoli.xml";

		JSBMLValidator validator = new JSBMLValidator(filePath);

		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			e.printStackTrace();
		}

		validator.validate(filePath);

	}

	
	public static void main(String... args) throws Exception{
		
		String filePath = "C:\\Users\\Sophia Santos\\Dropbox\\DD-DeCaf\\Models\\Ec_iAF1260.xml";
		String fileSbml = "C:\\Users\\Sophia Santos\\Dropbox\\SBML\\coeficients\\files\\sbml_ecoli.xml";
		
		JSBMLReader reader = new JSBMLReader(filePath, "");
		Container cont = new Container(reader);
		Set<String> bMetabolites = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(bMetabolites);
		
		Coeficients coef = Coeficients.buildCoeficients("C:\\Users\\Sophia\\Desktop\\SBML\\coeficients\\macro.csv", 
				"C:\\Users\\Sophia\\Desktop\\SBML\\coeficients\\dicMet.csv", "C:\\Users\\Sophia\\Desktop\\SBML\\coeficients\\files\\coef");
		
		
		
		Set<String> macro = coef.getMacroIds();
		Set<String> single = coef.getSingleIds();
//		Double[] vars = {0.05, 0.075, 0.1, 0.5};
		Double[] vars = {0.0};
		
		List<String> headers = new ArrayList<String>();
		Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> biomass = new HashMap<String, Map<String,Double>>();
		
		addToMapBiomass(biomass, cont.getReaction(cont.getBiomassId()), "original");
		
		SteadyStateSimulationResult result2 = simulateContainer(cont);

		System.out.println( "original\t" +cont.getBiomassId() + "\t" + result2.getFluxValues().get(cont.getBiomassId()));
		
		for(String id : macro){
			for(double v : vars){
				Container newCont = cont.clone();
				ReactionCI newbio = coef.changeBiomass(cont.getReaction(cont.getBiomassId()), id, v);
				newCont.getReactions().put(newbio.getId(), newbio);
				SteadyStateSimulationResult result = simulateContainer(newCont);
				List<String> result3 = cGenes(newCont);
				System.out.println(id + "\t" + v + "\t"+ cont.getBiomassId() + "\t" + result.getFluxValues().get(cont.getBiomassId())+ "\t" + result3);
				addToMap(values, result,id+"["+v+"]", result3);
				addToMapBiomass(biomass, newbio, id+"["+v+"]");
				
				newbio = coef.changeBiomass(cont.getReaction(cont.getBiomassId()), id, -v);
				newCont.getReactions().put(newbio.getId(), newbio);
				result = simulateContainer(newCont);
				
				System.out.println(id + "\t" + -v + "\t"+cont.getBiomassId() + "\t" + result.getFluxValues().get(cont.getBiomassId()) + "\t" + result3);
				addToMap(values, result,id+"["+(-v)+"]", result3);
				addToMapBiomass(biomass, newbio, id+"["+(-v)+"]");
			}
			
		}
		
		for(String id : single){
			for(double v : vars){
				Container newCont = cont.clone();
				ReactionCI newbio = coef.changeBiomass(cont.getReaction(cont.getBiomassId()), id, v);
				newCont.getReactions().put(newbio.getId(), newbio);
				SteadyStateSimulationResult result = simulateContainer(newCont);
				List<String> result3 = cGenes(newCont);

				System.out.println(id + "\t" + v + "\t"+ cont.getBiomassId() + "\t" + result.getFluxValues().get(cont.getBiomassId()) +"\t" + result3);
				addToMap(values, result,id+"["+v+"]", result3);
				addToMapBiomass(biomass, newbio, id+"["+v+"]");
				
				newbio = coef.changeBiomass(cont.getReaction(cont.getBiomassId()), id, -v);
				newCont.getReactions().put(newbio.getId(), newbio);
				result = simulateContainer(newCont);
				result3 = cGenes(newCont);

				
				System.out.println(id + "\t" + -v + "\t"+cont.getBiomassId() + "\t" + result.getFluxValues().get(cont.getBiomassId()) + "\t" + result3);
				addToMap(values, result, id+"["+(-v)+"]", result3);
				addToMapBiomass(biomass, newbio, id+"["+(-v)+"]");
			}
			
		}

		FileUtils.saveStringInFile("info.csv", MapUtils.prettyMAP2ToString(values, cont.getReactions().keySet()));
		FileUtils.saveStringInFile("biomass.csv", MapUtils.prettyMAP2ToString(biomass,cont.getReaction(cont.getBiomassId()).getReactants().keySet()));
	}
	
	
	
	public static SteadyStateSimulationResult simulateContainer(Container cont) throws Exception{
		ISteadyStateModel model = ContainerConverter.convert(cont);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setMaximization(true);
//		cc.setSolver);
		cc.setSolver(SolverType.CLP);
		SteadyStateSimulationResult result = cc.simulate();
		return result;
	}
	
	public static List<String> cGenes(Container cont) throws Exception{
		ISteadyStateModel model = ContainerConverter.convert(cont);
		CriticalGenes cG = new CriticalGenes (model, null, SolverType.CLP);
		List<String> result3 = cG.getCriticalGenesIds();
		return result3;
	}
	
	public static void addToMap(Map<String, Map<String, Double>> values, SteadyStateSimulationResult res, String id, List<String> result3){
		
			values.put(id, res.getFluxValues());
	}
	
	public static void addToMapBiomass(Map<String, Map<String, Double>> values, ReactionCI r, String id){
		
		Map<String, Double> coef = new HashMap<String, Double>();
		for(StoichiometryValueCI s : r.getReactants().values()){
			coef.put(s.getMetaboliteId(), s.getStoichiometryValue());
		}
		values.put(id, coef);
	}
}
