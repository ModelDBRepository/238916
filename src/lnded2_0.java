import java.io.*;
import java.text.*;
import java.util.*;
import swcparts.*;
import java.math.*;

public class lnded2_0 {

     public static void main(String[] args) {
          try {

               //*****************************************************************************
                //***********************Initialize global variables***************************
                 //*****************************************************************************
                  double DRconstant = 1;
               double TRconstant = 1;
               double PDRconstant = 1;
               int SomaType = 1; //sets the type tag for soma
               double PercentStep = .1;

               int RandomSeed = 1;
               Random myRand = new Random();
               myRand.setSeed(RandomSeed);

               //Parameter (.prn) file setable varialbes with default values
               int typeToDo = 3; //Type tag to do, default (3) is dendrite/basal dendrite
               int nToDo = 10; //number of virt trees to produce for each real one
               int Binning = 85; //minimum number of points (TR) in each bin

               double MinRad = .15;
               boolean Debugging = false;
               boolean SWCout = false;
               boolean DendroOut = false;
               boolean RealDendro = false;

               //ratio of virtual to real bifurcations at which explosive growth is said to have occured and further growth is terminated
               int explodeRatio = 20;

               //Data arays, maximum sizes and subscript variables
               int maxInFiles = 100;
               int actualInFiles = 0;
               int maxBiffLines = 100000;
               double realBiffCount = 0;
               int maxVirtBifLines = 20000;
               int actualBiffLines = 0;
               int maxStems = 1000;
               int stemCount = 0;
               int maxInputLines = 120;
               int actualInputLines = 0;
               int maxBins = 60;
               int actualRDbins = 1;
               int actualPLbins = 1;
               int actualBObins = 1;

               String[] inputLineArray = new String[maxInputLines];
               String[] inputFileArray = new String[maxInFiles];
               swcparts.SwcNeuron[] Neurons = new swcparts.SwcNeuron[maxInFiles];
               swcparts.BiffLine[] BifsArray = new swcparts.BiffLine[maxBiffLines];
               ////
               swcparts.BinnedBiffData[] BOTable = new swcparts.BinnedBiffData[maxBins];
               swcparts.BinnedBiffData[] PLTable = new swcparts.BinnedBiffData[maxBins];
               swcparts.BinnedBiffData[] RDTable = new swcparts.BinnedBiffData[maxBins];
               ////
               int[] realBifs = new int[maxInFiles]; //real biffs per cell
               int[] realTreeBifs = new int[maxStems]; //real bifs per tree
               double[] realAsymetryArray = new double[maxStems];
               double[] realSurfaceArray = new double[maxStems];
               double[] realSurfaceAsymArray = new double[maxStems];
               double[] stemRadArray = new double[maxStems];
               int stemRadArrayCurveType = 0;

               //cell parameter temporary values
               double tempDSrad;
               double tempDLrad;

               //other variables
               GammaDist gDist = new GammaDist();

               String inFile = ""; //input file name from cmd line
               String outFile = ""; //output file prefix from cmd line

               double realBiffMean = 0; //computed as total over n trees (not counting biffs of each tree)
               int tempBiff;
               boolean BifsMismatch = false;

               double stemMin = 0;
               double stemMax = 0;
               double stemMean = 0;
               double stemStdev = 0;
               double tempStemSum = 0;
               double tempStemSqSum = 0;

               double RealBifsSum = 0;
               double RealBifsSqSum = 0;
               double RealBifsMean = 0;
               double RealBifsStdev = 0;
               int RealBifsMax = 0;

               double RealAsymSum = 0;
               double RealAsymSqSum = 0;
               double RealAsymMean = 0;
               double RealAsymStdev = 0;

               double RealSurfaceSum = 0;
               double RealSurfaceSqSum = 0;
               double RealSurfaceMean = 0;
               double RealSurfaceStdev = 0;

               double RealSurfaceAsymSum = 0;
               double RealSurfaceAsymSqSum = 0;
               double RealSurfaceAsymMean = 0;
               double RealSurfaceAsymStdev = 0;

               //set number output style
               NumberFormat myFormat = NumberFormat.getInstance();
               myFormat.setMinimumIntegerDigits(1);
               myFormat.setMaximumFractionDigits(5);
               myFormat.setMinimumFractionDigits(5);

               //*****************************************************************************
                //*****************Parse command line arguments********************************
                 //*****************************************************************************

                  //set input and out file names
                  if (args.length == 2) {
                       inFile = args[0];
                       outFile = args[1];
                  }

                  //too few or too many command line args
                  else {
                       System.out.println("Incorrect command line arguments.");
                       System.out.println("Format = [input file name], [output file prefix] ");
                       System.exit(1);
                  }

               //*****************************************************************************
                //*****************Open and parse .prn input file******************************
                 //*****************************************************************************

                  //open input file
                  FileInputStream fin = new FileInputStream(inFile);
               BufferedInputStream bin = new BufferedInputStream(fin);
               //character stream
               BufferedReader r = new BufferedReader(new InputStreamReader(bin));
               r.mark(1);

               //Input comment lines into string array
               for (int j = 1; 1 < maxInputLines; j++) {
                    inputLineArray[j] = new String();
                    inputLineArray[j] = r.readLine();
                    if (inputLineArray[j] != null) {
                         ++actualInputLines;
                    }
                    else {
                         break;
                    }
               }
               r.close();

               //set variables as given in input file
               for (int j = 1; j <= actualInputLines; j++) {
                    if (inputLineArray[j].startsWith("TYPETODO")) {
                         typeToDo = Integer.parseInt(inputLineArray[ (j + 1)]);
                    }
                    if (inputLineArray[j].startsWith("NTODO")) {
                         nToDo = Integer.parseInt(inputLineArray[ (j + 1)]);
                    }
                    if (inputLineArray[j].startsWith("BINNING")) {
                         Binning = Integer.parseInt(inputLineArray[ (j + 1)]);
                    }
                    if (inputLineArray[j].startsWith("SEED")) {
                         RandomSeed = Integer.parseInt(inputLineArray[ (j + 1)]);
                    }
                    if (inputLineArray[j].startsWith("MINRAD")) {
                         MinRad = Double.parseDouble(inputLineArray[ (j + 1)]);
                    }
                    if (inputLineArray[j].startsWith("PERCENTSTEP")) {
                         PercentStep = Double.parseDouble(inputLineArray[ (j + 1)]);
                    }

                    if (inputLineArray[j].startsWith("DEBUGGING")) {
                         if (inputLineArray[ (j + 1)].startsWith("TRUE")) {
                              Debugging = true;
                         }
                         else if (inputLineArray[ (j + 1)].startsWith("FALSE")) {
                              Debugging = false;
                         }
                         else {
                              System.out.println(
                                     "ERROR:  Unrecognized value given for DEBUGGING.  Only TRUE and FALSE accepted.  Default value of " +
                                     Debugging + " used.");
                         }
                    }
                    if (inputLineArray[j].startsWith("DENDRO")) {
                         if (inputLineArray[ (j + 1)].startsWith("TRUE")) {
                              DendroOut = true;
                         }
                         else if (inputLineArray[ (j + 1)].startsWith("FALSE")) {
                              DendroOut = false;
                         }
                         else {
                              System.out.println(
                                     "ERROR:  Unrecognized value given for DENDRO.  Only TRUE and FALSE accepted.  Default value of " +
                                     Debugging + " used.");
                         }
                    }
                    if (inputLineArray[j].startsWith("REALDENDRO")) {
                         if (inputLineArray[ (j + 1)].startsWith("TRUE")) {
                              RealDendro = true;
                         }
                         else if (inputLineArray[ (j + 1)].startsWith("FALSE")) {
                              RealDendro = false;
                         }
                         else {
                              System.out.println(
                                     "ERROR:  Unrecognized value given for REALDENDRO.  Only TRUE and FALSE accepted.  Default value of " +
                                     Debugging + " used.");
                         }
                    }

                    if (inputLineArray[j].startsWith("SWCOUT")) {
                         if (inputLineArray[ (j + 1)].startsWith("TRUE")) {
                              SWCout = true;
                         }
                         else if (inputLineArray[ (j + 1)].startsWith("FALSE")) {
                              SWCout = false;
                         }
                         else {
                              System.out.println(
                                     "ERROR:  Unrecognized value given for SWCOUT.  Only TRUE and FALSE accepted.  Default value of " +
                                     SWCout + " used.");
                         }
                    }

                    if (inputLineArray[j].startsWith("INPUT")) {
                         actualInFiles = Integer.parseInt(inputLineArray[ (j + 1)]);
                         //  System.out.println("infiles = " + actualInFiles);
                         if (actualInFiles > maxInFiles) {
                              System.out.println("Too many input files given.  Only first " +
                                                 maxInFiles + " will be used.");
                              actualInFiles = maxInFiles;
                         }
                         if ( (actualInFiles + j + 2) > actualInputLines) {
                              System.out.println("Input File Error. Not enought input files given.");
                              System.exit( -1);
                         }
                         int infilecount = 0;
                         for (int k = j + 2; k <= j + actualInFiles + 2; k++) {
                              ++infilecount;
                              inputFileArray[infilecount] = new String(inputLineArray[k]);
                         }
                    }
               }

               //*****************************************************************************
                //***********************open and process SWC files****************************
                 //*****************************************************************************

                  //initialize output streams ****************************************************

                  //virtBifsOutFile :  print NBifs for each virt tree
                  FileOutputStream ggg = new FileOutputStream("VirtBifs_" + outFile);
               BufferedOutputStream hhh = new BufferedOutputStream(ggg);
               PrintWriter virtBifsOutFile = new PrintWriter(hhh);

               //inputInfoOutFile :  print each real bif line for debugging
               //  FileOutputStream fo = new FileOutputStream("InputInfo_" + outFile);
               //  BufferedOutputStream bfot = new BufferedOutputStream(fo);
               //  PrintWriter inputInfoOutFile = new PrintWriter(bfot);

               //tableOut :  print table file for program
               FileOutputStream fob = new FileOutputStream("Table_" + outFile);
               BufferedOutputStream bfotb = new BufferedOutputStream(fob);
               PrintWriter tableOut = new PrintWriter(bfotb);

               //virtDetailsOut :  print parameter details for each virt tree
               FileOutputStream fod = new FileOutputStream("OutputInfo_" + outFile);
               BufferedOutputStream dfot = new BufferedOutputStream(fod);
               PrintWriter virtDetailsOut = new PrintWriter(dfot);

               //virtAsymetryOut :  print parameter details for each virt tree
               FileOutputStream vays = new FileOutputStream("VirtAsymetry_" + outFile);
               BufferedOutputStream dvays = new BufferedOutputStream(vays);
               PrintWriter virtAsymetryFile = new PrintWriter(dvays);

               //virtSurfaceOut :  print parameter details for each virt tree
               FileOutputStream says = new FileOutputStream("VirtSurface_" + outFile);
               BufferedOutputStream dsays = new BufferedOutputStream(says);
               PrintWriter virtSurfaceFile = new PrintWriter(dsays);

               //virtSurfaceAsymetryOut :  print parameter details for each virt tree
               FileOutputStream gays = new FileOutputStream("VirtSurfaceAsym_" + outFile);
               BufferedOutputStream dgays = new BufferedOutputStream(gays);
               PrintWriter virtSurfaceAsymFile = new PrintWriter(dgays);

               //open swc files and place them into an array of swcNeuron type
               for (int j = 1; j <= actualInFiles; j++) {
                    Neurons[j] = new swcparts.SwcNeuron();
                    Neurons[j].InitSwcNeuron(inputFileArray[j]);
                    System.out.println("processing " + inputFileArray[j]);
                    Neurons[j].removeOtherTypeSideBranches(typeToDo);
               }

               //Put all bifurcation information into bifLineType array and all stem radius into array of doubles
               for (int j = 1; j <= actualInFiles; j++) { //for each neuron
                    realBifs[j] = 0;
                    for (int k = 1; k <= Neurons[j].getSize(); k++) { //for every line

                         Neurons[j].setEuclidianDistancesToSoma();

                         if (Neurons[j].getType(k) == typeToDo) { //only wory about lines of correct type
                              // set initial diameters
                              if (k > 1) {
                                   //count stems and get stem start radius
                                   if (Neurons[j].getType(Neurons[j].getLink(k)) == SomaType) {
                                        ++stemCount;

                                        stemRadArray[stemCount] = Neurons[j].getRad(k);

                                        realTreeBifs[stemCount] = Neurons[j].getTreeSize(k,
                                               typeToDo);
                                        if (realTreeBifs[stemCount] > RealBifsMax) {
                                             RealBifsMax = realTreeBifs[stemCount];
                                        }

                                        realAsymetryArray[stemCount] = Neurons[j].getTreeAsymetry(k,
                                               typeToDo);

                                        realSurfaceArray[stemCount] = Neurons[j].getTreeSurface(k,
                                               typeToDo);

                                        //  System.out.println(realSurfaceArray[stemCount]);

                                        realSurfaceAsymArray[stemCount] = Neurons[j].
                                               getTreeSurfaceAsym(
                                               k, typeToDo);

                                        // System.out.println("  Neuron, Stem, Asymetry "+j+"  "+k+"  "+realAsymetryArray[stemCount]);
                                   }
                              }

                              //count Bifs for this cell
                              if ( (Neurons[j].getNumDaughters(k) == 2)) {
                                   ++realBifs[j];
                              }

                              //initialize and populate BIFSarray with only those lines that are bifurcations or terminations
                              if ( (Neurons[j].getNumDaughters(k) == 2) ||
                                  (Neurons[j].getNumDaughters(k) == 0)) {
                                   ++actualBiffLines;
                                   BifsArray[actualBiffLines] = new swcparts.BiffLine();
                                   tempBiff = 0;
                                   tempDSrad = 0;
                                   tempDLrad = 0;

                                   //if bifurcation, set daughter diameters
                                   if (Neurons[j].getNumDaughters(k) == 2) {
                                        tempBiff = 1;

                                        if (Neurons[j].getDaughter1rad(k) >
                                            Neurons[j].getDaughter2rad(k)) {
                                             tempDSrad = Neurons[j].getDaughter2rad(k);
                                             tempDLrad = Neurons[j].getDaughter1rad(k);
                                        }
                                        else {
                                             tempDSrad = Neurons[j].getDaughter1rad(k);
                                             tempDLrad = Neurons[j].getDaughter2rad(k);
                                        }
                                   }

                                   BifsArray[actualBiffLines].setAll(Neurons[j].getBranchOrder(k),
                                          (Neurons[j].getTreeLength(k) -
                                           Neurons[j].getBranchLength(k)),
                                          Neurons[j].getTreeLength(k),
                                          Neurons[j].getEDistanceToSoma(k), Neurons[j].getY(k),
                                          Neurons[j].getRad(k), Neurons[j].getBranchStartRad(k),
                                          Neurons[j].getBranchLength(k), tempBiff, tempDSrad,
                                          tempDLrad);
                              }
                         }
                    }
                    realBiffCount = realBiffCount + realBifs[j];
               }
               realBiffMean = (realBiffCount / stemCount);

//**************Create input table for sampling virtual neurons****************
                //compute  initial stem diameter mean, stdev
                if (stemCount <= 0) {
                     System.out.println("No tree stems found.");
                     System.exit(4);
                }
                else {
                     stemMin = stemRadArray[1];
                     stemMax = stemRadArray[1];
                     for (int i = 1; i <= stemCount; i++) { //for each stem
                          tempStemSum = tempStemSum + stemRadArray[i];
                          if (stemRadArray[i] < stemMin) {
                               stemMin = stemRadArray[i];
                          }
                          if (stemRadArray[i] > stemMax) {
                               stemMax = stemRadArray[i];
                          }
                     }
                     stemMean = tempStemSum / stemCount;
                     for (int i = 1; i <= stemCount; i++) { //for each stem

                          tempStemSqSum = tempStemSqSum +
                                 ( (stemRadArray[i] - stemMean) *
                                  (stemRadArray[i] - stemMean));
                     }
                     stemStdev = java.lang.Math.sqrt(tempStemSqSum / (stemCount - 1)); ;
                }
                
                System.out.println("inded2: Main:\t"+java.util.Arrays.toString(stemRadArray));
                System.out.println("count:\t"+stemCount);
               stemRadArrayCurveType = GammaDist.getCurveType(stemRadArray, stemCount, myRand);
               double symetricStemMax = ( (stemMean * 2) - stemMin);
               //populate input tables
               BOTable = BiffLine.createBBDTable(BifsArray, actualBiffLines, "BO", Binning, myRand);
               PLTable = BiffLine.createBBDTable(BifsArray, actualBiffLines, "PLS", Binning, myRand);
               RDTable = BiffLine.createBBDTable(BifsArray, actualBiffLines, "RAD", Binning, myRand);

               //    if (Debugging) {
               tableOut.println("Branch Order Table.");
               BinnedBiffData.writeOutInputTable(BOTable, tableOut, BOTable[1].getArraySize());
               tableOut.println("Path Length Table.");
               BinnedBiffData.writeOutInputTable(PLTable, tableOut, PLTable[1].getArraySize());
               tableOut.println("Radius Table.");
               BinnedBiffData.writeOutInputTable(RDTable, tableOut, RDTable[1].getArraySize());

               // inputInfoOutFile.close();
               tableOut.close();
               //  }
//******************************************************************************
//******************************************************************************
//*********************       END OF INPUT PROCESSING       ********************
//*********************       Generation of new trees       ********************
//******************************************************************************
//******************************************************************************
                     virtBifsOutFile.println("inFile , outFile");
               virtBifsOutFile.println(inFile + " , " + outFile);

               virtAsymetryFile.println("inFile , outFile");
               virtAsymetryFile.println(inFile + " , " + outFile);

               virtSurfaceFile.println("inFile , outFile");
               virtSurfaceFile.println(inFile + " , " + outFile);

               virtSurfaceAsymFile.println("inFile , outFile");
               virtSurfaceAsymFile.println(inFile + " , " + outFile);

               int newTreeCount = stemCount * nToDo; //number of virtual trees to create
               System.out.println("Creating " + newTreeCount + " virtual trees.");
               if (Debugging) {
                    virtDetailsOut.println();
               }
               virtBifsOutFile.println(
                      "mean of group means , stdev of group means , , mean of group stdevs , stdev of group stdevs");

               virtAsymetryFile.println(
                      "mean of group means , stdev of group means , , mean of group stdevs , stdev of group stdevs");

               virtSurfaceFile.println(
                      "mean of group means , stdev of group means , , mean of group stdevs , stdev of group stdevs");

               virtSurfaceAsymFile.println(
                      "mean of group means , stdev of group means , , mean of group stdevs , stdev of group stdevs");

//******************master loop for mixing of fundemental parameters********

                double PLpercentDR = 0;
               double RDpercentDR = 0;
               double BOpercentDR = 0;

               double PLpercentPDR = 0;
               double RDpercentPDR = 0;
               double BOpercentPDR = 0;

               double PLpercentTR = 0;
               double RDpercentTR = 0;
               double BOpercentTR = 0;

               double PLpercentBPL = 0;
               double RDpercentBPL = 0;
               double BOpercentBPL = 0;

               double PLpercentBIFF = 0;
               double RDpercentBIFF = 0;
               double BOpercentBIFF = 0;
               int groupN = 0;
               for (int DRdeterminant = 1; DRdeterminant <= 3; DRdeterminant++) {
                    for (int PDRdeterminant = 1; PDRdeterminant <= 3; PDRdeterminant++) {
                         for (int TRdeterminant = 1; TRdeterminant <= 3; TRdeterminant++) {
                              for (int BPLdeterminant = 1; BPLdeterminant <= 3; BPLdeterminant++) {
                                   for (int BIFFdeterminant = 1; BIFFdeterminant <= 3;
                                        BIFFdeterminant++) {
                                        ++groupN;
                                        //Variables to track and limit explosive growth
                                        boolean doesExplodeGroup = false;
                                        int explodeCount = 0;
                                        int MaxBifs = explodeRatio * RealBifsMax;

                                        switch (DRdeterminant) {
                                             case 1:
                                                  PLpercentDR = 1;
                                                  RDpercentDR = 0;
                                                  BOpercentDR = 0;
                                                  System.out.print("DR = PL;  ");
                                                  virtBifsOutFile.print("DR = PL;  ");
                                                  virtAsymetryFile.print("DR = PL;  ");
                                                  virtSurfaceFile.print("DR = PL;  ");
                                                  virtSurfaceAsymFile.print("DR = PL;  ");
                                                  break;
                                             case 2:
                                                  PLpercentDR = 0;
                                                  RDpercentDR = 1;
                                                  BOpercentDR = 0;
                                                  System.out.print("DR = RD;  ");
                                                  virtBifsOutFile.print("DR = RD;  ");
                                                  virtAsymetryFile.print("DR = RD;  ");
                                                  virtSurfaceFile.print("DR = RD;  ");
                                                  virtSurfaceAsymFile.print("DR = RD;  ");
                                                  break;
                                             case 3:
                                                  PLpercentDR = 0;
                                                  RDpercentDR = 0;
                                                  BOpercentDR = 1;
                                                  System.out.print("DR = BO;  ");
                                                  virtBifsOutFile.print("DR = BO;  ");
                                                  virtAsymetryFile.print("DR = BO;  ");
                                                  virtSurfaceFile.print("DR = BO;  ");
                                                  virtSurfaceAsymFile.print("DR = BO;  ");
                                                  break;
                                             default:
                                                  System.out.println("switch error, value is : " +
                                                         DRdeterminant);
                                                  System.exit(44);
                                        }
                                        switch (PDRdeterminant) {
                                             case 1:
                                                  PLpercentPDR = 1;
                                                  RDpercentPDR = 0;
                                                  BOpercentPDR = 0;
                                                  System.out.print("PDR = PL;  ");
                                                  virtBifsOutFile.print("PDR = PL;  ");
                                                  virtAsymetryFile.print("PDR = PL;  ");
                                                  virtSurfaceFile.print("PDR = PL;  ");
                                                  virtSurfaceAsymFile.print("PDR = PL;  ");
                                                  break;
                                             case 2:
                                                  PLpercentPDR = 0;
                                                  RDpercentPDR = 1;
                                                  BOpercentPDR = 0;
                                                  System.out.print("PDR = RD;  ");
                                                  virtBifsOutFile.print("PDR = RD;  ");
                                                  virtAsymetryFile.print("PDR = RD;  ");
                                                  virtSurfaceFile.print("PDR = RD;  ");
                                                  virtSurfaceAsymFile.print("PDR = RD;  ");
                                                  break;
                                             case 3:
                                                  PLpercentPDR = 0;
                                                  RDpercentPDR = 0;
                                                  BOpercentPDR = 1;
                                                  System.out.print("PDR = BO;  ");
                                                  virtBifsOutFile.print("PDR = BO;  ");
                                                  virtAsymetryFile.print("PDR = BO;  ");
                                                  virtSurfaceFile.print("PDR = BO;  ");
                                                  virtSurfaceAsymFile.print("PDR = BO;  ");
                                                  break;
                                             default:
                                                  System.out.println("switch error, value is : " +
                                                         PDRdeterminant);
                                                  System.exit(44);
                                        }
                                        switch (TRdeterminant) {
                                             case 1:
                                                  PLpercentTR = 1;
                                                  RDpercentTR = 0;
                                                  BOpercentTR = 0;
                                                  System.out.print("TR = PL;  ");
                                                  virtBifsOutFile.print("TR = PL;  ");
                                                  virtAsymetryFile.print("TR = PL;  ");
                                                  virtSurfaceFile.print("TR = PL;  ");
                                                  virtSurfaceAsymFile.print("TR = PL;  ");
                                                  break;
                                             case 2:
                                                  PLpercentTR = 0;
                                                  RDpercentTR = 1;
                                                  BOpercentTR = 0;
                                                  System.out.print("TR = RD;  ");
                                                  virtBifsOutFile.print("TR = RD;  ");
                                                  virtAsymetryFile.print("TR = RD;  ");
                                                  virtSurfaceFile.print("TR = RD;  ");
                                                  virtSurfaceAsymFile.print("TR = RD;  ");
                                                  break;
                                             case 3:
                                                  PLpercentTR = 0;
                                                  RDpercentTR = 0;
                                                  BOpercentTR = 1;
                                                  System.out.print("TR = BO;  ");
                                                  virtBifsOutFile.print("TR = BO;  ");
                                                  virtAsymetryFile.print("TR = BO;  ");
                                                  virtSurfaceFile.print("TR = BO;  ");
                                                  virtSurfaceAsymFile.print("TR = BO;  ");
                                                  break;
                                             default:
                                                  System.out.println("switch error, value is : " +
                                                         TRdeterminant);
                                                  System.exit(44);
                                        }
                                        switch (BPLdeterminant) {
                                             case 1:
                                                  PLpercentBPL = 1;
                                                  RDpercentBPL = 0;
                                                  BOpercentBPL = 0;
                                                  System.out.print("BPL = PL;  ");
                                                  virtBifsOutFile.print("BPL = PL;  ");
                                                  virtAsymetryFile.print("BPL = PL;  ");
                                                  virtSurfaceFile.print("BPL = PL;  ");
                                                  virtSurfaceAsymFile.print("BPL = PL;  ");
                                                  break;
                                             case 2:
                                                  PLpercentBPL = 0;
                                                  RDpercentBPL = 1;
                                                  BOpercentBPL = 0;
                                                  System.out.print("BPL = RD;  ");
                                                  virtBifsOutFile.print("BPL = RD;  ");
                                                  virtAsymetryFile.print("BPL = RD;  ");
                                                  virtSurfaceFile.print("BPL = RD;  ");
                                                  virtSurfaceAsymFile.print("BPL = RD;  ");
                                                  break;
                                             case 3:
                                                  PLpercentBPL = 0;
                                                  RDpercentBPL = 0;
                                                  BOpercentBPL = 1;
                                                  System.out.print("BPL = BO;  ");
                                                  virtBifsOutFile.print("BPL = BO;  ");
                                                  virtAsymetryFile.print("BPL = BO;  ");
                                                  virtSurfaceFile.print("BPL = BO;  ");
                                                  virtSurfaceAsymFile.print("BPL = BO;  ");
                                                  break;
                                             default:
                                                  System.out.println("switch error, value is : " +
                                                         BPLdeterminant);
                                                  System.exit(44);
                                        }
                                        switch (BIFFdeterminant) {
                                             case 1:
                                                  PLpercentBIFF = 1;
                                                  RDpercentBIFF = 0;
                                                  BOpercentBIFF = 0;
                                                  System.out.println("BIFF = PL");
                                                  virtBifsOutFile.println("BIFF = PL");
                                                  virtAsymetryFile.println("BIFF = PL");
                                                  virtSurfaceFile.println("BIFF = PL");
                                                  virtSurfaceAsymFile.println("BIFF = PL");
                                                  break;
                                             case 2:
                                                  PLpercentBIFF = 0;
                                                  RDpercentBIFF = 1;
                                                  BOpercentBIFF = 0;
                                                  System.out.println("BIFF = RD");
                                                  virtBifsOutFile.println("BIFF = RD");
                                                  virtAsymetryFile.println("BIFF = RD");
                                                  virtSurfaceFile.println("BIFF = RD");
                                                  virtSurfaceAsymFile.println("BIFF = RD");
                                                  break;
                                             case 3:
                                                  PLpercentBIFF = 0;
                                                  RDpercentBIFF = 0;
                                                  BOpercentBIFF = 1;
                                                  System.out.println("BIFF = BO");
                                                  virtBifsOutFile.println("BIFF = BO");
                                                  virtAsymetryFile.println("BIFF = BO");
                                                  virtSurfaceFile.println("BIFF = BO");
                                                  virtSurfaceAsymFile.println("BIFF = BO");
                                                  break;
                                             default:
                                                  System.out.println("switch error, value is : " +
                                                         BIFFdeterminant);
                                                  System.exit(44);
                                        }

                                        /*
                                         for (double RDpercent = 0; RDpercent <= 1; RDpercent += PercentStep) {
                                             for (double PLpercent = 0; PLpercent <= 1 - RDpercent;
                                                  PLpercent += PercentStep) {
                                                  double BOpercent = 1 - (RDpercent + PLpercent);

                                                  //lot of trouble to correctly round and cast a double to int
                                                  BigDecimal rdbd = new BigDecimal(RDpercent);
                                                  BigDecimal plbd = new BigDecimal(PLpercent);
                                                  BigDecimal bobd = new BigDecimal(BOpercent);
                                         rdbd = rdbd.setScale(4, BigDecimal.ROUND_HALF_UP);
                                         plbd = plbd.setScale(4, BigDecimal.ROUND_HALF_UP);
                                         bobd = bobd.setScale(4, BigDecimal.ROUND_HALF_UP);

                                         int intRDpercent = (int) (rdbd.doubleValue() * 100);
                                         int intBOpercent = (int) (bobd.doubleValue() * 100);
                                         int intPLpercent = (int) (plbd.doubleValue() * 100);
                                         System.out.println(":rd% = " + intRDpercent + "  pl% = " + intPLpercent +
                                                                     "  bo% = " + intBOpercent);
                                         virtBifsOutFile.println(":rd% = " + intRDpercent + "  pl% = " +
                                                                          intPLpercent +
                                         "  bo% = " + intBOpercent);
                                         virtAsymetryFile.println(":rd% = " + intRDpercent + "  pl% = " +
                                                         intPLpercent +
                                                         "  bo% = " + intBOpercent);
                                         virtSurfaceFile.println(":rd% = " + intRDpercent + "  pl% = " +
                                                                          intPLpercent +
                                         "  bo% = " + intBOpercent);
                                         virtSurfaceAsymFile.println(":rd% = " + intRDpercent + "  pl% = " +
                                                         intPLpercent +
                                                         "  bo% = " + intBOpercent);

                                                  if (Debugging) {
                                         virtDetailsOut.println(":rd% = " + intRDpercent + "  pl% = " +
                                                              intPLpercent +
                                                              "  bo% = " + intBOpercent);
                                                  }
                                                        //
                                         */
                                        actualRDbins = RDTable[1].getArraySize();
                                        actualPLbins = PLTable[1].getArraySize();
                                        actualBObins = BOTable[1].getArraySize();

                                        boolean[] virtExplodeArray = new boolean[newTreeCount + 1];

                                        int[] virtBifsArray = new int[newTreeCount + 1];
                                        double[] virtAsymArray = new double[newTreeCount + 1];
                                        double virtAsymSum = 0;

                                        double[] virtSurfaceArray = new double[newTreeCount + 1];
                                        double virtSurfaceSum = 0;

                                        double[] virtSurfaceAsymArray = new double[newTreeCount + 1];
                                        double virtSurfaceAsymSum = 0;

                                        double BifsSum = 0;
                                        double BifsMean = 0;
                                        swcparts.BiffLine[] virtTreesArray = new swcparts.BiffLine[
                                               maxVirtBifLines + 1];

                                        for (int currentBranch = 1;
                                             currentBranch <= maxVirtBifLines;
                                             currentBranch++) {
                                             virtTreesArray[currentBranch] = new swcparts.BiffLine();
                                        }

                                        double tempTP = 0;
                                        double tempDR = 0;
                                        double tempPDR = 0;
                                        double tempBPL = 0;
                                        int NBifs = 0;

                                        if (Debugging) {
                                             virtDetailsOut.println(
                                                    "Line , startRad, taperRate, endFP, branchPL, bif, termRand, random, PDR, DR, d1Rad, d2Rad ");
                                        }
                                        int treeSize = 0;
                                        for (int currentTreeN = 1; currentTreeN <= newTreeCount;
                                             currentTreeN++) { //for each virtual tree
                                             treeSize = 0;
                                             boolean doesExplodeTree = false;
                                             if (Debugging) {
                                                  virtBifsOutFile.print(currentTreeN);
                                                  virtDetailsOut.println();
                                                  virtDetailsOut.println("Tree number : " +
                                                         currentTreeN);
                                             }
                                             NBifs = 0;

                                             double idia = 0;

                                             if (stemRadArrayCurveType == 1) {
                                                  idia = (myRand.nextDouble() *
                                                         (symetricStemMax - stemMin) +
                                                         stemMin);
                                             }
                                             else if (stemRadArrayCurveType == 2) {
                                                  while ( (idia < stemMin) ||
                                                         (idia > symetricStemMax)) {
                                                       idia = gDist.GetGammaMS(stemMean, stemStdev,
                                                              myRand);
                                                  }
                                             }
                                             else if (stemRadArrayCurveType == 3) {
                                                  idia = stemMin - 1;
                                                  while ( (idia < stemMin) || (idia > stemMax)) {
                                                       idia = (myRand.nextGaussian() * stemStdev) +
                                                              stemMean;
                                                  }
                                             }
                                             else {
                                                  System.out.println(
                                                         "stemRadArrayCurveType invalid:  " +
                                                         stemRadArrayCurveType);
                                                  System.exit( -11);
                                             }

                                             if (Debugging) {
                                                  System.out.print("Tree " + currentTreeN +
                                                         "  idia: " + idia);
                                             }
                                             virtTreesArray[1].setStartRAD(idia);
                                             virtTreesArray[1].setBO(0);
                                             virtTreesArray[1].setStartPLS(0);
                                             virtTreesArray[1].setParrentNum( -1);

                                             //go through each line of the new virt cell cell
                                             int nextFreeBranchNum = 2;
                                             int tempCurrnetBranchLoopMax = (maxVirtBifLines - 4) /
                                                    2;
                                             double tempTermRand = 0;
                                             double tempCompareRand = 0;
                                             for (int currentBranch = 1;
                                                  currentBranch <= tempCurrnetBranchLoopMax;
                                                  currentBranch++) {

                                                  //find fundemental param bin for start of seg
                                                  int currentRDbin = actualRDbins;
                                                  int currentPLbin = actualPLbins;
                                                  int currentBObin = actualBObins;

                                                  for (int j = actualRDbins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getStartFundementalParamValue(
                                                              "RAD") <=
                                                              RDTable[j].getFPbinMax()) {
                                                            currentRDbin = j;
                                                       }
                                                  }
                                                  for (int j = actualPLbins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getStartFundementalParamValue(
                                                              "PLS") <=
                                                              PLTable[j].getFPbinMax()) {
                                                            currentPLbin = j;
                                                       }
                                                  }

                                                  for (int j = actualBObins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getStartFundementalParamValue(
                                                              "BO") <=
                                                              BOTable[j].getFPbinMax()) {
                                                            currentBObin = j;
                                                       }
                                                  }

                                                  //taper
                                                  //sample taper rate

                                                  if (virtTreesArray[currentBranch].
                                                         getStartRAD() <=
                                                         MinRad) {
                                                       tempTP = 1;
                                                  }
                                                  else {
                                                       double tempTPuf = ( (RDTable[
                                                              currentRDbin].
                                                              getTRuf() *
                                                              RDpercentTR) +
                                                              (PLTable[currentPLbin].getTRuf() *
                                                              PLpercentTR) +
                                                              (BOTable[currentBObin].getTRuf() *
                                                              BOpercentTR));

                                                       if (myRand.nextDouble() < tempTPuf) {
                                                            tempTP = TRconstant;
                                                       }
                                                       else {
                                                            //    tempTP = ( (RDTable[currentRDbin].
                                                            //           getParamValueCurve(
                                                            //           "TP", myRand, gDist) *
                                                            //           RDpercentTR) +
                                                            //           (PLTable[currentPLbin].
                                                            //           getParamValueCurve(
                                                            //           "TP", myRand, gDist) *
                                                            //           PLpercentTR) +
                                                            //           (BOTable[currentBObin].
                                                            //           getParamValueCurve(
                                                            //           "TP", myRand, gDist) *
                                                            //           BOpercentTR));
                                                            if (RDpercentTR == 1) {
                                                                 tempTP = RDTable[currentRDbin].
                                                                        getParamValueCurve("TP",
                                                                        myRand, gDist);
                                                            }
                                                            else if (PLpercentTR == 1) {
                                                                 tempTP = PLTable[currentPLbin].
                                                                        getParamValueCurve("TP",
                                                                        myRand, gDist);
                                                            }
                                                            else if (BOpercentTR == 1) {
                                                                 tempTP = BOTable[currentBObin].
                                                                        getParamValueCurve("TP",
                                                                        myRand, gDist);
                                                            }
                                                            else {
                                                                 System.out.println("TP error");
                                                            }
                                                       }
                                                  }

                                                  //set end rad to start rad * tp
                                                  virtTreesArray[currentBranch].setEndRAD(
                                                         virtTreesArray[currentBranch].
                                                         getStartRAD() /
                                                         tempTP);

                                                  // find branch path length

                                                  //    tempBPL = ( (RDTable[currentRDbin].
                                                  //           getParamValueCurve(
                                                  //           "BPL", myRand, gDist) * RDpercentBPL) +
                                                  //           (PLTable[currentPLbin].
                                                  //           getParamValueCurve(
                                                  //           "BPL", myRand, gDist) * PLpercentBPL) +
                                                  //           (BOTable[currentBObin].
                                                  //           getParamValueCurve(
                                                  //           "BPL", myRand, gDist) * BOpercentBPL));
                                                  if (RDpercentBPL == 1) {
                                                       tempBPL = RDTable[currentRDbin].
                                                              getParamValueCurve(
                                                              "BPL", myRand, gDist);
                                                  }
                                                  else if (PLpercentBPL == 1) {
                                                       tempBPL = PLTable[currentPLbin].
                                                              getParamValueCurve(
                                                              "BPL", myRand, gDist);
                                                  }
                                                  else if (BOpercentBPL == 1) {
                                                       tempBPL = BOTable[currentBObin].
                                                              getParamValueCurve(
                                                              "BPL", myRand, gDist);
                                                  }
                                                  else {
                                                       System.out.println("BPL error");
                                                  }
                                                  //set bramch path length
                                                  virtTreesArray[currentBranch].setBPL(tempBPL);

                                                  virtTreesArray[currentBranch].setSurface(Math.PI *
                                                         (virtTreesArray[currentBranch].getStartRAD() +
                                                         virtTreesArray[currentBranch].getEndRAD()) *
                                                         tempBPL);

                                                  //set path length soma
                                                  virtTreesArray[currentBranch].setEndPLS(
                                                         virtTreesArray[currentBranch].
                                                         getStartPLS() +
                                                         tempBPL);

                                                  //reset current fundemental paramater bin with end value
                                                  currentRDbin = actualRDbins;
                                                  currentPLbin = actualPLbins;
                                                  currentBObin = actualBObins;

                                                  for (int j = actualRDbins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getEndFundementalParamValue(
                                                              "RAD") <=
                                                              RDTable[j].getFPbinMax()) {
                                                            currentRDbin = j;
                                                       }
                                                  }
                                                  for (int j = actualPLbins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getEndFundementalParamValue(
                                                              "PLS") <=
                                                              PLTable[j].getFPbinMax()) {
                                                            currentPLbin = j;
                                                       }
                                                  }

                                                  for (int j = actualBObins; j >= 1; j--) {
                                                       if (virtTreesArray[currentBranch].
                                                              getEndFundementalParamValue(
                                                              "BO") <=
                                                              BOTable[j].getFPbinMax()) {
                                                            currentBObin = j;
                                                       }
                                                  }

                                                  //Print out virtual cell details
                                                  if (Debugging) {
                                                       virtDetailsOut.print(currentBranch +
                                                              " , " +
                                                              virtTreesArray[currentBranch].
                                                              getStartRAD() +
                                                              " , " +
                                                              tempTP + " , " + tempBPL);
                                                  }

                                                  //Bif?
                                                  //sample bif prob
                                                  //if no, set bif to 0
                                                  virtTreesArray[currentBranch].setBIFF(1);

                                                  //     tempTermRand = ( (RDTable[currentRDbin].
                                                  //            getParamValueCurve(
                                                  //            "BP", myRand, gDist) * RDpercentBIFF) +
                                                  //            (PLTable[currentPLbin].
                                                  //            getParamValueCurve(
                                                  //            "BP", myRand, gDist) * PLpercentBIFF) +
                                                  //            (BOTable[currentBObin].
                                                  //            getParamValueCurve(
                                                  //            "BP", myRand, gDist) * BOpercentBIFF));
                                                  if (RDpercentBIFF == 1) {
                                                       tempTermRand = RDTable[currentRDbin].
                                                              getParamValueCurve(
                                                              "BP", myRand, gDist);
                                                  }
                                                  else if (PLpercentBIFF == 1) {
                                                       tempTermRand = PLTable[currentPLbin].
                                                              getParamValueCurve(
                                                              "BP", myRand, gDist);
                                                  }
                                                  else if (BOpercentBIFF == 1) {
                                                       tempTermRand = BOTable[currentBObin].
                                                              getParamValueCurve(
                                                              "BP", myRand, gDist);
                                                  }
                                                  else {
                                                       System.out.println("BIFF error");
                                                  }
                                                  tempCompareRand = myRand.nextDouble();
                                                  //    if ( (tempTermRand < tempCompareRand) ||
                                                  //        (virtTreesArray[currentBranch].getEndFundementalParamValue(
                                                  //               "RAD") <= MinRad)) {
                                                  //         virtTreesArray[currentBranch].setBIFF(0);
                                                  //    }
                                                  if ( (tempTermRand < tempCompareRand)) {
                                                       virtTreesArray[currentBranch].setBIFF(0);
                                                  }

                                                  //      System.out.println("BO bin, tempTermRand, tempCompareRand, bif?  " +currentBObin+"  "+ tempTermRand+"  "+ tempCompareRand+"  "+virtTreesArray[currentBranch].getBIFF());
                                                  //Print out virtual Bif cell details
                                                  if (Debugging) {
                                                       virtDetailsOut.print(" , " +
                                                              virtTreesArray[currentBranch].
                                                              getBIFF() +
                                                              " , " + tempTermRand + " , " +
                                                              tempCompareRand);
                                                  }

                                                  //if yes, set bif to 1
                                                  if (virtTreesArray[currentBranch].getBIFF() ==
                                                         1) {
                                                       ++NBifs;

                                                       double tempPDRuf = ( (RDTable[
                                                              currentRDbin].
                                                              getPDRuf() *
                                                              RDpercentPDR) +
                                                              (PLTable[currentPLbin].getPDRuf() *
                                                              PLpercentPDR) +
                                                              (BOTable[currentBObin].getPDRuf() *
                                                              BOpercentPDR));

                                                       if (myRand.nextDouble() < tempPDRuf) {
                                                            tempPDR = PDRconstant;
                                                       }
                                                       else {
                                                            //    tempPDR = ( (RDTable[currentRDbin].
                                                            //           getParamValueCurve(
                                                            //           "PDR", myRand, gDist) *
                                                            //           RDpercentPDR) +
                                                            //           (PLTable[currentPLbin].
                                                            //           getParamValueCurve(
                                                            //           "PDR", myRand, gDist) *
                                                            //           PLpercentPDR) +
                                                            //           (BOTable[currentBObin].
                                                            //           getParamValueCurve(
                                                            //           "PDR", myRand, gDist) *
                                                            //           BOpercentPDR));
                                                            if (RDpercentPDR == 1) {
                                                                 tempPDR = RDTable[currentRDbin].
                                                                        getParamValueCurve(
                                                                        "PDR", myRand, gDist);
                                                            }
                                                            else if (PLpercentPDR == 1) {
                                                                 tempPDR = PLTable[currentPLbin].
                                                                        getParamValueCurve(
                                                                        "PDR", myRand, gDist);
                                                            }
                                                            else if (BOpercentPDR == 1) {
                                                                 tempPDR = BOTable[currentBObin].
                                                                        getParamValueCurve(
                                                                        "PDR", myRand, gDist);
                                                            }
                                                            else {
                                                                 System.out.println("PDR error");
                                                            }

                                                       }

                                                       if ( (virtTreesArray[currentBranch].
                                                              getEndFundementalParamValue(
                                                              "RAD") <=
                                                              MinRad)) {
                                                            tempPDR = PDRconstant;
                                                       }
                                                       //Large Daughter rad = end rad / PDR
                                                       virtTreesArray[currentBranch].setDLRAD(
                                                              virtTreesArray[currentBranch].
                                                              getEndRAD() /
                                                              tempPDR);

                                                       double tempDRuf = ( (RDTable[
                                                              currentRDbin].
                                                              getDRuf() *
                                                              RDpercentDR) +
                                                              (PLTable[currentPLbin].getDRuf() *
                                                              PLpercentDR) +
                                                              (BOTable[currentBObin].getDRuf() *
                                                              BOpercentDR));

                                                       if (myRand.nextDouble() < tempDRuf) {
                                                            tempDR = DRconstant;
                                                       }
                                                       else {
                                                            //  tempDR = ( (RDTable[currentRDbin].
                                                            //         getParamValueCurve(
                                                            //         "DR", myRand, gDist) *
                                                            //         RDpercentDR) +
                                                            //         (PLTable[currentPLbin].
                                                            //         getParamValueCurve(
                                                            //      "DR", myRand, gDist) *
                                                            //      PLpercentDR) +
                                                            //      (BOTable[currentBObin].
                                                            //       getParamValueCurve(
                                                            //       "DR", myRand, gDist) *
                                                            //       BOpercentDR));

                                                            if (RDpercentDR == 1) {
                                                                 tempDR = RDTable[currentRDbin].
                                                                        getParamValueCurve(
                                                                        "DR", myRand, gDist);
                                                            }
                                                            else if (PLpercentDR == 1) {
                                                                 tempDR = PLTable[currentPLbin].
                                                                        getParamValueCurve(
                                                                        "DR", myRand, gDist);
                                                            }
                                                            else if (BOpercentDR == 1) {
                                                                 tempDR = BOTable[currentBObin].
                                                                        getParamValueCurve(
                                                                        "DR", myRand, gDist);
                                                            }
                                                            else {
                                                                 System.out.println("DR error");
                                                            }

                                                       }
                                                       if ( (virtTreesArray[currentBranch].
                                                              getEndFundementalParamValue(
                                                              "RAD") <=
                                                              MinRad)) {
                                                            tempDR = DRconstant;
                                                       }

                                                       //Samll Daughter Rad = Large Daughter Rad / DR

                                                       virtTreesArray[currentBranch].setDSRAD(
                                                              virtTreesArray[currentBranch].
                                                              getDLRAD() / tempDR);

                                                       //initialize new daughters
                                                       virtTreesArray[currentBranch].setD1num(
                                                              nextFreeBranchNum);
                                                       ++nextFreeBranchNum;
                                                       virtTreesArray[currentBranch].setD2num(
                                                              nextFreeBranchNum);
                                                       ++nextFreeBranchNum;

                                                       //set new daughter diameters
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD1num()].
                                                              setStartRAD(
                                                              virtTreesArray[currentBranch].
                                                              getDLRAD());
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD2num()].
                                                              setStartRAD(
                                                              virtTreesArray[currentBranch].
                                                              getDSRAD());

                                                       //set parrent number
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD1num()].
                                                              setParrentNum(currentBranch);
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD2num()].
                                                              setParrentNum(currentBranch);

                                                       //set branch orders
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD1num()].
                                                              setBO(virtTreesArray[currentBranch].
                                                              getBO() + 1);
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD2num()].
                                                              setBO(virtTreesArray[currentBranch].
                                                              getBO() + 1);

                                                       //set pls
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD1num()].
                                                              setStartPLS(
                                                              virtTreesArray[currentBranch].
                                                              getEndPLS());
                                                       virtTreesArray[virtTreesArray[currentBranch].
                                                              getD2num()].
                                                              setStartPLS(
                                                              virtTreesArray[currentBranch].
                                                              getEndPLS());
                                                  }
                                                  treeSize = currentBranch;
                                                  if (currentBranch + 1 == (nextFreeBranchNum)) {
                                                       break;
                                                  }
                                                  if (Debugging) {
                                                       virtDetailsOut.println();
                                                  }
                                                  if (NBifs == MaxBifs) {
                                                       doesExplodeTree = true;
                                                       doesExplodeGroup = true;
                                                       explodeCount++;
                                                       break;
                                                  }

                                             }
                                             if (Debugging) {
                                                  System.out.println("  NBifs: " + NBifs);
                                                  virtBifsOutFile.println(" , " + NBifs);
                                                  if (currentTreeN % stemCount == 0) {
                                                       virtBifsOutFile.println(); // a space in between tree groups
                                                  }
                                             }

                                             if (doesExplodeTree) {

                                             }
                                             else {

                                                  virtExplodeArray[currentTreeN] = doesExplodeTree;

                                                  virtBifsArray[currentTreeN] = NBifs;
                                                  BifsSum = BifsSum + NBifs;

                                                  virtAsymArray[currentTreeN] = BiffLine.
                                                         getVirtualAsymetry(
                                                         virtTreesArray, treeSize);
                                                  if (virtAsymArray[currentTreeN] == -1) {
                                                       //       ++undefinedAsymCount;
                                                  }
                                                  else {
                                                       virtAsymSum += virtAsymArray[currentTreeN];
                                                  }
                                                  virtSurfaceArray[currentTreeN] = BiffLine.
                                                         getVirtualSurface(
                                                         virtTreesArray, 1);

                                                  //              System.out.println(virtSurfaceArray[currentTreeN]);
                                                  virtSurfaceSum += virtSurfaceArray[currentTreeN];

                                                  virtSurfaceAsymArray[currentTreeN] = BiffLine.
                                                         getVirtualSurfaceAsym(
                                                         virtTreesArray, treeSize);
                                                  if (virtSurfaceArray[currentTreeN] == -1) {
                                                       //        ++virtSurfaceAsymSum;
                                                  }
                                                  else {
                                                       virtSurfaceAsymSum += virtSurfaceArray[
                                                              currentTreeN];
                                                  }
                                             }
                                             //******************************************************************************
