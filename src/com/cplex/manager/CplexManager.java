package com.cplex.manager;

import java.io.File;
import java.util.regex.Pattern;

import com.cplex.dao.CplexDAO;
import com.cplex.entity.ResultDetailsEntity;
import com.cplex.entity.ResultEntity;
import com.cplex.util.Parameter;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;


/**
 * Classe responsável em ler os arquivos dats, executar os modelos e salvar os dados no banco de dados MySQL.
 * @author Makin
 */
public class CplexManager {
	
	public static IloOplFactory oplF = new IloOplFactory();
	private IloOplModel oplMilp;
	private CplexDAO cplexDAO;
	

	public CplexManager() {
		cplexDAO = new CplexDAO();
	}

	/**
	 * Class that runs all dat's in the current spec project folder by one
	 * model.
	 * 
	 * @param datFolderName
	 *            ex: "cplex_dat".
	 * @param modelFileName
	 *            ex: "/Modelo_SBPO.mod"
	 */
	public void executeDatWithModel(String datFolderName, String modelFileName) {
		File folder = new File(datFolderName);
		File[] listOfFiles = folder.listFiles();
		double initialRunningTime = System.currentTimeMillis();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(Parameter.DAT)) {
				runScenarioWithModel(file.getAbsolutePath(), modelFileName);
			}
		}
		System.out.println(System.currentTimeMillis() - initialRunningTime);
		System.out.println("The End");
	}
	
	
	/**
	 * Método que executa vários dats de vários modelos contidos nas devidas pastadas que são passadas pelos parâmetros
	 * método.
	 * @param datFolderName Nome da pasta dentro do projeto que está os arquivos dats (.dat).
	 * @param modFolderName Nome da pasta dentrod o projeto que está os arquivos de modelo (.mod).
	 */
	public void executeDatWithAllModels(String datFolderName, String modFolderName){
		File datFolder = new File(datFolderName);
		File[] listOfAllDatFiles = datFolder.listFiles();
		
		File modFolder = new File(modFolderName);
		File[] listOfAllModFiles = modFolder.listFiles();
		
		double initialRunningTime = System.currentTimeMillis();
		for (int i = 0; i < listOfAllDatFiles.length; i++) {
			File datFile = listOfAllDatFiles[i];
			if (datFile.isFile() && datFile.getName().endsWith(Parameter.DAT)) {
				
				for(int u=0; u < listOfAllModFiles.length; u++){
					File modFile = listOfAllModFiles[u];
					if (modFile.isFile() && modFile.getName().endsWith(Parameter.MOD)) {
						runScenarioWithModel(datFile.getAbsolutePath(), modFile.getAbsolutePath());
					}
				}
			}
		}
		System.out.println(System.currentTimeMillis() - initialRunningTime);
		System.out.println("The End");
	}

	/**
	 * Método que executa um cenário (.dat) com um modelo (.mod) e salva no banco de dados.
	 * @param scenario path absoluto do cenário (.dat)
	 * @param modelFile path absoluto do modelo (.mod)
	 */
	private void runScenarioWithModel(String scenario, String modelFile) {
		try {
			IloCplex cplex = configureScenario(scenario, modelFile);
			runScenario(scenario, modelFile, cplex);
			clearMemory(cplex);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 * Método que configura os dados necessários para gerar uma solução de dado cenário e modelo.
	 * @param scenario
	 * @param modelFile
	 * @return
	 * @throws IloException
	 */
	private IloCplex configureScenario(String scenario, String modelFile) throws IloException {
		oplF = new IloOplFactory();
		IloOplSettings settings = generateSettings();
		IloOplDataSource dataSource = oplF.createOplDataSource(scenario);
		oplMilp = criaModeloMILP(settings, modelFile);
		oplMilp.addDataSource(dataSource);
		oplMilp.generate();
		IloCplex cplex = oplMilp.getCplex();
		cplex.setParam(IloCplex.DoubleParam.TiLim, Parameter.MAXIMUM_TIME_RUNNING);
		return cplex;
	}

	
	/**
	 * Método que executa um dado cenário pré-configurado.
	 * @param scenario
	 * @param modelFile
	 * @param cplex
	 * @throws IloException
	 */
	private void runScenario(String scenario, String modelFile, IloCplex cplex) throws IloException {
		double init = System.currentTimeMillis();
		if (cplex.solve()) {
			double end = System.currentTimeMillis();
			
			String[] modelSplitName = modelFile.split(Pattern.quote(File.separator));
			String modelFileName = modelSplitName[modelSplitName.length-1];
			
			String[] datSplitName = scenario.split(Pattern.quote(File.separator));
			String datFileName = datSplitName[datSplitName.length-1];
			
			ResultEntity resultEntity = new ResultEntity();
			resultEntity.setDatName(datFileName);			
			resultEntity.setModName(modelFileName);
			resultEntity.setMaximumTimeRunning(Parameter.MAXIMUM_TIME_RUNNING);
			resultEntity.setObjectiveValue(cplex.getObjValue());
			resultEntity.setStatus(cplex.getStatus().toString());
			resultEntity.setTimeElapsedRunning((end - init) / 1000);
			resultEntity.setVersion(cplex.getVersion());

			ResultDetailsEntity resultDetailsEntity = new ResultDetailsEntity();
			resultDetailsEntity.setBestObjectiveValue(cplex.getBestObjValue());
			resultDetailsEntity.setGapPercentage((cplex.getObjValue() - cplex.getBestObjValue()) / cplex.getBestObjValue() * 100);
			resultDetailsEntity.setNumberOfIterations(cplex.getNiterations());
			resultDetailsEntity.setNumberOfTotalVariables(cplex.getNcols());
			resultDetailsEntity.setNumberOfBinaryVariables(cplex.getNNZs());
			resultDetailsEntity.setNumberOfConstraints(cplex.getNrows());

			resultEntity.setResultDetailsEntity(resultDetailsEntity);
			cplexDAO.saveResultEntity(resultEntity);				
		}
	}
	
	
	/**
	 * Método criado apenas para forçar a limpeza de memória ram do computador. Sem essas chamadas é possível que
	 * o computador congele por falha na memória RAM.
	 * @param cplex
	 * @throws IloException
	 */
	private void clearMemory(IloCplex cplex) throws IloException {
		try {				
			cplex.clearCallbacks();
			cplex.clearCuts();
			cplex.clearLazyConstraints();
			cplex.clearModel();
			cplex.clearUserCuts();
			cplex.end();
			clearObjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método temporário para auxiliar a limpeza de memória.
	 * É um overkill, não precisa de forma alguma ;)
	 * @throws InterruptedException
	 */
	private void clearObjects() throws InterruptedException {
		oplF = null;
		oplMilp = null;		
		Thread.sleep(2000);
		System.gc();
	}

	private static IloOplModel criaModeloMILP(IloOplSettings settings, String modFilePath) throws IloException {
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource milpSource = oplF.createOplModelSource(modFilePath);		
		IloOplModelDefinition milpDef = oplF.createOplModelDefinition(milpSource, settings);
		return oplF.createOplModel(milpDef, cplex);
	}

	private static IloOplSettings generateSettings() {
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
		return oplF.createOplSettings(errHandler);
	}
}
