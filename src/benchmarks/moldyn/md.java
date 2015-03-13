/**************************************************************************
*                                                                         *
*             Java Grande Forum Benchmark Suite - Version 2.0             *
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
*                  Original version of this code by                       *
*                         Dieter Heermann                                 * 
*                       converted to Java by                              *
*                Lorna Smith  (l.smith@epcc.ed.ac.uk)                     *
*                   (see copyright notice below)                          *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/



package benchmarks.moldyn;

import benchmarks.jgfutil.*;

public class md {

  public static final int ITERS = 100;
  public static final double LENGTH = 50e-10;
  public static final double m = 4.0026;
  public static final double mu = 1.66056e-27;
  public static final double kb = 1.38066e-23;
  public static final double TSIM = 50;
  public static final double deltat = 5e-16;

  public static int PARTSIZE;

  public static double [] epot;
  public static double [] vir;
  public static double [] ek;

  int size,mm;
  int datasizes[] = {8,13};

  public static int interactions = 0;
  public static int [] interacts;
 
  public void initialise() {

  mm = datasizes[size];
  PARTSIZE = mm*mm*mm*4;

  }


  public void runiters(){

/* Create new arrays */

    epot = new double [JGFMolDynBench.nthreads];
    vir  = new double [JGFMolDynBench.nthreads];
    ek   = new double [JGFMolDynBench.nthreads];

    interacts = new int [JGFMolDynBench.nthreads];

    double sh_force [][] = new double[3][PARTSIZE];
    double sh_force2 [][][] = new double[3][JGFMolDynBench.nthreads][PARTSIZE];

/* spawn threads */

    Runnable thobjects[] = new Runnable [JGFMolDynBench.nthreads];
    Thread th[] = new Thread [JGFMolDynBench.nthreads];
    Barrier br= new TournamentBarrier(JGFMolDynBench.nthreads);

    for(int i=1;i<JGFMolDynBench.nthreads;i++) {
      thobjects[i] = new mdRunner(i,mm,sh_force,sh_force2,br);
      th[i] = new Thread(thobjects[i]);
      th[i].start();
    }

    thobjects[0] = new mdRunner(0,mm,sh_force,sh_force2,br);
    thobjects[0].run();

    for(int i=1;i<JGFMolDynBench.nthreads;i++) {
      try {
        th[i].join();
      }
      catch (InterruptedException e) {}
    }

  }


}


class mdRunner implements Runnable {

  double count = 0.0;

  int id,i,j,k,lg,mdsize,move,mm;

  double l,rcoff,rcoffs,side,sideh,hsq,hsq2,vel,velt;
  double a,r,sum,tscale,sc,ekin,ts,sp;
  double den = 0.83134;
  double tref = 0.722;
  double h = 0.064;
  double vaver,vaverh,rand;
  double etot,temp,pres,rp;
  double u1,u2,v1,v2,s, xx, yy, zz;
  double xvelocity, yvelocity, zvelocity;

  double [][] sh_force;
  double [][][] sh_force2;

  int ijk,npartm,iseed,tint;
  int irep = 10;
  int istop = 19;
  int iprint = 10;
  int movemx = 50;
  
  Barrier br;
  random randnum;

  particle one [] = null;

    public mdRunner(int id, int mm, double [][] sh_force, double [][][] sh_force2,Barrier br) {
     this.id=id;
     this.mm=mm;
     this.sh_force=sh_force;
     this.sh_force2=sh_force2;
     this.br=br;
    } 