//******************************************************************************
//***********************Output SWC Files***************************************
//******************************************************************************
//******************************************************************************
                                                  if (SWCout) {

                                                       //swcOut :  output file stream for virtual swc files
                                                       FileOutputStream sod = new FileOutputStream(
                                                              "SWC_" +
                                                              outFile + ".group" + groupN + "tree" +
                                                              currentTreeN + ".swc");
                                                       BufferedOutputStream bossod = new
                                                              BufferedOutputStream(
                                                              sod);
                                                       PrintWriter swcOut = new PrintWriter(bossod);
                                                       switch (DRdeterminant) {
                                                            case 1:
                                                                 swcOut.print("# DR = PL;  ");
                                                                 break;
                                                            case 2:
                                                                 swcOut.print("# DR = RD;  ");
                                                                 break;
                                                            case 3:
                                                                 swcOut.print("# DR = BO;  ");
                                                                 break;
                                                            default:
                                                                 System.out.println(
                                                                        "switch error, value is : " +
                                                                        DRdeterminant);
                                                                 System.exit(44);
                                                       }
                                                       switch (PDRdeterminant) {
                                                            case 1:
                                                                 swcOut.print("PDR = PL;  ");
                                                                 break;
                                                            case 2:
                                                                 swcOut.print("PDR = RD;  ");
                                                                 break;
                                                            case 3:
                                                                 swcOut.print("PDR = BO;  ");
                                                                 break;
                                                            default:
                                                                 System.out.println(
                                                                        "switch error, value is : " +
                                                                        PDRdeterminant);
                                                                 System.exit(44);
                                                       }

                                                       switch (TRdeterminant) {
                                                            case 1:
                                                                 swcOut.print("TR = PL;  ");
                                                                 break;
                                                            case 2:
                                                                 swcOut.print("TR = RD;  ");
                                                                 break;
                                                            case 3:
                                                                 swcOut.print("TR = BO;  ");
                                                                 break;
                                                            default:
                                                                 System.out.println(
                                                                        "switch error, value is : " +
                                                                        TRdeterminant);
                                                                 System.exit(44);
                                                       }
                                                       switch (BPLdeterminant) {
                                                            case 1:
                                                                 swcOut.print("BPL = PL;  ");
                                                                 break;
                                                            case 2:
                                                                 swcOut.print("BPL = RD;  ");
                                                                 break;
                                                            case 3:
                                                                 swcOut.print("BPL = BO;  ");
                                                                 break;
                                                            default:
                                                                 System.out.println(
                                                                        "switch error, value is : " +
                                                                        BPLdeterminant);
                                                                 System.exit(44);
                                                       }
                                                       switch (BIFFdeterminant) {
                                                            case 1:
                                                                 swcOut.println("BIFF = PL");
                                                                 break;
                                                            case 2:
                                                                 swcOut.println("BIFF = RD");
                                                                 break;
                                                            case 3:
                                                                 swcOut.println("BIFF = BO");
                                                                 break;
                                                            default:
                                                                 System.out.println(
                                                                        "switch error, value is : " +
                                                                        BIFFdeterminant);
                                                                 System.exit(44);
                                                       }

                                                       swcOut.println("# Tree Number : " +
                                                              currentTreeN +
                                                              " out of: " + newTreeCount);

                                                       if (doesExplodeTree) {
                                                            swcOut.println("# Tree Explodes");
                                                       }
                                                       else {

                                                            swcOut.println("# Bifurcations: " +
                                                                   virtBifsArray[currentTreeN]);
                                                            swcOut.println("# Asymmetry: " +
                                                                   virtAsymArray[currentTreeN]);
                                                            swcOut.println("# Surface Area: " +
                                                                   virtSurfaceArray[currentTreeN]);
                                                            swcOut.println("# Surface Asymmetry: " +
                                                                   virtSurfaceAsymArray[
                                                                   currentTreeN]);
                                                            swcOut.println("#  ");
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                                                            if (DendroOut) {
                                                                 swcOut.println("1 1 0 0 0 0 -1");
                                                                 for (int i = 1; i <= treeSize; i++) {








                                                                 }
                                                            }
                                                            else {

                                                                 swcOut.println("1 1 0 0 0 0 -1");
                                                                 for (int i = 1; i <= treeSize; i++) {

                                                                      if (i == 1) {
                                                                           swcOut.println( ( (i * 2)) +
                                                                                  " 3 0 " +
                                                                                  virtTreesArray[i].
                                                                                  getStartPLS() +
                                                                                  " 0 " +
                                                                                  virtTreesArray[i].
                                                                                  getStartRAD() +
                                                                                  " " +
                                                                                  1);

                                                                      }
                                                                      else {

                                                                           swcOut.println( ( (i * 2)) +
                                                                                  " 3 0 " +
                                                                                  virtTreesArray[i].
                                                                                  getStartPLS() +
                                                                                  " 0 " +
                                                                                  virtTreesArray[i].
                                                                                  getStartRAD() +
                                                                                  " " +
                                                                                  ( (virtTreesArray[
                                                                                  i].
                                                                                  getParrentNum() *
                                                                                  2) +
                                                                                  1));
                                                                      }
                                                                      swcOut.println( ( (i * 2) + 1) +
                                                                             " 3 0 " +
                                                                             virtTreesArray[i].
                                                                             getEndPLS() +
                                                                             " 0 " +
                                                                             virtTreesArray[i].
                                                                             getEndRAD() +
                                                                             " " +
                                                                             ( (i * 2)));

                                                                 }
                                                            }
                                                       }
                                                       swcOut.close();
                                                  }

