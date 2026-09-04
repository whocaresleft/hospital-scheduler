# Hospital Scheduler
Project for the AST exam at University of Florence, master's deegre in Software: Science and Technology. The application is a manager for doctor-department schedule.<br>
`N.B.` the file `report.pdf` is the same report that was submitted on Moodle, just under a different name.

## Cool Badges
[![Java CI with Maven in Linux](https://github.com/whocaresleft/hospital-scheduler/actions/workflows/ci-linux.yml/badge.svg)](https://github.com/whocaresleft/hospital-scheduler/actions/workflows/ci-linux.yml)
[![Mutation Testing (Night + Manual)](https://github.com/whocaresleft/hospital-scheduler/actions/workflows/night-mutation-testing.yml/badge.svg)](https://github.com/whocaresleft/hospital-scheduler/actions/workflows/night-mutation-testing.yml)
[![Coverage Status](https://coveralls.io/repos/github/whocaresleft/hospital-scheduler/badge.svg?branch=main)](https://coveralls.io/github/whocaresleft/hospital-scheduler?branch=main)
[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=coverage)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Reliability issues](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=software_quality_reliability_issues)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Security issues](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=software_quality_security_issues)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=bugs)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=whocaresleft_hospital-scheduler&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=whocaresleft_hospital-scheduler)

# Description
This project implements an application to manage Hospital shifts between doctors and hospital departments:
* A Doctor is an hospital worker (well, the name Doctor is a bit specific, it could be expanded in the future, the main idea started from just that), represented by an Id, first name, and last name;
* A Department is a section of the Hospital in which a Doctor could be found working (e.g. Emergency Room or Surgery Room), it's represented by an Id and a name;
* A Shift represents a time slot, within a specific day, in which a Doctor is scheduled to work in a certain Department (e.g. doc1-er, 26/08/2026, 08:00-15:00).

The application was developed with a MVP architecture with transaction support in mind. The overall stack is comprised of four layers: view, presentation, transactional, and data.<br>
The view layer consists of three views, one for each entity, defined as interfaces. The implementation of such views was done in Swing, as each view is a panel which can be integrated, for example, in a frame. This Swing implementation can also interact with the presentation layer, to issue commands.<br>
In the presentation layer there is a presenter for each entity. On one side, this layer interacts with the data layer (however this is done through the transactional layer) to perform CRUD operations regarding the corresponding entity. On the other side, it interacts with the view layer in order to update the view, and show the outcome of the performed operation. While each presenter only works with one view (because they both handle only one entity), its not entirely true that it works with a single repository only. The reason lies in the n-n relation between Doctors and Departments, which results in a third entity, Shift. The relation between Doctors, Shifts, and Departments is 1-n-1, this the following two must be considered:
* Deleting either a Doctor or Department requires also handling the corresponding Shifts (DoctorPresenter and DepartmentPresenter ALSO work with the Shift repository);
* Inserting (or updating) a Shift requires consistency with the currently present Doctors and Departments (ShiftPresenter works with all three repositories).

The transactional layer is what makes multi-repository operations possible, by encapsulating the presenter's operations in a transaction block (without the presenter needing to perform extra actions).<br>
The data layer is made up of the three repositories, one for each entity. These were defined as interfaces, and have an implementation both in MongoDB and MariaDB (this means the repositories are created to work with a MongoDB, or MariaDB, server respectively).<br>
<br>
The transactional layer as well was implemented both to work with MongoDB and MariaDB. As expected, a TransactionManager implemented for a particular database requires the repositories to be of that same "type" (as in, developed for the same database).

## Project Structure
This project is a multi-module Maven project, consisting of seven following sub-projects total: `bom`, `parent`, `model`, `presentation`, `aggregator`, `report`, and `app`, each with the `hospital-scheduler-` prefix.
* `bom`: Defines the Bill of Material by defining all the dependencies and locking the versions;
* `parent`: Defined the maven plugins with the versions and configurations;
* `model`: Contains the model entities as well as the repositories (data layer) and transaction managers (transactional layer), with the corresponding tests (both **unit** and **integration**);
* `presentation`: Contains the presenters (presentation layer) and the views (view layer), with the corresponging tests (both **unit** and **integration**);
* `aggregator`: Module used to build the overall project. It defines the modules to include in the build tree (`mvn` is run from here);
* `report`: Module used to aggregate the generated JaCoCo and Pitest reports, into a single html file (I mean one for JaCoCo and one for Pitest, if they are enabled);
* `app`: Contains the Main class (used to start the Swing application), as well as the **e2e** tests.

For a better explaination of the contents of the modules **model** and **presentation** it would be best to consult the `report.pdf` present in the repository.

## Dependencies
I highly suggest having **make** installed in order to use the given **Makefile**. In case **make** is already installed, it can be used to check the presence of the other dependencies.

### Build dependencies
In order to build this project locally, the following dependencies are required:
* **Java** (JDK), minimum version: **17**;
* **Docker**, daemon needs to be active and socket (**/var/run/docker.sock**) needs to be accessible (user needs to be in **docker** group);
* **xvfb**, in particular the command **xvfb-run**;
* **openbox**.

These can be checked by running `make dependencies-check-dev`.

### Run dependencies
In the goal is to just run the final application, the only required dependencies are **Docker compose** (which requires Docker) and **xhost**.

Again, these can be checked by running `make dependencies-check-run`.
Otherwise, if one is interested in both building and running the applications, ALL dependencies can be checked with `make dependencies-check-all`.

### Installing dependencies
#### Debian-based distributions
These can be installed using the package manage **apt**. The packages have the following names:
* **make**, `build-essentials`;
* **Java**, `openjdk-17-jdk`;
* **Docker**, `docker`;
* **xvfb**, `xvfb`;
* **openbox**, `openbox`.

* **Docker compose**, `docker-compose-plugin` (requires Docker);
* **xhost**, `x11-xserver-utils`.

#### Arch-based distributions
These can be installed using the package manager **pacman**. The packages have the following names:
* **make**, `make`;
* **Java**, `jdk17-openjdk`;
* **Docker**, `docker`;
* **xvfb**, `xorg-server-xvfb`;
* **openbox**, `openbox`.

* **Docker compose**, `docker compose` (requires Docker);
* **xhost**, `xorg-xhost`.

## Build (Linux)
The following sections give requirements and instructions to build the project on a Linux machine.
As I haven't tested building on MacOS or Windows, users running those OSes can refer to the section [Run](#run) for instructions on running the application.

Once dependencies are installed, **make** can also be used to build the project, and/or run the tests.<br>

To clean the project of all build generated files, run
```
make clean-all
```

To build the application (i.e. create the FatJar), run
```
make package
```
However, note that the former also runs all Unit tests beforehand. In case the goal is to just build the application without running any test, run
```
make package-only
```
### Running tests
Recall that `make package` also runs Unit tests before creating the FarJar. However, if we want to run all tests (i.e. Unit, Integration, and E2E), you can run
```
make run-coverage
```
this command runs all the tests, creating a JaCoCo report in the end, which can be found in the **target** folder of the `report` module. <br>
<br>
There are also two commands to run mutation tests (alongside UT, IT, and E2ET), the first one is
```
make run-mutation-light
```
which run mutation tests with **STRONGER** mutators on the following classes: `Id`, `Shift`, and `Presenters` (all three). However, there is another version, which ALSO runs mutation tests on the `Repositories` (all three, for both MongoDB and MariaDB implementations), the command is
```
make run-mutation-all
```
which is slower than the previous one, of course. There is also a final command, which runs both Code Coverage and all mutation tests (w.r.t the previous `-full`), which can be run with
```
make run-all
```

## Run
The application can be run in two different ways, those being on your local machine, or within a Docker container. In both cases i highly
suggest using **make**!!

### Run native
> [!NOTE]
> To run the application natevely, the [Build dependencies](#build-dependencies) need to be installed!

While typing the **make**, we can also specify the database to be used. As for now the only valid options are `mongo` and `maria`:
```
make native-build-and-run DB=mongo
```
and the counterpart
```
make native-build-and-run DB=maria
```
There is also a default form, without specifying the database
```
make native-build-and-run
```
This commands first try to build the application, creating a FatJar, then they start a docker container with the corresponding database. As a final
step, the application Jar is started natively (through `java -jar`). The output is silenced to not clutter the terminal.<br>
Once the application is no longer of interest, it can be closed using the window's **X** button, and to stop the container we can use
```
make native-stop
```
Alternatively, if the container need to be actually removed (alongside the volumes), we can use
```
make native-clean
```

### Run within Docker
> [!NOTE]
> Running within Docker is available for Linux only (with either X11 or Wayland with xorg installed)
If both **Docker compose** and **xhost** are installed, the application can be run within a Docker container using **make**. While typing the command, we can also specify the database to be used. As for now the only valid options are `mongo` and `maria`:
```
make docker-build-and-run DB=mongo
```
and the counterpart
```
make docker-build-and-run DB=maria
```
There is also a default form, without specifying the database
```
make docker-build-and-run
```
which uses `mongo` as default.<br>
Either command will start two containers, one with a database (either MongoDB or MariaDB), and one where the application
will be built and run.<br>
Once the application is no longer of interest, it can be closed using the window's **X** button, and to stop the containers we can use
```
make docker-stop
```
Alternatively, if the containers need to be actually removed (alongside the volumes), we can use
```
make docker-clean
```