/*********************************************
 * OPL 12.5.1.0 Model
 * Author: Celso
 * Creation Date: 01/04/2015 at 11:21:08
 *********************************************/

int TimeLimit = ...; // Time limit for the model
float precision = ...; //Precision required 1/(C+1)

int NT = ...; //Number of Tasks
int NS = ...; //Number of workStations
int NW = ...; //Number of Workers
int NM = ...; //Number of Models

range Tasks    = 1..NT; //Range for Tasks
range Stations = 1..NS; //Range for workStations
range Workers  = 1..NW; //Range for Workers
range Models   = 1..NM; //Range for Models


//Definition of the tuples

//Tuple used to define the duration of tasks
tuple DTmType{
  int models;
  int tasks;
  float time;
}

// Tuple used to define precedence relations between Tasks
tuple task_task{
  int Tp; //Task that precedes
  int Ts; //Task that succeds
}

// Tuple used to define moving time between workStations
tuple station_station_time{
	int Sp;        // Station that precedes
	int Ss;        // Station that succeeds
	float MovTime; // Moving time
}

// Tuple used to define task allocations
tuple task_station{
	int T; // Task
	int S; // workStation
}


// Tuple used to define task-worker allocations
tuple task_worker{
	int T; // Task
	int W; // Worker
}

// Tuple used to define worker-station allocations
tuple worker_station{
	int W; // Worker
	int S; // workStation
}

// Auxiliary tuple that involves Tasks, Workers, and workStations
tuple task_worker_station{
	int T; // Task
	int W; // Worker
	int S; // workStation
}

// Auxiliary tuple that involves Workers and workStations
tuple worker_station_station{
	int W;  // Worker
	int Sp; // workStation	
	int Ss; // workStation
}


//User defined properties - Entries

{DTmType} DTm = ...;               //Duration of Tasks for each Model and Station
float OR[i in Models] = ...;       //Ocupation Rate of the line by a model

sorted {task_task} TTIne = ...;   //TTIncompatible Tasks *********
sorted {task_worker} TWIne = ...;   //Tasks that cannot be performed by a worker *********

sorted {task_task} PREC = ...;     //Precedence between tasks

sorted {station_station_time} MT = ...; // Tuple used to define moving time between workStations

sorted {worker_station} WSFIXe  = ...; //Fixed position for workers
sorted {worker_station} WSFEASe = ...; //Posible positions for workers

sorted {task_station} TSFIXe  = ...; //Tasks fixed to a station
sorted {task_station} FATSe = ...;   //Task limited to stations


//Removing Null Elements
{worker_station} NullWS = {<w,s> | w in {0}, s in {0}};
{task_station} NullTS = {<t,s> | t in {0}, s in {0}};
{task_task} NullTT = {<t1,t2> | t1 in {0}, t2 in {0}};
{task_worker} NullTW = {<t,w> | t in {0}, w in {0}};

sorted {task_task} TTIn = TTIne diff NullTT;   //TTIncompatible Tasks *********
sorted {task_worker} TWIn = TWIne diff NullTW;   //Tasks that cannot be performed by a worker *********
sorted {worker_station} WSFIX = WSFIXe diff NullWS; //Restricted Positions for workers
sorted {worker_station} WSFEAS = WSFEASe diff NullWS; //Restricted Positions for workers

sorted {task_station} TSFIX  = TSFIXe diff NullTS; //Tasks fixed to a station
sorted {task_station} FATS = FATSe diff NullTS;   //Task limited to stations

//Problem domain sets determination
sorted {int} WSFIXw = {w | <w,s> in WSFIX};
sorted {int} WSFIXs = {s | <w,s> in WSFIX};
sorted {int} WSFEASw = {w | <w,s> in WSFEAS};
sorted {int} exWSFIXw = {w | w in Workers} diff WSFIXw;
sorted {int} exWSFIXs = {s | s in Stations} diff WSFIXs;
sorted {int} exWSFEASw = {w | w in Workers} diff WSFEASw;

sorted {worker_station} WSFree = {<w,s>| w in (exWSFIXw inter exWSFEASw), s in exWSFIXs};

sorted {worker_station} W_Si = WSFree union WSFIX union WSFEAS;

sorted {int} TSFIXt = {t | <t,s> in TSFIX};
sorted {int} FATSt = {t | <t,s> in FATS};

sorted {int} exTSFIXt = {t | t in Tasks} diff TSFIXt;
sorted {int} exFATSt = {t | t in Tasks} diff FATSt;

sorted {task_station} TSFree = {<t,s> | t in (exTSFIXt inter exFATSt), s in Stations};

sorted {task_station} T_Si = TSFree union TSFIX union FATS;

sorted {task_worker} T_Wi = {<t, w>| t in Tasks, w in Workers} diff TWIn;

sorted {task_worker_station} T_W_S = {<t,w,s> | <t,w> in T_Wi, <t,s> in T_Si, <w,s> in W_Si};

sorted {task_worker} T_W = {<t,w> | <t,w,s> in T_W_S};
sorted {task_station} T_S = {<t,s> | <t,w,s> in T_W_S};
sorted {worker_station} W_S = {<w,s> | <t,w,s> in T_W_S};

//Set for the worker movements between stations
sorted {worker_station_station} W_S_S = {<w,sp,ss> | 
	w in (exWSFIXw inter exWSFEASw), sp in exWSFIXs, ss in exWSFIXs, <w,sp> in W_S: sp<ss}
	union {<w,sp,ss> | <w,sp> in WSFEAS, <w,ss> in WSFEAS: sp<ss};