//******************************************************************************
//******************************************************************************


                                        }
                                        if (doesExplodeGroup) {
                                             System.out.println(explodeCount + " out of " +
                                                    newTreeCount +
                                                    " virtual trees exploded.");
                                        }
                                        //
                                        //compute mean and stdev by cell group
                                        int[] groupExplodes = new int[nToDo + 1];
                                        double[] groupMeans = new double[nToDo + 1];
                                        double[] groupSums = new double[nToDo + 1];
                                        double[] groupStdevs = new double[nToDo + 1];
                                        double[] groupSqSums = new double[nToDo + 1];
                                        double meanOfGroupMeans = 0;
                                        double sumOfGroupMeans = 0;
                                        double sumOfGroupMeanSqSums = 0;
                                        double meanOfGroupStdevs = 0;
                                        double sumOfGroupStdevs = 0;
                                        double stdevOfGroupMeans = 0;
                                        double stdevOfGroupStdevs = 0;
                                        double sumOfGroupStdevSqSums = 0;

                                        int[] GroupUndefinedAsymCount = new int[nToDo + 1];
                                        double[] groupAsymMeans = new double[nToDo + 1];
                                        double[] groupAsymSums = new double[nToDo + 1];
                                        double[] groupAsymStdevs = new double[nToDo + 1];
                                        double[] groupAsymSqSums = new double[nToDo + 1];
                                        double meanAsymOfGroupMeans = 0;
                                        double sumAsymOfGroupMeans = 0;
                                        double sumAsymOfGroupMeanSqSums = 0;
                                        double meanAsymOfGroupStdevs = 0;
                                        double sumAsymOfGroupStdevs = 0;
                                        double stdevAsymOfGroupMeans = 0;
                                        double stdevAsymOfGroupStdevs = 0;
                                        double sumAsymOfGroupStdevSqSums = 0;

                                        double[] groupSurfaceMeans = new double[nToDo + 1];
                                        double[] groupSurfaceSums = new double[nToDo + 1];
                                        double[] groupSurfaceStdevs = new double[nToDo + 1];
                                        double[] groupSurfaceSqSums = new double[nToDo + 1];
                                        double meanSurfaceOfGroupMeans = 0;
                                        double sumSurfaceOfGroupMeans = 0;
                                        double sumSurfaceOfGroupMeanSqSums = 0;
                                        double meanSurfaceOfGroupStdevs = 0;
                                        double sumSurfaceOfGroupStdevs = 0;
                                        double stdevSurfaceOfGroupMeans = 0;
                                        double stdevSurfaceOfGroupStdevs = 0;
                                        double sumSurfaceOfGroupStdevSqSums = 0;

                                        int[] GroupUndefinedSurfaceAsymCount = new int[nToDo + 1];
                                        double[] groupSurfaceAsymMeans = new double[nToDo + 1];
                                        double[] groupSurfaceAsymSums = new double[nToDo + 1];
                                        double[] groupSurfaceAsymStdevs = new double[nToDo + 1];
                                        double[] groupSurfaceAsymSqSums = new double[nToDo + 1];
                                        double meanSurfaceAsymOfGroupMeans = 0;
                                        double sumSurfaceAsymOfGroupMeans = 0;
                                        double sumSurfaceAsymOfGroupMeanSqSums = 0;
                                        double meanSurfaceAsymOfGroupStdevs = 0;
                                        double sumSurfaceAsymOfGroupStdevs = 0;
                                        double stdevSurfaceAsymOfGroupMeans = 0;
                                        double stdevSurfaceAsymOfGroupStdevs = 0;
                                        double sumSurfaceAsymOfGroupStdevSqSums = 0;

                                        for (int i = 1; i <= nToDo; i++) {
                                             //for each virtual tree group
                                             groupExplodes[i] = 0;
                                             GroupUndefinedSurfaceAsymCount[i] = 0;
                                             GroupUndefinedAsymCount[i] = 0;

                                             for (int j = 1; j <= stemCount; j++) {
                                                  //for each virtual tree in the grouip
                                                  int currentTree = ( (j - 1) * nToDo) + i;
                                                  //get the tree number
                                                  if (virtExplodeArray[currentTree]) {
                                                       //keep track of individual tree esplosions
                                                       groupExplodes[i]++;
                                                  }
                                                  else {
                                                       //if tree doesn't explode, add emergent prop values to running group sums
                                                       groupSums[i] += virtBifsArray[currentTree];
                                                       groupSurfaceSums[i] += virtSurfaceArray[
                                                              currentTree];
                                                       if (virtAsymArray[currentTree] == -1) {
                                                            //if asym is undefined, incriment count
                                                            ++GroupUndefinedAsymCount[i];
                                                       }
                                                       else {
                                                            //otherwise add value to sum
                                                            groupAsymSums[i] += virtAsymArray[
                                                                   currentTree];
                                                       }
                                                       if (virtSurfaceAsymArray[currentTree] == -1) {
                                                            //if surface asym is undefined, incriment count
                                                            ++GroupUndefinedSurfaceAsymCount[i];
                                                       }
                                                       else {
                                                            groupSurfaceAsymSums[i] +=
                                                                   virtSurfaceAsymArray[
                                                                   currentTree];
                                                       }
                                                  }
                                             }

                                             //if all trees in group explode set means to undefined (-1)
                                             if (groupExplodes[i] == stemCount) {
                                                  groupMeans[i] = -1;
                                                  groupAsymMeans[i] = -1;
                                                  groupSurfaceMeans[i] = -1;
                                                  groupSurfaceAsymMeans[i] = -1;
                                             }
                                             //otherwise compute means
                                             else {
                                                  groupMeans[i] = groupSums[i] /
                                                         (stemCount - groupExplodes[i]);

                                                  if (stemCount >
                                                         (groupExplodes[i] +
                                                         GroupUndefinedAsymCount[i])) {
                                                       //if there are asym values which did not explode or return -1 compute the group mean
                                                       groupAsymMeans[i] = groupAsymSums[i] /
                                                              ( (stemCount - groupExplodes[i]) -
                                                              GroupUndefinedAsymCount[i]);
                                                  }
                                                  else {
                                                       //otherwise set group mean to undefined (-1)
                                                       groupAsymMeans[i] = -1;
                                                  }

                                                  groupSurfaceMeans[i] = groupSurfaceSums[i] /
                                                         (stemCount - groupExplodes[i]);
                                                  if (stemCount >
                                                         groupExplodes[i] +
                                                         GroupUndefinedSurfaceAsymCount[i]) {
                                                       //if there are Surf asym values which did not explode or return -1 compute the group mean
                                                       groupSurfaceAsymMeans[i] =
                                                              groupSurfaceAsymSums[i] /
                                                              ( (stemCount - groupExplodes[i]) -
                                                              GroupUndefinedSurfaceAsymCount[i]);
                                                  }
                                                  else {
                                                       //otherwise set group mean to undefined (-1)
                                                       groupSurfaceAsymMeans[i] = -1;
                                                  }
                                             }
                                        }

                                        int BifGroups = nToDo;
                                        int BifStdevGroups = nToDo;
                                        int AsymGroups = nToDo;
                                        int AsymStdevGroups = nToDo;
                                        int SurfaceGroups = nToDo;
                                        int SurfaceStdevGroups = nToDo;
                                        int SurfaceAsymGroups = nToDo;
                                        int SurfaceAsymStdevGroups = nToDo;

                                        //Compute group stdevs
                                        for (int i = 1; i <= nToDo; i++) {
                                             //set n's to stem counts
                                             int GroupBifs = stemCount;
                                             int GroupAsyms = stemCount;
                                             int GroupSurfaces = stemCount;
                                             int GroupSurfaceAsyms = stemCount;

                                             for (int j = 1; j <= stemCount; j++) {
                                                  int currentTree = ( (j - 1) * nToDo) + i;

                                                  if (virtExplodeArray[currentTree]) {
                                                       //if tree explodes, remove from stdev computation
                                                       --GroupBifs;
                                                       --GroupAsyms;
                                                       --GroupSurfaces;
                                                       --GroupSurfaceAsyms;
                                                  }
                                                  else {
                                                       if (groupMeans[i] == -1) {
                                                            //if group means undefined, decriment group counter
                                                            --BifGroups;
                                                       }
                                                       else {
                                                            //otherwise add to sqSum
                                                            groupSqSums[i] +=
                                                                   ( (virtBifsArray[currentTree] -
                                                                   groupMeans[i]) *
                                                                   (virtBifsArray[currentTree] -
                                                                   groupMeans[i]));
                                                       }
                                                       if (groupAsymMeans[i] == -1) {
                                                            //if group means undefined, decriment group n's
                                                            --AsymGroups;
                                                       }
                                                       else {
                                                            //add to sq sum
                                                            if (virtAsymArray[currentTree] == -1) {
                                                                 //if virt asym undefined, decriment group counter
                                                                 --GroupAsyms;
                                                            }
                                                            else {
                                                                 //otherwise add to sq sum
                                                                 groupAsymSqSums[i] +=
                                                                        ( (virtAsymArray[
                                                                        currentTree] -
                                                                        groupAsymMeans[i]) *
                                                                        (virtAsymArray[currentTree] -
                                                                        groupAsymMeans[i]));
                                                            }
                                                       }
                                                       if (groupSurfaceMeans[i] == -1) {
                                                            // if undefined, decriment group n's
                                                            --SurfaceGroups;
                                                       }
                                                       else {

                                                            groupSurfaceSqSums[i] +=
                                                                   ( (virtSurfaceArray[currentTree] -
                                                                   groupSurfaceMeans[i]) *
                                                                   (virtSurfaceArray[currentTree] -
                                                                   groupSurfaceMeans[i]));
                                                       }
                                                       if (groupSurfaceAsymMeans[i] == -1) {
                                                            //if group surf asym mean undevined, decriment number of groups
                                                            --SurfaceAsymGroups;
                                                       }
                                                       else {
                                                            if (virtSurfaceAsymArray[currentTree] ==
                                                                   -1) {
                                                                 //if virt surf asym undefined, decriment group counter
                                                                 --GroupAsyms;
                                                            }
                                                            else {
                                                                 //add tp surf sq sum
                                                                 groupSurfaceAsymSqSums[i] +=
                                                                        ( (virtSurfaceAsymArray[
                                                                        currentTree] -
                                                                        groupSurfaceAsymMeans[i]) *
                                                                        (virtSurfaceAsymArray[
                                                                        currentTree] -
                                                                        groupSurfaceAsymMeans[i]));
                                                            }
                                                       }
                                                  }
                                             }
//////////////

                                             if (GroupBifs >= 2) {
                                                  //if there are enough group bif values, compute stdev
                                                  groupStdevs[i] = java.lang.Math.sqrt(groupSqSums[
                                                         i] /
                                                         ( (GroupBifs) - 1));
                                             }
                                             else {
                                                  //else, set group stdev to -1
                                                  groupStdevs[i] = -1;
                                             }

                                             if (groupMeans[i] == -1) {
                                                  //  --BifGroups;, already done

                                             }
                                             else {
                                                  sumOfGroupMeans += groupMeans[i];
                                             }

                                             if (groupStdevs[i] == -1) {
                                                  --BifStdevGroups;
                                             }
                                             else {
                                                  sumOfGroupStdevs += groupStdevs[i];
                                             }
/////////////////////

                                             if (GroupAsyms >= 2) {
                                                  //if there are enough group asym values, compute stdev
                                                  groupAsymStdevs[i] = java.lang.Math.sqrt(
                                                         groupAsymSqSums[i] /
                                                         ( (stemCount - groupExplodes[i]) - 1));
                                             }
                                             else {
                                                  groupAsymStdevs[i] = -1;
                                             }
                                             if (groupAsymMeans[i] == -1) {
                                                  //decriment already done
                                             }
                                             else {
                                                  sumAsymOfGroupMeans += groupAsymMeans[i];
                                             }
                                             if (groupAsymStdevs[i] == -1) {
                                                  --AsymStdevGroups;
                                             }
                                             else {
                                                  sumAsymOfGroupStdevs += groupAsymStdevs[i];
                                             }
//////////////////////////////

                                             if (GroupSurfaces >= 2) {
                                                  groupSurfaceStdevs[i] = java.lang.Math.sqrt(
                                                         groupSurfaceSqSums[
                                                         i] / ( (stemCount - groupExplodes[i]) - 1));
                                             }
                                             else {
                                                  //else, set group stdev to -1
                                                  groupSurfaceStdevs[i] = -1;
                                             }

                                             if (groupSurfaceMeans[i] == -1) {
                                                  //decriment alreadly done
                                             }
                                             else {
                                                  sumSurfaceOfGroupMeans += groupSurfaceMeans[i];
                                             }
                                             if (groupSurfaceStdevs[i] == -1) {
                                                  --SurfaceStdevGroups;
                                             }
                                             else {
                                                  sumSurfaceOfGroupStdevs += groupSurfaceStdevs[i];
                                             }
//////////////
                                             if (GroupSurfaceAsyms >= 2) {
                                                  groupSurfaceAsymStdevs[i] = java.lang.Math.sqrt(
                                                         groupSurfaceAsymSqSums[i] /
                                                         ( (stemCount - groupExplodes[i]) - 1));
                                             }
                                             else {
                                                  //else, set group stdev to -1
                                                  groupSurfaceAsymStdevs[i] = -1;
                                             }

                                             if (groupSurfaceAsymMeans[i] == -1) {
                                                  //decriment alreadly done
                                             }
                                             else {
                                                  sumSurfaceAsymOfGroupMeans +=
                                                         groupSurfaceAsymMeans[i];
                                             }
                                             if (groupSurfaceAsymStdevs[i] == -1) {
                                                  --SurfaceAsymStdevGroups;
                                             }
                                             else {
                                                  sumSurfaceAsymOfGroupStdevs +=
                                                         groupSurfaceAsymStdevs[i];
                                             }

                                        } //compute global (group) means of means and stdevs
                                        if (BifGroups >= 1) {
                                             meanOfGroupMeans = sumOfGroupMeans / BifGroups;
                                        }
                                        else {
                                             meanOfGroupMeans = -1;
                                        }

                                        if (BifStdevGroups >= 1) {
                                             meanOfGroupStdevs = sumOfGroupStdevs / BifStdevGroups;
                                        }
                                        else {
                                             meanOfGroupStdevs = -1;
                                        }

                                        if (AsymGroups >= 1) {
                                             meanAsymOfGroupMeans = sumAsymOfGroupMeans /
                                                    AsymGroups;
                                        }
                                        else {
                                             meanAsymOfGroupMeans = -1;
                                        }

                                        if (AsymStdevGroups >= 1) {
                                             meanAsymOfGroupStdevs = sumAsymOfGroupStdevs /
                                                    AsymStdevGroups;
                                        }
                                        else {
                                             meanAsymOfGroupStdevs = -1;
                                        }

                                        if (SurfaceGroups >= 1) {
                                             meanSurfaceOfGroupMeans = sumSurfaceOfGroupMeans /
                                                    SurfaceGroups;
                                        }
                                        else {
                                             meanSurfaceOfGroupMeans = -1;
                                        }

                                        if (SurfaceStdevGroups >= 1) {
                                             meanSurfaceOfGroupStdevs = sumSurfaceOfGroupStdevs /
                                                    SurfaceStdevGroups;
                                        }
                                        else {
                                             meanSurfaceOfGroupStdevs = -1;
                                        }

                                        if (SurfaceAsymGroups >= 1) {
                                             meanSurfaceAsymOfGroupMeans =
                                                    sumSurfaceAsymOfGroupMeans /
                                                    SurfaceAsymGroups;
                                        }
                                        else {
                                             meanSurfaceAsymOfGroupMeans = -1;
                                        }

                                        if (SurfaceAsymStdevGroups >= 1) {
                                             meanSurfaceAsymOfGroupStdevs =
                                                    sumSurfaceAsymOfGroupStdevs /
                                                    SurfaceAsymStdevGroups;
                                        }
                                        else {
                                             meanSurfaceAsymOfGroupStdevs = -1;
                                        }
