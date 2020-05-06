CC=javac
JAR=jar
EXE=java
TAR_FILE=COS341_u18171185_P00.tar
TAR_DIR=..
MAIN=Lexer
BIN=./bin
JAR_FILE=COS341_u18171185_P00.jar
MANIFEST_FILE=Manifest.txt
MANIFEST_CONTENT="Main-Class: ${MAIN}"
INPUT_FILE=example_input.txt
OUTPUT_FILE=example_output.txt

make: *.java
	$(CC) *.java

jar: make
	echo ${MANIFEST_CONTENT} > ${MANIFEST_FILE}
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 
	mkdir ${BIN}
	mv $(JAR_FILE) ${BIN} 
	cp -n $(INPUT_FILE) $(OUTPUT_FILE) ${BIN} 2>/dev/null

run: 
	$(EXE) $(MAIN) $(INPUT_FILE)

exec:
	${EXE} -${JAR} ${BIN}/${JAR_FILE} ${BIN}/$(INPUT_FILE)

clean:
	-rm -f *.class $(OUTPUT_FILE) *_output.txt Manifest.txt

wipe: clean
	-rm -f -r ${BIN}

rs:
	-rm -f $(OUTPUT_FILE) ${BIN}/${JAR_FILE}

again: make
	clear
	make run

tar:
	tar -cvz makefile ReadMe.txt *.java bin -f ${TAR_DIR}/${TAR_FILE}