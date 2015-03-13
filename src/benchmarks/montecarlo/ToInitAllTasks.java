/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         *
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package benchmarks.montecarlo;
/**
  * Class for defining the initialisation data for all tasks.
  *
  * @author H W Yau
  * @version $Revision: 1.10 $ $Date: 1999/02/16 18:52:53 $
  */
public class ToInitAllTasks implements java.io.Serializable {
  private String header;
  private String name;
  private int startDate;
  private int endDate;
  private double dTime;
  private int returnDefinition;
  private double expectedReturnRate;
  private double volatility;
  private int nTimeSteps;
  private double pathStartValue;

  /**
    * Constructor, for initialisation data which are common to all
    * computation tasks.
    *
    * @param header Simple header string.
    * @param name The name of the security which this Monte Carlo path
    *             should represent.
    * @param startDate The date when the path starts, in 'YYYYMMDD' format.
    * @param endDate The date when the path ends, in 'YYYYMMDD' format.
    * @param dTime The interval in the data between successive data points
    *              in the generated path.
    * @param returnDefinition How the statistic variables were defined,
    *                         according to the definitions in
    *                         <code>ReturnPath</code>'s two class variables
    *                         <code>COMPOUNDED</code> and
    *                         <code>NONCOMPOUNDED</code>.
    * @param expectedReturnRate The measured expected return rate for which
    *       to generate.
    * @param volatility The measured volatility for which to generate.
    * @param nTimeSteps The number of time steps for which to generate.
    * @param pathStartValue The stock price value to use at the start of each
    *        Monte Carlo simulation path.
    */
  public ToInitAllTasks(String header, String name, int startDate, int endDate, 
  double dTime, int returnDefinition, double expectedReturnRate, double volatility, 
  double pathStartValue) {
    this.header             = header;
    this.name               = name;
    this.startDate          = startDate;
    this.endDate            = endDate;
    this.dTime              = dTime;
    this.returnDefinition   = returnDefinition;
    this.expectedReturnRate = expectedReturnRate;
    this.volatility         = volatility;
    this.nTimeSteps         = nTimeSteps;
    this.pathStartValue     = pathStartValue;
  }
  /**
    * Another constructor, slightly easier to use by having slightly
    * fewer arguments.  Makes use of the "ReturnPath" object to
    * accomplish this.
    *
    * @param obj Object used to define the instance variables which
    *            should be carried over to this object.
    * @param nTimeSteps The number of time steps which the Monte
    *                   Carlo generator should make.
    * @param pathStartValue The stock price value to use at the start of each
    *        Monte Carlo simulation path.
    * @exception DemoException thrown if there is a problem accessing the
    *                          instance variables from the target objetct.
    */
  public ToInitAllTasks(ReturnPath obj, int nTimeSteps, double pathStartValue) 
  throws DemoException {
    //
    // Instance variables defined in the PathId object.
    this.name      = obj.get_name();
    this.startDate = obj.get_startDate();
    this.endDate   = obj.get_endDate();
    this.dTime     = obj.get_dTime();
    //
    // Instance variables defined in ReturnPath object.
    this.returnDefinition   = obj.get_returnDefinition();
    this.expectedReturnRate = obj.get_expectedReturnRate();
    this.volatility         = obj.get_volatility();
    this.nTimeSteps         = nTimeSteps;
    this.pathStartValue     = pathStartValue;
  }
  //------------------------------------------------------------------------
  // Accessor methods for class ToInitAllTasks.
  // Generated by 'makeJavaAccessor.pl' script.  HWY.  20th January 1999.
  //------------------------------------------------------------------------
  /**
    * Accessor method for private instance variable <code>header</code>.
    *
    * @return Value of instance variable <code>header</code>.
    */
  public String get_header() {
    return(this.header);
  }
  /**
    * Set method for private instance variable <code>header</code>.
    *
    * @param header the value to set for the instance variable <code>header</code>.
    */
  public void set_header(String header) {
    this.header = header;
  }
  /**
    * Accessor method for private instance variable <code>name</code>.
    *
    * @return Value of instance variable <code>name</code>.
    */
  public String get_name() {
    return(this.name);
  }
  /**
    * Set method for private instance variable <code>name</code>.
    *
    * @param name the value to set for the instance variable <code>name</code>.
    */
  public void set_name(String name) {
    this.name = name;
  }
  /**
    * Accessor method for private instance variable <code>startDate</code>.
    *
    * @return Value of instance variable <code>startDate</code>.
    */
  public int get_startDate() {
    return(this.startDate);
  }
  /**
    * Set method for private instance variable <code>startDate</code>.
    *
    * @param startDate the value to set for the instance variable <code>startDate</code>.
    */
  public void set_startDate(int startDate) {
    this.startDate = startDate;
  }
  /**
    * Accessor method for private instance variable <code>endDate</code>.
    *
    * @return Value of instance variable <code>endDate</code>.
    */
  public int get_endDate() {
    return(this.endDate);
  }
  /**
    * Set method for private instance variable <code>endDate</code>.
    *
    * @param endDate the value to set for the instance variable <code>endDate</code>.
    */
  public void set_endDate(int endDate) {
    this.endDate = endDate;
  }
  /**
    * Accessor method for private instance variable <code>dTime</code>.
    *
    * @return Value of instance variable <code>dTime</code>.
    */
  public double get_dTime() {
    return(this.dTime);
  }
  /**
    * Set method for private instance variable <code>dTime</code>.
    *
    * @param dTime the value to set for the instance variable <code>dTime</code>.
    */
  public void set_dTime(double dTime) {
    this.dTime = dTime;
  }
  /**
    * Accessor method for private instance variable <code>returnDefinition</code>.
    *
    * @return Value of instance variable <code>returnDefinition</code>.
    */
  public int get_returnDefinition() {
    return(this.returnDefinition);
  }
  /**
    * Set method for private instance variable <code>returnDefinition</code>.
    *
    * @param returnDefinition the value to set for the instance variable <code>returnDefinition</code>.
    */
  public void set_returnDefinition(int returnDefinition) {
    this.returnDefinition = returnDefinition;
  }
  /**
    * Accessor method for private instance variable <code>expectedReturnRate</code>.
    *
    * @return Value of instance variable <code>expectedReturnRate</code>.
    */
  public double get_expectedReturnRate() {
    return(this.expectedReturnRate);
  }
  /**
    * Set method for private instance variable <code>expectedReturnRate</code>.
    *
    * @param expectedReturnRate the value to set for the instance variable <code>expectedReturnRate</code>.
    */
  public void set_expectedReturnRate(double expectedReturnRate) {
    this.expectedReturnRate = expectedReturnRate;
  }
  /**
    * Accessor method for private instance variable <code>volatility</code>.
    *
    * @return Value of instance variable <code>volatility</code>.
    */
  public double get_volatility() {
    return(this.volatility);
  }
  /**
    * Set method for private instance variable <code>volatility</code>.
    *
    * @param volatility the value to set for the instance variable <code>volatility</code>.
    */
  public void set_volatility(double volatility) {
    this.volatility = volatility;
  }
  /**
    * Accessor method for private instance variable <code>nTimeSteps</code>.
    *
    * @return Value of instance variable <code>nTimeSteps</code>.
    */
  public int get_nTimeSteps() {
    return(this.nTimeSteps);
  }
  /**
    * Set method for private instance variable <code>nTimeSteps</code>.
    *
    * @param nTimeSteps the value to set for the instance variable <code>nTimeSteps</code>.
    */
  public void set_nTimeSteps(int nTimeSteps) {
    this.nTimeSteps = nTimeSteps;
  }
  /**
    * Accessor method for private instance variable <code>pathStartValue</code>.
    *
    * @return Value of instance variable <code>pathStartValue</code>.
    */
  public double get_pathStartValue() {
    return(this.pathStartValue);
  }
  /**
    * Set method for private instance variable <code>pathStartValue</code>.
    *
    * @param pathStartValue the value to set for the instance variable <code>pathStartValue</code>.
    */
  public void set_pathStartValue(double pathStartValue) {
    this.pathStartValue = pathStartValue;
  }
  //------------------------------------------------------------------------
}