//Compute Stdevs of groups
                                        int NgroupBifsStdevs = nToDo;
                                        int NgroupAsymStdevs = nToDo;
                                        int NgroupSurfStdevs = nToDo;
                                        int NgroupSurfAsymStdevs = nToDo;

                                        int NgroupBifsMeans = nToDo;
                                        int NgroupAsymMeans = nToDo;
                                        int NgroupSurfMeans = nToDo;
                                        int NgroupSurfAsymMeans = nToDo;

/////////////////Bifs Stdevs
                                        if (meanOfGroupStdevs == -1) {
                                             stdevOfGroupStdevs = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupStdevs[i] == -1) {
                                                       --NgroupBifsStdevs;
                                                  }
                                                  else {
                                                       sumOfGroupStdevSqSums +=
                                                              ( (groupStdevs[i] - meanOfGroupStdevs) *
                                                              (groupStdevs[i] - meanOfGroupStdevs));
                                                  }
                                             }
                                             if (NgroupBifsStdevs > 1) {
                                                  stdevOfGroupStdevs = java.lang.Math.sqrt(
                                                         sumOfGroupStdevSqSums /
                                                         (NgroupBifsStdevs - 1));
                                             }
                                             else {
                                                  stdevOfGroupStdevs = -1;
                                             }

                                        }
