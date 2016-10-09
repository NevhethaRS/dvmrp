rm -f lan*.txt
rm -f hout*.txt
rm -f hin*.txt
rm -f rout*.txt
javac host.java
javac controller.java
javac router.java
java router router 0 0 1 &
java router router 1 1 2 &
java router router 2 2 3 &
java router router 3 3 0 &
java controller controller host router 0 1 2 3 lan 0 1 2 3 &

