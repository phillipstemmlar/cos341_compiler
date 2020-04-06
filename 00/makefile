CC=javac
JAR=jar
EXE=java

MAIN=Main
JAR_FILE=COS341.jar
MANIFEST_FILE=Manifest.txt
INPUT_FILE=input.txt

make: *.java
	$(CC) *.java

jar: make
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 

run: 
	$(EXE) $(MAIN) $(INPUT_FILE)

exec:
	${EXE} -${JAR} ${JAR_FILE} $(INPUT_FILE)

clean:
	-rm *.class

wipe: clean
	-rm *.jar

again: make
	make run