    public void run() {

/* Parameter determination */

    mdsize = md.PARTSIZE;
    one = new particle [mdsize];
    l = md.LENGTH;

    side = Math.pow((mdsize/den),0.3333333);
    rcoff = mm/4.0;

    a = side/mm;
    sideh = side*0.5;
    hsq = h*h;
    hsq2 = hsq*0.5;
    npartm = mdsize - 1;
    rcoffs = rcoff * rcoff;
    tscale = 16.0 / (1.0 * mdsize - 1.0);
    vaver = 1.13 * Math.sqrt(tref / 24.0);
    vaverh = vaver * h;

/* Particle Generation */

    xvelocity = 0.0;
    yvelocity = 0.0;
    zvelocity = 0.0;

    ijk = 0;
    for (lg=0; lg<=1; lg++) {
     for (i=0; i<mm; i++) {
      for (j=0; j<mm; j++) {
       for (k=0; k<mm; k++) {
        one[ijk] = new particle((i*a+lg*a*0.5),(j*a+lg*a*0.5),(k*a),
        xvelocity,yvelocity,zvelocity,sh_force,sh_force2,id,this);
        ijk = ijk + 1;
       }
      }
     }
    }
    for (lg=1; lg<=2; lg++) {
     for (i=0; i<mm; i++) {
      for (j=0; j<mm; j++) {
       for (k=0; k<mm; k++) {
        one[ijk] = new particle((i*a+(2-lg)*a*0.5),(j*a+(lg-1)*a*0.5),
        (k*a+a*0.5),xvelocity,yvelocity,zvelocity,sh_force,sh_force2,id,this);
        ijk = ijk + 1;
       }
      }
     }
    }

    
/* Initialise velocities */

    iseed = 0;
    v1 = 0.0;
    v2 = 0.0;

    randnum = new random(iseed,v1,v2);

    for (i=0; i<mdsize; i+=2) {
     r  = randnum.seed();
     one[i].xvelocity = r*randnum.v1;
     one[i+1].xvelocity  = r*randnum.v2;
    }

    for (i=0; i<mdsize; i+=2) {
     r  = randnum.seed();
     one[i].yvelocity = r*randnum.v1;
     one[i+1].yvelocity  = r*randnum.v2;
    }

    for (i=0; i<mdsize; i+=2) {
     r  = randnum.seed();
     one[i].zvelocity = r*randnum.v1;
     one[i+1].zvelocity  = r*randnum.v2;
    }


/* velocity scaling */

    ekin = 0.0;
    sp = 0.0;

    for(i=0;i<mdsize;i++) {
     sp = sp + one[i].xvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
     one[i].xvelocity = one[i].xvelocity - sp;
     ekin = ekin + one[i].xvelocity*one[i].xvelocity;
    }

    sp = 0.0;
    for(i=0;i<mdsize;i++) {
     sp = sp + one[i].yvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
     one[i].yvelocity = one[i].yvelocity - sp;
     ekin = ekin + one[i].yvelocity*one[i].yvelocity;
    }


    sp = 0.0;
    for(i=0;i<mdsize;i++) {
     sp = sp + one[i].zvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
     one[i].zvelocity = one[i].zvelocity - sp;
     ekin = ekin + one[i].zvelocity*one[i].zvelocity;
    }

    ts = tscale * ekin;
    sc = h * Math.sqrt(tref/ts);


    for(i=0;i<mdsize;i++) {

    one[i].xvelocity = one[i].xvelocity * sc;     
    one[i].yvelocity = one[i].yvelocity * sc;     
    one[i].zvelocity = one[i].zvelocity * sc;     

    }


/* Synchronise threads and start timer before MD simulation */

   br.DoBarrier(id);
   if (id == 0) JGFInstrumentor.startTimer("Section3:MolDyn:Run");
   br.DoBarrier(id);


/* MD simulation */

   move = 0;
   for (move=0;move<movemx;move++) {

/* move the particles and update velocities */

    for (i=0;i<mdsize;i++) {
     one[i].domove(side,i);       
    }

/* Barrier */
   br.DoBarrier(id);

    if(id==0) {
     for(j=0;j<3;j++) {
      for (i=0;i<mdsize;i++) {
        sh_force[j][i] = 0.0;
      }
     }
    }

    md.epot[id] = 0.0;
    md.vir[id] = 0.0;
    md.interacts[id] = 0;

/* Barrier */
   br.DoBarrier(id);



/* compute forces */

    for (i=0+id;i<mdsize;i+=JGFMolDynBench.nthreads) {
        one[i].force(side,rcoff,mdsize,i,xx,yy,zz); 
    }

/* Barrier */
   br.DoBarrier(id);

/* update force arrays */

   if(id == 0) {
    for(int k=0;k<3;k++) {
     for(i=0;i<mdsize;i++) {
       for(j=0;j<JGFMolDynBench.nthreads;j++) {
        sh_force[k][i] += sh_force2[k][j][i];
       }
     }
    }
   }

   if(id == 0) {
    for(int k=0;k<3;k++) {
     for(i=0;i<mdsize;i++) {
       for(j=0;j<JGFMolDynBench.nthreads;j++) {
        sh_force2[k][j][i] = 0.0;
       }
     }
    }
   }

   if(id==0) {
     for(j=1;j<JGFMolDynBench.nthreads;j++) {
       md.epot[0] += md.epot[j];
       md.vir[0] += md.vir[j];
     }
     for(j=1;j<JGFMolDynBench.nthreads;j++) {       
      md.epot[j] = md.epot[0];
      md.vir[j] = md.vir[0];
     }
     for(j=0;j<JGFMolDynBench.nthreads;j++) {
      md.interactions += md.interacts[j]; 
     }
   }

/* Barrier */
   br.DoBarrier(id);

    if(id == 0) {
      for (j=0;j<3;j++) {
        for (i=0;i<mdsize;i++) {
          sh_force[j][i] = sh_force[j][i] * hsq2;
        }
      }
    }

    sum = 0.0;

/* Barrier */
   br.DoBarrier(id);

/*scale forces, update velocities */

    for (i=0;i<mdsize;i++) {
     sum = sum + one[i].mkekin(hsq2,i);  
    }

    ekin = sum/hsq;

    vel = 0.0;
    count = 0.0;

/* average velocity */

    for (i=0;i<mdsize;i++) {
     velt = one[i].velavg(vaverh,h);
     if(velt > vaverh) { count = count + 1.0; }
     vel = vel + velt;                    
    }

    vel = vel / h;

/* temperature scale if required */

    if((move < istop) && (((move+1) % irep) == 0)) {
     sc = Math.sqrt(tref / (tscale*ekin));
     for (i=0;i<mdsize;i++) {
      one[i].dscal(sc,1);
     }
     ekin = tref / tscale;
    }

/* sum to get full potential energy and virial */

    if(((move+1) % iprint) == 0) {
     md.ek[id] = 24.0*ekin;
     md.epot[id] = 4.0*md.epot[id];
     etot = md.ek[id] + md.epot[id];
     temp = tscale * ekin;
     pres = den * 16.0 * (ekin - md.vir[id]) / mdsize;
     vel = vel / mdsize; 
     rp = (count / mdsize) * 100.0;
    }

   br.DoBarrier(id);
   }


   br.DoBarrier(id);
   if (id == 0) JGFInstrumentor.stopTimer("Section3:MolDyn:Run");

 }

}