////////////////////Asym Stdevs
                                        if (meanAsymOfGroupStdevs == -1) {
                                             stdevAsymOfGroupStdevs = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupAsymStdevs[i] == -1) {
                                                       --NgroupAsymStdevs;
                                                  }
                                                  else {
                                                       sumAsymOfGroupStdevSqSums +=
                                                              ( (groupAsymStdevs[i] -
                                                              meanAsymOfGroupStdevs) *
                                                              (groupAsymStdevs[i] -
                                                              meanAsymOfGroupStdevs));
                                                  }
                                             }
                                             if (NgroupAsymStdevs > 1) {
                                                  stdevAsymOfGroupStdevs = java.lang.Math.sqrt(
                                                         sumAsymOfGroupStdevSqSums /
                                                         (NgroupAsymStdevs - 1));
                                             }
                                             else {
                                                  stdevAsymOfGroupStdevs = -1;
                                             }

                                        }
////////////////////Surf Stdevs
                                        if (meanSurfaceOfGroupStdevs == -1) {
                                             stdevSurfaceOfGroupStdevs = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupSurfaceStdevs[i] == -1) {
                                                       --NgroupSurfStdevs;
                                                  }
                                                  else {
                                                       sumSurfaceOfGroupStdevSqSums +=
                                                              ( (groupSurfaceStdevs[i] -
                                                              meanSurfaceOfGroupStdevs) *
                                                              (groupSurfaceStdevs[i] -
                                                              meanSurfaceOfGroupStdevs));
                                                  }
                                             }
                                             if (NgroupSurfStdevs > 1) {
                                                  stdevSurfaceOfGroupStdevs = java.lang.Math.sqrt(
                                                         sumSurfaceOfGroupStdevSqSums /
                                                         (NgroupSurfStdevs - 1));
                                             }
                                             else {
                                                  stdevSurfaceOfGroupStdevs = -1;
                                             }

                                        }
