package com.cplex.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "result_details")
public class ResultDetailsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "best_objective_value")
	private double bestObjectiveValue;
	@Column(name = "gap_percentage")
	private double gapPercentage;
	@Column(name = "iterations")
	private int numberOfIterations;
	@Column(name = "total_variables")
	private int numberOfTotalVariables;
	@Column(name = "binary_variables")
	private int numberOfBinaryVariables;
	@Column(name = "contraints")
	private int numberOfConstraints;

	// writeln("Best O.F.: ", cplex.getBestObjValue());
	// writeln("GAP (%): ",
	// ((cplex.getObjValue()-cplex.getBestObjValue())/cplex.getBestObjValue())*100);
	// writeln("No Iteration: ", cplex.getNiterations());
	// writeln("No Tot.Variables: ", cplex.getNcols());
	//// writeln("No Bin.Variables: ", cplex.getNbinVars() );
	// writeln("No Bin.Variables: ", cplex.getNNZs() );
	// writeln("No Constraints: ", cplex.getNrows());
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public double getBestObjectiveValue() {
		return bestObjectiveValue;
	}

	public void setBestObjectiveValue(double bestObjectiveValue) {
		this.bestObjectiveValue = bestObjectiveValue;
	}

	public double getGapPercentage() {
		return gapPercentage;
	}

	public void setGapPercentage(double gapPercentage) {
		this.gapPercentage = gapPercentage;
	}

	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}

	public int getNumberOfTotalVariables() {
		return numberOfTotalVariables;
	}

	public void setNumberOfTotalVariables(int numberOfTotalVariables) {
		this.numberOfTotalVariables = numberOfTotalVariables;
	}

	public int getNumberOfBinaryVariables() {
		return numberOfBinaryVariables;
	}

	public void setNumberOfBinaryVariables(int numberOfBinaryVariables) {
		this.numberOfBinaryVariables = numberOfBinaryVariables;
	}

	public int getNumberOfConstraints() {
		return numberOfConstraints;
	}

	public void setNumberOfConstraints(int numberOfConstraints) {
		this.numberOfConstraints = numberOfConstraints;
	}

}