class particle {

  public double xcoord, ycoord, zcoord;
  public double xvelocity,yvelocity,zvelocity;
  int part_id;
  int id;
  double [][] sh_force;
  double [][][] sh_force2;
  mdRunner runner;

  public particle(double xcoord, double ycoord, double zcoord, double xvelocity,
                  double yvelocity,double zvelocity,double [][] sh_force, 
                  double [][][] sh_force2,int id,mdRunner runner) {

   this.xcoord = xcoord; 
   this.ycoord = ycoord; 
   this.zcoord = zcoord;
   this.xvelocity = xvelocity;
   this.yvelocity = yvelocity;
   this.zvelocity = zvelocity;
   this.sh_force = sh_force;
   this.sh_force2 = sh_force2;
   this.id=id;
   this.runner=runner;
  }

  public void domove(double side,int part_id) {

    xcoord = xcoord + xvelocity + sh_force[0][part_id];
    ycoord = ycoord + yvelocity + sh_force[1][part_id];
    zcoord = zcoord + zvelocity + sh_force[2][part_id];

    if(xcoord < 0) { xcoord = xcoord + side; } 
    if(xcoord > side) { xcoord = xcoord - side; }
    if(ycoord < 0) { ycoord = ycoord + side; }
    if(ycoord > side) { ycoord = ycoord - side; }
    if(zcoord < 0) { zcoord = zcoord + side; }
    if(zcoord > side) { zcoord = zcoord - side; }

    xvelocity = xvelocity + sh_force[0][part_id];
    yvelocity = yvelocity + sh_force[1][part_id];
    zvelocity = zvelocity + sh_force[2][part_id];

  }