////////////////////SurfAsym Stdevs
                                        if (meanSurfaceAsymOfGroupStdevs == -1) {
                                             stdevSurfaceAsymOfGroupStdevs = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupSurfaceAsymStdevs[i] == -1) {
                                                       --NgroupSurfAsymStdevs;
                                                  }
                                                  else {
                                                       sumSurfaceAsymOfGroupStdevSqSums +=
                                                              ( (groupSurfaceAsymStdevs[i] -
                                                              meanSurfaceAsymOfGroupStdevs) *
                                                              (groupSurfaceAsymStdevs[i] -
                                                              meanSurfaceAsymOfGroupStdevs));
                                                  }
                                             }
                                             if (NgroupSurfAsymStdevs > 1) {
                                                  stdevSurfaceAsymOfGroupStdevs = java.lang.Math.
                                                         sqrt(
                                                         sumSurfaceAsymOfGroupStdevSqSums /
                                                         (NgroupSurfAsymStdevs - 1));
                                             }
                                             else {
                                                  stdevSurfaceAsymOfGroupStdevs = -1;
                                             }

                                        }

/////////////////Bifs Means
                                        if (meanOfGroupMeans == -1) {
                                             stdevOfGroupMeans = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupMeans[i] == -1) {
                                                       --NgroupBifsMeans;
                                                  }
                                                  else {
                                                       sumOfGroupMeanSqSums +=
                                                              ( (groupMeans[i] - meanOfGroupMeans) *
                                                              (groupMeans[i] - meanOfGroupMeans));
                                                  }
                                             }
                                             if (NgroupBifsMeans > 1) {
                                                  stdevOfGroupMeans = java.lang.Math.sqrt(
                                                         sumOfGroupMeanSqSums /
                                                         (NgroupBifsMeans - 1));
                                             }
                                             else {
                                                  stdevOfGroupMeans = -1;
                                             }

                                        }
