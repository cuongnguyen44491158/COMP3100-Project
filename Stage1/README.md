The Final .java file for Stage1 is stored in FINAL folder

copy all necessary files into folder (e.g test files)
## How to run a Stage1
`make all ...`

`make clean`

## Report Link
https://docs.google.com/document/d/1Pj1iP13A2SU3ama2_NGRJ2aCSSYgoK-aY7f959qMm5I/edit

---

## Overview
ds-sim is a discrete-event simulator that has been developed primarily for leveraging scheduling algorithm design. It adopts a minimalist design explicitly taking into account modularity in that it uses the client-server model. The client-side simulator acts as a job scheduler while the server-side simulator simulates everything else including users (job submissions) and servers (job execution).

---
## How to run a simulation
1. run server `$ ds-server [OPTION]...`
2. run client `$ ds-client [-a algorithm] [OPTION]...`

## Usage
`$ ds-server -c ds-config01.xml -v brief`

`$ ds-client -a bf`

