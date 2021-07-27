# Biomass_Pertubation
Java script to perform wild-type simulation with a perturbation of a given coefficient of a biomass component. To perform *in silico* simulations, the Optflux 3.2.8 code (Rocha et al., 2010) was used.

### Description
This script performs a sensitivity analysis to the coefficients of biomass precursors and allows to evaluate the impact of small modifications in specific growth rate and flux distribution.
For a given factor, the tool changes the coefficient of each macromolecule or building block (one at a time) and compensates all other coefficients so that the biomass composition equates the same 1g. 

### Mandatory inputs: 
- a model in SBML format; 
- a file with all macromolecular components and percentages; 
- files with each individual macromolecular constituent and their percentage (i.e. Protein file contains amino acid content, percentage and molecular weight), 
- and one or more sensitivity factors (i.e. coefficient variation). 

### Outputs:
For each sensitivity factor a wild-type simulation is performed and the specific growth rate and the flux distribution of all reactions retrieved.

### Pre-requisites
Git and JDK 8 update 20 or later

### References
Rocha I. , Maia P., Evangelista P., Vilaça P., Soares S., Pinto J. P., Nielsen J., Patil K. R., Ferreira E. C., Rocha M. (2010) *OptFlux: an open-source software platform for in silico metabolic engineering*.BMC Systems Biology. 4:45.
