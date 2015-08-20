package com.cplex.main;

import com.cplex.manager.CplexManager;
import com.cplex.util.Parameter;

public class MainTest {

	public static void main(String[] args) {
		
		CplexManager cplexModReader = new CplexManager();
		
		/**
		 * Maximum time running in seconds.
		 */
		Parameter.MAXIMUM_TIME_RUNNING = 60*5;
		
		//executa apenas um modelo para vários dats.
//		cplexModReader.executeDatWithModel("cplex_dat/", "cplex_mod/Modelo_SBPO.mod");
		
		
		//executa vários modelos para vários dats.
		cplexModReader.executeDatWithAllModels("cplex_dat/", "cplex_mod/");
		

	}

}
