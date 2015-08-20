package com.cplex.util;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.cplex.entity.ResultDetailsEntity;
import com.cplex.entity.ResultEntity;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

public class CplexModReader {

	public static IloOplFactory oplF = new IloOplFactory();
	public static String cenario = "cplex_dat/MUKHERJE_000_03.dat";
	private IloOplModel oplMilp;
	private EntityManagerFactory entityManagerFactory;
	private EntityManager em;

	public CplexModReader() {

		entityManagerFactory = Persistence.createEntityManagerFactory("estudo_cplex");

//		em = entityManagerFactory.createEntityManager();

	}

	public void execute() {
		File folder = new File("cplex_dat/");
		File[] listOfFiles = folder.listFiles();
		double initialRunningTime = System.currentTimeMillis();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(".dat")) {
				runScenario(file.getAbsolutePath());
				/* do somthing with content */
			}
		}
		System.out.println(System.currentTimeMillis() - initialRunningTime);
		System.out.println("The End");
	}

	public void runScenario(String scenario) {
		try {			

			oplF = new IloOplFactory();
			IloOplSettings settings = criaSettings();
			IloOplDataSource dataSource = oplF.createOplDataSource(scenario);
			oplMilp = criaModeloMILP(settings);
			oplMilp.addDataSource(dataSource);
			oplMilp.generate();
			
			IloCplex cplex = oplMilp.getCplex();
			
			double init = System.currentTimeMillis();			
			cplex.setParam(IloCplex.DoubleParam.TiLim, Parameter.MAXIMUM_TIME_RUNNING);
			
			if (cplex.solve()) {
				System.out.println("OBJ = " + cplex.getObjValue());
			}
			double end = System.currentTimeMillis();

			ResultEntity resultEntity = new ResultEntity();
			resultEntity.setDatName(scenario.replace("C:\\Users\\Makin\\workspace\\CplexSample\\cplex_dat", ""));
			resultEntity.setModName("/Modelo_SBPO.mod");
			resultEntity.setMaximumTimeRunning(Parameter.MAXIMUM_TIME_RUNNING);
			resultEntity.setObjectiveValue(cplex.getObjValue());
			resultEntity.setStatus(cplex.getStatus().toString());
			resultEntity.setTimeElapsedRunning((end - init)/1000);
			resultEntity.setVersion(cplex.getVersion());
			
			ResultDetailsEntity resultDetailsEntity = new ResultDetailsEntity();
			resultDetailsEntity.setBestObjectiveValue(cplex.getBestObjValue());
			resultDetailsEntity.setGapPercentage((cplex.getObjValue()-cplex.getBestObjValue())/cplex.getBestObjValue()*100);
			resultDetailsEntity.setNumberOfIterations(cplex.getNiterations());
			resultDetailsEntity.setNumberOfTotalVariables(cplex.getNcols());
			resultDetailsEntity.setNumberOfBinaryVariables(cplex.getNNZs());
			resultDetailsEntity.setNumberOfConstraints(cplex.getNrows());

			resultEntity.setResultDetailsEntity(resultDetailsEntity);
			
			 
			cplex.writeSolution("cplex_solutions/batata/teste.sol");
			em = entityManagerFactory.createEntityManager();
			em.getTransaction().begin();
			em.persist(resultEntity);
			em.getTransaction().commit();
//			em.flush();
			em.clear();
			em.close();
			
			
			cplex.clearCallbacks();
			cplex.clearCuts();
			cplex.clearLazyConstraints();
			cplex.clearModel();
			cplex.clearUserCuts();
			
			cplex.end();
			
			Thread.sleep(2000);

			oplF = null;
			oplMilp = null;
			settings = null;
			dataSource = null;
			// entityManagerFactory = null;
			 em = null;
			System.gc();
			

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static IloOplModel criaModeloMILP(IloOplSettings settings) throws IloException {
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource milpSource = oplF.createOplModelSource("cplex_mod/Modelo_SBPO.mod");
		IloOplModelDefinition milpDef = oplF.createOplModelDefinition(milpSource, settings);
		return oplF.createOplModel(milpDef, cplex);
	}

	public static IloOplSettings criaSettings() {

		IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
		return oplF.createOplSettings(errHandler);
	}
}