// Initial Execution time (to capture the elapsed time in post-processing)
float InitialTime;
execute load_ExecTime{
	var StartTime = new Date();
	InitialTime=StartTime;
};

// Some settings for the CPLEX, in case of not using .ops file added to the conf. run
execute Model_Parameters {
  cplex.MIPEmphasis = 2; //1
  cplex.tilim = TimeLimit*2.5; // Set this value (in seconds) for limiting the execution time
  cplex.epgap = precision/1000; // relative gap of 0.1%
}

// Variables

dvar float+ CT;
dvar boolean TWS[T_W_S]; 
dvar boolean TS[T_S];
dvar boolean WS[W_S];
dvar boolean TW[T_W];
dvar boolean WSS[W_S_S];
dvar float+ WTime[Workers];
dvar float+ STime[Stations];


// Objective Function

minimize
 CT;
  
subject to {

// C1) Each Task has to be allocated in a workStation
forall(t in Tasks)
	sum(<t,s> in T_S) TS[<t,s>] == 1;

// C2) Precedence relations between Tasks that are to be allocated
// Notice that the precedence in relation to fixed Tasks have also to be considered
forall(<tp,ts> in PREC)
	sum(<tp,s> in T_S) s*TS[<tp,s>] <= sum(<ts,s> in T_S) s*TS[<ts,s>];

// C3) Cicle Time (CT) determination - related to a Station
forall(s in Stations)
	STime[s] <= CT;
	
// C5) Ocupation of a Station.
forall(s in Stations)
 	 STime[s] == sum(<t,s> in (T_S), <m,t,tp> in DTm) OR[m]*tp*TS[<t,s>];

// C6) A Task is made by one Worker - except when totally automated.
forall(t in Workers)
	sum(<t,s> in (T_S)) TS[<t,s>] == sum(<t,w> in (T_W)) TW[<t,w>];

// C7-C9) Allocation of a Worker in a Station  
//# C = A^B, where,  A --> TW[t,w], B-->TS[t,s], C--> TWS[t,w,s]
//# C <=A; C <=B; C >=A+B-1;
forall(<t,w,s> in T_W_S) TWS[<t,w,s>] <= TW[<t,w>];
forall(<t,w,s> in T_W_S) TWS[<t,w,s>] <= TS[<t,s>];
forall(<t,w,s> in T_W_S) TWS[<t,w,s>] >= TW[<t,w>]+TS[<t,s>]-1;	

// C10-C11) Allocation of a Worker in a Station (complementary constraints)
forall(<t,w> in T_W) sum(<t,w,s> in T_W_S)TWS[<t,w,s>] >= TW[<t,w>];
forall(<t,s> in T_S) sum(<t,w,s> in T_W_S)TWS[<t,w,s>] >= TS[<t,s>];

// ##C12) Allocation of a worker into a Station
forall(<w,s> in W_S)
	WS[<w,s>] <= sum(<t,w,s> in T_W_S) TWS[<t,w,s>];
forall(<w,s> in W_S)
	WS[<w,s>]*NT >= sum(<t,w,s> in T_W_S) TWS[<t,w,s>];

// C13) Only one Worker can be assigned to a Station
forall(s in Stations)
	sum(<w,s> in W_S) WS[<w,s>] <= 1;
	
// C14-C16) Assignment constraints ***** 1a linha
forall(<w,s,ss> in W_S_S, <w,s> in W_S) WSS[<w,s,ss>] <= WS[<w,s>];
forall(<w,s,ss> in W_S_S, <w,ss> in W_S) WSS[<w,s,ss>] <= WS[<w,ss>];
forall(<w,s,ss> in W_S_S, <w,s> in W_S, <w,ss> in W_S) WSS[<w,s,ss>] >= WS[<w,s>]+WS[<w,ss>]-1;

// C17) Ocupation of a Worker.
forall(w in Workers)
 	 WTime[w] == sum(<t,w,s> in (T_W_S), <m,t,tp> in DTm) OR[m]*tp*TWS[<t,w,s>] +
				sum(<w,sp,ss> in (W_S_S), <sp,ss,movtime> in MT) WSS[<w,sp,ss>]*movtime*2;
				
// C18) Cicle Time can be determined by the 'bottleneck Worker' 
forall(w in Workers)
	WTime[w] <= CT;

// C21-22) The cicle time for each model cannot be longer than k*CT
forall(s in Stations, m in Models)
  sum(<t,s> in T_S, <m,t,tp> in DTm) tp*TS[<t,s>] <= 1.3*CT;
  
forall(w in Workers, m in Models)
 	 			 sum(<t,w,s> in (T_W_S), <m,t,tp> in DTm) tp*TWS[<t,w,s>] +
				sum(<w,sp,ss> in (W_S_S), <sp,ss,movtime> in MT) WSS[<w,sp,ss>]*movtime*2
				<= 1.3*CT;
				
// C23) Incompatible tasks can't be assigned to the same station *******			
forall(s in Stations, <t1, t2> in TTIn)
  sum(<t1, s> in T_S) TS[<t1,s>] + sum(<t2, s> in T_S) TS[<t2,s>]<=1;
}

// Elapsed Time (in seconds)
float ElapsedTime=0;
execute{
var EndTime = new Date();
	ElapsedTime = (EndTime - InitialTime)/1000;
};