  public void force(double side, double rcoff,int mdsize,int x, double xx, double yy, double zz) {

    double sideh;
    double rcoffs;

    double fxi,fyi,fzi;
    double rd,rrd,rrd2,rrd3,rrd4,rrd6,rrd7,r148;
    double forcex,forcey,forcez;

    sideh = 0.5*side; 
    rcoffs = rcoff*rcoff;

     fxi = 0.0;
     fyi = 0.0;
     fzi = 0.0;

      for (int i=x+1;i<mdsize;i++) {
        xx = this.xcoord - runner.one[i].xcoord;
        yy = this.ycoord - runner.one[i].ycoord;
        zz = this.zcoord - runner.one[i].zcoord;

        if(xx < (-sideh)) { xx = xx + side; }
        if(xx > (sideh))  { xx = xx - side; }
        if(yy < (-sideh)) { yy = yy + side; }
        if(yy > (sideh))  { yy = yy - side; }
        if(zz < (-sideh)) { zz = zz + side; }
        if(zz > (sideh))  { zz = zz - side; }


        rd = xx*xx + yy*yy + zz*zz;

        if(rd <= rcoffs) {
           rrd = 1.0/rd;
           rrd2 = rrd*rrd;
           rrd3 = rrd2*rrd;
           rrd4 = rrd2*rrd2;
           rrd6 = rrd2*rrd4;
           rrd7 = rrd6*rrd;
           md.epot[id] = md.epot[id] + (rrd6 - rrd3);
           r148 = rrd7 - 0.5*rrd4;
           md.vir[id] = md.vir[id] - rd*r148;
           forcex = xx * r148;
           fxi = fxi + forcex;

           sh_force2[0][id][i] = sh_force2[0][id][i] - forcex;

           forcey = yy * r148;
           fyi = fyi + forcey;

           sh_force2[1][id][i] = sh_force2[1][id][i] - forcey;

           forcez = zz * r148;
           fzi = fzi + forcez;

           sh_force2[2][id][i] = sh_force2[2][id][i] - forcez;

           md.interacts[id]++;
        }

     }

     sh_force2[0][id][x] = sh_force2[0][id][x] + fxi;
     sh_force2[1][id][x] = sh_force2[1][id][x] + fyi;
     sh_force2[2][id][x] = sh_force2[2][id][x] + fzi;

  }

  public double mkekin(double hsq2,int part_id) {

    double sumt = 0.0; 

    xvelocity = xvelocity + sh_force[0][part_id]; 
    yvelocity = yvelocity + sh_force[1][part_id]; 
    zvelocity = zvelocity + sh_force[2][part_id]; 

    sumt = (xvelocity*xvelocity)+(yvelocity*yvelocity)+(zvelocity*zvelocity);
    return sumt;
  }

  public double velavg(double vaverh,double h) {
 
    double velt;
    double sq;

    sq = Math.sqrt(xvelocity*xvelocity + yvelocity*yvelocity +
                 zvelocity*zvelocity);

    velt = sq;
    return velt;
  }

  public void dscal(double sc,int incx) {

    xvelocity = xvelocity * sc;
    yvelocity = yvelocity * sc;   
    zvelocity = zvelocity * sc;   



  }

}

class random {

  public int iseed;
  public double v1,v2;

  public random(int iseed,double v1,double v2) {
  this.iseed = iseed;
  this.v1 = v1;
  this.v2 = v2;
  }

  public double update() {

  double rand;
  double scale= 4.656612875e-10;

  int is1,is2,iss2;
  int imult=16807;
  int imod = 2147483647;

  if (iseed<=0) { iseed = 1; }

  is2 = iseed % 32768;
  is1 = (iseed-is2)/32768;
  iss2 = is2 * imult;
  is2 = iss2 % 32768;
  is1 = (is1*imult+(iss2-is2)/32768) % (65536);

  iseed = (is1*32768+is2) % imod;

  rand = scale * iseed;

  return rand;

  }

  public double seed() {

   double s,u1,u2,r;
     s = 1.0;
     do {
       u1 = update();
       u2 = update();

       v1 = 2.0 * u1 - 1.0;
       v2 = 2.0 * u2 - 1.0;
       s = v1*v1 + v2*v2;

     } while (s >= 1.0);

     r = Math.sqrt(-2.0*Math.log(s)/s);

     return r;

  }
}


