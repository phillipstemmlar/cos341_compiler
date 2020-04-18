CC=javac
JAR=jar
EXE=java
MAIN=Main
BIN=./bin
JAR_FILE=COS341.jar
MANIFEST_FILE=Manifest.txt
INPUT_FILE=input.txt
OUTPUT_FILE=output.txt

make: *.java
	$(CC) *.java

jar: make
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 
	cp $(INPUT_FILE) $(OUTPUT_FILE) ${BIN}
	mv $(JAR_FILE) ${BIN}

run: 
	$(EXE) $(MAIN) $(INPUT_FILE) $(OUTPUT_FILE)

exec:
	${EXE} -${JAR} ${BIN}/${JAR_FILE} ${BIN}/$(INPUT_FILE) ${BIN}/$(OUTPUT_FILE)

clean:
	-rm *.class $(OUTPUT_FILE)

wipe: clean
	-rm ${BIN}/*

again: make
	clear
	make run