////////////////////Asym Means
                                        if (meanAsymOfGroupMeans == -1) {
                                             stdevAsymOfGroupMeans = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupAsymMeans[i] == -1) {
                                                       --NgroupAsymMeans;
                                                  }
                                                  else {
                                                       sumAsymOfGroupMeanSqSums +=
                                                              ( (groupAsymMeans[i] -
                                                              meanAsymOfGroupMeans) *
                                                              (groupAsymMeans[i] -
                                                              meanAsymOfGroupMeans));
                                                  }
                                             }
                                             if (NgroupAsymMeans > 1) {
                                                  stdevAsymOfGroupMeans = java.lang.Math.sqrt(
                                                         sumAsymOfGroupMeanSqSums /
                                                         (NgroupAsymMeans - 1));
                                             }
                                             else {
                                                  stdevAsymOfGroupMeans = -1;
                                             }

                                        }
////////////////////Surf Means
                                        if (meanSurfaceOfGroupMeans == -1) {
                                             stdevSurfaceOfGroupMeans = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupSurfaceMeans[i] == -1) {
                                                       --NgroupSurfMeans;
                                                  }
                                                  else {
                                                       sumSurfaceOfGroupMeanSqSums +=
                                                              ( (groupSurfaceMeans[i] -
                                                              meanSurfaceOfGroupMeans) *
                                                              (groupSurfaceMeans[i] -
                                                              meanSurfaceOfGroupMeans));
                                                  }
                                             }
                                             if (NgroupSurfMeans > 1) {
                                                  stdevSurfaceOfGroupMeans = java.lang.Math.sqrt(
                                                         sumSurfaceOfGroupMeanSqSums /
                                                         (NgroupSurfMeans - 1));
                                             }
                                             else {
                                                  stdevSurfaceOfGroupStdevs = -1;
                                             }

                                        }
