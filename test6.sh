rm -f lan*.txt
rm -f hout*.txt
rm -f hin*.txt
rm -f rout*.txt
javac host.java
javac controller.java
javac router.java
java host host 0 0 sender 20 20 &
java router router 0 0 1 &
java router router 1 1 2 &
java controller controller host 0 1 router 0 1 lan 0 1 2 &

