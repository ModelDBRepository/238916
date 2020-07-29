The zip file has the program to generate trees. There are 
243 variants that varies based on  which parameters
control which aspects of dendritic growth.

lnded2_0.class is the main file. It takes two comand line parameters, an input file
(the .prn parameter file) and an output file name. Program reads
the parameter file, reads and processes the swc files listed in the
parameter file, and generates new swc files.

Detailed Instruction:
Nanda S, Das R, Bhattacharjee S, Cox DN, Ascoli GA (2018) Morphological determinants of dendritic arborization neurons in Drosophila larva. Brain Struct Funct 223(3):1107–1120.

This models is a slight adaptation from the Donohue and Ascoli (2008) paper. 

Model descriptions and directions.

Overview:

The model is for creating virtual branching structures
based on 3D reconstructions of real neurons.  The model takes as input
a parameter file (FileList.prn) which gives the file path
of several neuronal reconstructions in .swc format.  Five basic
parameters are measured from each branch of the input neurons;
branching probability, taper rate, daughter ratio, parent daughter
ratio, and branch length.  They are then organized and reduced to
statistical distributions based on three fundamental determinants
(FDs); radius, path distance from the soma, and branch order.  The
distributions are then placed into a table file.  Morphometrics are
resampled from the tables based on the current virtual fundamental
determinant values to create virtual 2D branching structures.


Each basic parameter can be controlled
by a separate FD.  Given that there are 5 basic parameters and 3 FDs,
there are 5^3, or 243 unique variants of this model.  Note that the
three cases where all five parameters are under the control of the
same FD are equivalent to the three percent mix cases where one FD has
100% influence (although due to differences in random number
generation they are only statistically identical and will not provide
the exact same results).  In either case these are referred to as
"pure" models.

Running the program: 

The models are designed to be executed from the command line.  The
format (in mswin) is:

Java lnded2_0 inputFileName OutputFilePostfix

Where lnded2_0 is the .class file created from the .java files
included in each directory.  The input file is a text file with the
extension of .prn in our examples.  See the ExampleCommandLine.bat
file for a specific example. When the program is run a table file
prefixed "Table_" is created which gives the statistical distributions
of the basic parameters binned by FDs.  For each bin a distribution
type (Uniform (1), Gaussian (2), and Gamma (3)) is chosen based on a
least mean squares matching between the real data and generated data
of each distribution type.  An empty file is "OutputInfo_ " file is
also created.  If the "DEBUGGING" switch is set to "true" in the .prn
file (explained below) this "OutputInfo_" file will be filled with all
of the raw data used to create the table file.  Finally, four .csv
files are created beginning with "VirtAsymetry_", "VirtBifs_",
"VirtSurface_", and "VirtSurfaceAsym_".  These contain the group means
and standard deviations for bifurcation asymmetry, bifurcation
numbers, surface area, and surface area asymmetry of the virtual trees
for each model variant.  The mean and standard deviation of the given
emergent morphometric for the real input trees is included at the end
of the file.

.prn file options:

The following are the options which can be adjusted through the
parameter file.  For each option the value to be assigned is placed on
the following line of the .prm file.  Unless otherwise noted all
options apply to both model variants.

INPUT 
        Takes an integer value referring to the number of .swc files
	which are to be loaded.  On subsequent individual lines the
	full file path of each input files is given.  A large number
	of .swc files are available at NeuroMorpho.org.

TODO
	Takes an integer value giving the number of group of virtual
	trees to create.  If for example there were 12 input .swc
	files with three trees each and TODO is set to 10 (the default
	value) then 10 groups of 36 virtual trees will be created.
	The mean and standardizations given in the .csv files refer to
	theses group means and standardizations.

BINNING
	Takes an integer value giving the minimum number of points to
	be placed into each FD bin.  The default is 85.  It is
	important to note that this is the minimum and for those FDs
	which can be heavily discretized (branch order and radius)
	bins may contain many more data points than this.  For
	example, if there are 50 input branches of branch order 1, 75
	of branch order 2, and 130 of branch order three the first bin
	will contain 125 data points and the second 130 if the minimum
	is set at 85.

TYPETODO
	Takes an integer which specifies which tree type to be
	analyzed and recreated from the .swc file.  The default is 3
	(regular or basal dendrite).  4 (apical dendrite) and 2 (axon)
	are also accepted as is any custom type value.

MINRAD
	Takes a positive double value specifying the minimum allowed
	radius.  Because the model is stochastic it is possible to
	continue reducing the radius values of virtual trees beyond
	realistic boundaries.  This value specifies the radius values
	below which termination is not allowed.  Default is .15.

SEED
	Takes an integer and sets the random number seed value.
	Useful for doing multiple runs without getting the exact same
	results each time.

DEBUGGING
	Takes either "true" or "false".  Boolean which if true
	(default) populates the file prefixed with "OutputInfo_" with
	many details about the input .swc files parameter values.

SWCOUT
	Takes either "true" or "false".  Boolean which if true writes
	a separate .swc file for each virtual tree created for
	analysis by external programs.  Because no 2D or 3D
	information is included these files will appear as single
	lines in visualization software.  Default is false.

DENDRO
	Takes either "true" or "false".  Boolean which if true writes
	a separate .swc file for each virtual tree created in the form
	of a 2D dendrogram.  These files are useful for visualization
	and figures, but the lateral offset which separates the
	branches adds length to the trees which is not part of the
	model.  Default is false.

PERCENTSTEP
	not needed for this version of the model. Default in 0.1. 
