# Project1-Distributed-Computing

## Autors
- [Alejandro Clavera Poza](https://github.com/alejandroclavera/)
- [Moises Bernaus Lechosa](https://github.com/MoisesBernaus/)
## Proyect setup intructions
This project it is building with maven tool to install the difference dependencies that protect needs to run.

To run a node whiout ide must be generate a jar file, todo this put the next command in the  root project directory.
``
mvn install
``
In the targt folder you can find de jar generated. To run a node you must put the next command.
``
# By default the port is 1099
java -jar <namer of jar> <port>
``

If are you using a IDE like intellij can build the project from maven windows -> Lifecycle -> install 

Another way to run the node, is if you're using IDE you can execute the node directly from the class NodeStarter.
