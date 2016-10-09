ABOUT
=====
This folder includes the implementation of DVMRP in java. Each of the host,router and controller is programmed as separate java files.
The first data follows the truncated RPB, wherein all the routers get the data and the leaf lan’s don’t get the data.
Following the first message any further messages from the source to the receiver follows Reverse Path Forwarding, wherein the leaf routers send NMR, thus helping to prune the unwanted lans and other routers for the messages from the particular source. NMR sent is being tracked by the parent router of the particular lan, which sends the message to the lan if NMR’s were not received from all its children routers for 20 secs(as it can be seen in the last scenario6) .

Included files:
================
*Java code for router, controller and host
*Edited scenario files according to the program requirement
*Scenario output highlighted Screenshots
	Each of the sub-directories contain the screenshots of respective scenarios.
* A readme file

=====================================================================================
The code was developed in java and it requires the parameters specified in the correct format.

To run the program:
===================
*Copy all the files to the same directory
*Switch to that directory as the present working directory
*Execute the test.sh files as 
	./test1.sh
*For scenario6, execute test6.sh and execute the following command after 40 seconds
 	java host host 1 2 receiver &
*To change the file permissions,execute the following command
	chmod +x <file_name>

To test other scenarios:
========================
Create a bash file.
*To remove the files created during the previous run
	rm -f lan*.txt
	rm -f hout*.txt
	rm -f hin*.txt
	rm -f rout*.txt

*Compile the .java files
	javac host.java
	javac controller.java
	javac router.java

Separate java files are created for hosts,routers and controllers and hence,

*To add a host, include ‘java host’ before the command line arguments
	java host host 0 0 sender 50 20 &
	java host host 1 1 receiver &

*To add a router, include ‘java router’ before its arguments.
	java router router 0 0 1 &

*To add a controller, include ‘java controller’ before its arguments.
	java controller controller host 0 1 router 0 1 2 3 lan 0 1 2 3 &

*Even if no hosts exist, give ‘host’ in the arguments for the controller

<‘&’ is to be separated by a space from rest of the arguments>


