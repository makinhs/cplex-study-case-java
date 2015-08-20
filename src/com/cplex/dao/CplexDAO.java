package com.cplex.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.cplex.entity.ResultEntity;
import com.cplex.util.Parameter;

/**
 * Classe responsável em salvar os resultados no banco de dados.
 * @author Makin
 *
 */
public class CplexDAO {
	private EntityManager em;
	private EntityManagerFactory entityManagerFactory;
	
	public CplexDAO() {
		entityManagerFactory = Persistence.createEntityManagerFactory(Parameter.PERSISTENCE_UNIT_NAME);
		em = entityManagerFactory.createEntityManager();
	}
	
	
	/**
	 * Salva os dados contidos no ResultEntity para o banco de dados que está configurado no persistence.xml.
	 * @param resultEntity
	 */
	public void saveResultEntity(ResultEntity resultEntity){
		em.getTransaction().begin();
		em.persist(resultEntity);
		em.getTransaction().commit();
//		em.clear();
		
	}
	
}
