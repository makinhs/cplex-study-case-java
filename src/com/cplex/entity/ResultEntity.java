package com.cplex.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entidade mapeada para salvar o resultado de um cenário rodado em um devido modelo.
 * @author Makin
 *
 */
@Entity
@Table(name = "result")
public class ResultEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@OneToOne(cascade = CascadeType.ALL)
	private ResultDetailsEntity result_details;

	@Column(name = "dat_file_name")
	private String datFileName;

	@Column(name = "mod_file_name")
	private String modFileName;

	@Column(name = "objective_value")
	private double objectiveValue;
	private String status;
	private String version;

	@Column(name = "maximum_time_running")
	private double maximumTimeRunning;
	@Column(name = "elapsed_time")
	private double timeElapsedRunning;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDatName() {
		return datFileName;
	}

	public void setDatName(String datName) {
		this.datFileName = datName;
	}

	public String getModName() {
		return modFileName;
	}

	public void setModName(String modName) {
		this.modFileName = modName;
	}

	public double getObjectiveValue() {
		return objectiveValue;
	}

	public void setObjectiveValue(double objectiveValue) {
		this.objectiveValue = objectiveValue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public double getMaximumTimeRunning() {
		return maximumTimeRunning;
	}

	public void setMaximumTimeRunning(double maximumTimeRunning) {
		this.maximumTimeRunning = maximumTimeRunning;
	}

	public double getTimeElapsedRunning() {
		return timeElapsedRunning;
	}

	public void setTimeElapsedRunning(double timeElapsedRunning) {
		this.timeElapsedRunning = timeElapsedRunning;
	}

	public ResultDetailsEntity getResultDetailsEntity() {
		return result_details;
	}

	public void setResultDetailsEntity(ResultDetailsEntity resultDetailsEntity) {
		this.result_details = resultDetailsEntity;
	}

}