////////////////////SurfAsym Means
                                        if (meanSurfaceAsymOfGroupMeans == -1) {
                                             stdevSurfaceAsymOfGroupMeans = -1;
                                        }
                                        else {
                                             for (int i = 1; i <= nToDo; i++) {
                                                  if (groupSurfaceAsymMeans[i] == -1) {
                                                       --NgroupSurfAsymMeans;
                                                  }
                                                  else {
                                                       sumSurfaceAsymOfGroupMeanSqSums +=
                                                              ( (groupSurfaceAsymMeans[i] -
                                                              meanSurfaceAsymOfGroupMeans) *
                                                              (groupSurfaceAsymMeans[i] -
                                                              meanSurfaceAsymOfGroupMeans));
                                                  }
                                             }
                                             if (NgroupSurfAsymMeans > 1) {
                                                  stdevSurfaceAsymOfGroupMeans = java.lang.Math.
                                                         sqrt(
                                                         sumSurfaceAsymOfGroupMeanSqSums /
                                                         (NgroupSurfAsymMeans - 1));
                                             }
                                             else {
                                                  stdevSurfaceAsymOfGroupMeans = -1;
                                             }

                                        }

////////////////////Printing of virt emergent stuff




                                        System.out.println("Mean and Stdev of Means = " +
                                               meanOfGroupMeans +
                                               " +- " + stdevOfGroupMeans + "   , of Stdevs = " +
                                               meanOfGroupStdevs + " +- " + stdevOfGroupStdevs);
                                        virtBifsOutFile.println(meanOfGroupMeans + " , " +
                                               stdevOfGroupMeans +
                                               " ," + explodeCount + ", " + meanOfGroupStdevs +
                                               " , " +
                                               stdevOfGroupStdevs);

                                        System.out.println("Mean and Stdev of Asyms = " +
                                               meanAsymOfGroupMeans +
                                               " +- " + stdevAsymOfGroupMeans +
                                               "   , of Stdevs = " +
                                               meanAsymOfGroupStdevs + " +- " +
                                               stdevAsymOfGroupStdevs);
                                        virtAsymetryFile.println(meanAsymOfGroupMeans + " , " +
                                               stdevAsymOfGroupMeans +
                                               " ," + explodeCount + ", " + meanAsymOfGroupStdevs +
                                               " , " +
                                               stdevAsymOfGroupStdevs);

                                        System.out.println("Mean and Stdev of Surfaces = " +
                                               meanSurfaceOfGroupMeans +
                                               " +- " + stdevSurfaceOfGroupMeans +
                                               "   , of Stdevs = " +
                                               meanSurfaceOfGroupStdevs + " +- " +
                                               stdevSurfaceOfGroupStdevs);
                                        virtSurfaceFile.println(meanSurfaceOfGroupMeans + " , " +
                                               stdevSurfaceOfGroupMeans +
                                               " ," + explodeCount + ", " +
                                               meanSurfaceOfGroupStdevs + " , " +
                                               stdevSurfaceOfGroupStdevs);

                                        System.out.println("Mean and Stdev of Surface Asymetry = " +
                                               meanSurfaceAsymOfGroupMeans +
                                               " +- " + stdevSurfaceAsymOfGroupMeans +
                                               "   , of Stdevs = " +
                                               meanSurfaceAsymOfGroupStdevs + " +- " +
                                               stdevSurfaceAsymOfGroupStdevs);
                                        virtSurfaceAsymFile.println(meanSurfaceAsymOfGroupMeans +
                                               " , " +
                                               stdevSurfaceAsymOfGroupMeans +
                                               " ," + explodeCount + ", " +
                                               meanSurfaceAsymOfGroupStdevs +
                                               " , " +
                                               stdevSurfaceAsymOfGroupStdevs);

                                        if (BifsMismatch) {
                                             virtBifsOutFile.println("BifsMismatch");
                                        }
                                   }
                              }
                         }
                    }
               }
               //compute mean and stdev for real tree bifs and asymetry
               RealBifsSum = 0;
               RealBifsSqSum = 0;
               RealBifsMean = 0;
               RealBifsStdev = 0;

               int NrealAsyms = stemCount;
               RealAsymSum = 0;
               RealAsymSqSum = 0;
               RealAsymMean = 0;
               RealAsymStdev = 0;

               RealSurfaceSum = 0;
               RealSurfaceSqSum = 0;
               RealSurfaceMean = 0;
               RealSurfaceStdev = 0;

               int NrealSurfaceAsyms = stemCount;
               RealSurfaceAsymSum = 0;
               RealSurfaceAsymSqSum = 0;
               RealSurfaceAsymMean = 0;
               RealSurfaceAsymStdev = 0;

               for (int i = 1; i <= stemCount; i++) {
                    RealBifsSum = RealBifsSum + realTreeBifs[i];
                    if (realAsymetryArray[i] == -1) {
                         --NrealAsyms;
                    }
                    else {
                         RealAsymSum = RealAsymSum + realAsymetryArray[i];
                    }

                    RealSurfaceSum = RealSurfaceSum + realSurfaceArray[i];

                    if (realSurfaceAsymArray[i] == -1) {
                         --NrealSurfaceAsyms;
                    }
                    else {
                         RealSurfaceAsymSum = RealSurfaceAsymSum + realSurfaceAsymArray[i];
                    }
                    if (Debugging) {
                         virtBifsOutFile.println(", , , ," + realTreeBifs[i]);
                         virtAsymetryFile.println(", , , ," + realAsymetryArray[i]);
                         virtSurfaceFile.println(", , , ," + realSurfaceArray[i]);
                         virtSurfaceAsymFile.println(", , , ," + realSurfaceAsymArray[i]);
                    }
               }

               RealBifsMean = RealBifsSum / stemCount;

               if (NrealAsyms > 0) {
                    RealAsymMean = RealAsymSum / NrealAsyms;
               }
               else {
                    RealAsymMean = -1;
               }

               RealSurfaceMean = RealSurfaceSum / stemCount;
//System.out.println("lll   "+RealSurfaceMean);
               if (NrealSurfaceAsyms > 0) {
                    RealSurfaceAsymMean = RealSurfaceAsymSum / NrealSurfaceAsyms;
               }
               else {
                    RealSurfaceAsymMean = -1;
               }

               RealSurfaceAsymMean = RealSurfaceAsymSum / stemCount;
//compute real emergent param stdevs

               for (int i = 1; i <= stemCount; i++) {
                    RealBifsSqSum = RealBifsSqSum + ( (realTreeBifs[i] - RealBifsMean) *
                           (realTreeBifs[i] - RealBifsMean));

                    RealSurfaceSqSum = RealSurfaceSqSum +
                           ( (realSurfaceArray[i] - RealSurfaceMean) *
                            (realSurfaceArray[i] - RealSurfaceMean));
               }
               RealBifsStdev = java.lang.Math.sqrt(RealBifsSqSum / (stemCount - 1));
               RealSurfaceStdev = java.lang.Math.sqrt(RealSurfaceSqSum / (stemCount - 1));

               if (RealAsymMean == -1) {
                    RealAsymStdev = -1;
               }
               else {
                    for (int i = 1; i <= stemCount; i++) {
                         if (realAsymetryArray[i] == -1) {
                              //   --NrealAsyms; already done
                         }
                         else {
                              RealAsymSqSum = RealAsymSqSum +
                                     ( (realAsymetryArray[i] - RealAsymMean) *
                                      (realAsymetryArray[i] - RealAsymMean));
                         }
                    }
                    if (NrealAsyms > 1) {
                         RealAsymStdev = java.lang.Math.sqrt(RealAsymSqSum / (NrealAsyms - 1));
                    }
                    else {
                         RealAsymStdev = -1;
                    }
               }

               if (RealSurfaceAsymMean == -1) {
                    RealSurfaceAsymStdev = -1;
               }
               else {
                    for (int i = 1; i <= stemCount; i++) {
                         if (realSurfaceAsymArray[i] == -1) {
                              //
                         }
                         else {
                              RealSurfaceAsymSqSum = RealSurfaceAsymSqSum +
                                     ( (realSurfaceAsymArray[i] - RealSurfaceAsymMean) *
                                      (realSurfaceAsymArray[i] - RealSurfaceAsymMean));
                         }
                    }
                    if (NrealSurfaceAsyms > 1) {
                         RealSurfaceAsymStdev = java.lang.Math.sqrt(RealSurfaceAsymSqSum /
                                (NrealSurfaceAsyms - 1));
                    }
                    else {
                         RealSurfaceAsymStdev = -1;
                    }
               }

               //RealBifsMean error check (as computed from tree bifs and individual bifs
               if (RealBifsMean != realBiffMean) {
                    System.out.println("Real bifs do not match: " + RealBifsMean +
                                       " vs " +
                                       realBiffMean);
                    //             System.exit(47);
                    BifsMismatch = true;
               }

               System.out.println("  Real Biff Mean and Stdev =" + RealBifsMean +
                                  " +- " +
                                  RealBifsStdev);

               System.out.println("  Real Asym Mean and Stdev =" + RealAsymMean +
                                  " +- " +
                                  RealAsymStdev);
               System.out.println("  Real Surface Mean and Stdev =" + RealSurfaceMean +
                                  " +- " +
                                  RealSurfaceStdev);
               System.out.println("  Real SurfaceAsym Mean and Stdev =" + RealSurfaceAsymMean +
                                  " +- " +
                                  RealSurfaceAsymStdev);

               virtBifsOutFile.println(" , , , Real Biff Mean, Stdev ," + RealBifsMean +
                                       " , " +
                                       RealBifsStdev);
               virtAsymetryFile.println(" , , , Real Asym Mean, Stdev ," + RealAsymMean +
                                        " , " +
                                        RealAsymStdev);
               virtSurfaceFile.println(" , , , Real Surface Mean, Stdev ," + RealSurfaceMean +
                                       " , " +
                                       RealSurfaceStdev);
               virtSurfaceAsymFile.println(" , , , Real Surface Asym Mean, Stdev ," +
                                           RealSurfaceAsymMean +
                                           " , " +
                                           RealSurfaceAsymStdev);

               virtBifsOutFile.close();
               virtDetailsOut.close();
               virtAsymetryFile.close();
               virtSurfaceFile.close();
               virtSurfaceAsymFile.close();

          }

          catch (IOException e) {
               System.out.println("error:  " + e.getMessage());
               System.out.println("Hit enter.");
               char character;
               try {
                    character = (char) System.in.read();
               }
               catch (IOException f) {}
          }
     }
}
