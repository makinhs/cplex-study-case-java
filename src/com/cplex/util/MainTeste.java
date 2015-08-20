package com.cplex.util;

public class MainTeste {

	public static void main(String[] args) {
		
		CplexModReader cplexModReader = new CplexModReader();
		Parameter.MAXIMUM_TIME_RUNNING = 60*5;
		cplexModReader.execute();

	}

}
