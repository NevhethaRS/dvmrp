
rm -f lan*.txt
rm -f hout*.txt
rm -f hin*.txt
rm -f rout*.txt
javac host.java
javac controller.java
javac router.java
java host host 0 0 sender 50 20 &
java host host 1 1 receiver &
java host host 2 2 receiver &
java router router 0 0 1 &
java router router 1 1 2 &
java router router 2 2 3 &
java router router 3 3 0 &
java controller controller host 0 1 2 router 0 1 2 3 lan 0 1 2 3 &

