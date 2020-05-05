CC=javac
JAR=jar
EXE=java
TAR_FILE=COS341_u18171185_P00.tar
TAR_DIR=..
MAIN=Lexer
BIN=./bin
JAR_FILE=COS341_u18171185_P00.jar
MANIFEST_FILE=Manifest.txt
INPUT_FILE=example_input.txt
OUTPUT_FILE=example_output.txt

make: *.java
	$(CC) *.java

jar: make
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 
	mv $(JAR_FILE) ${BIN} 
	cp -n $(INPUT_FILE) $(OUTPUT_FILE) ${BIN} 2>/dev/null

run: 
	$(EXE) $(MAIN) $(INPUT_FILE) $(OUTPUT_FILE)

exec:
	${EXE} -${JAR} ${BIN}/${JAR_FILE} ${BIN}/$(INPUT_FILE) ${BIN}/$(OUTPUT_FILE)

clean:
	-rm -f *.class $(OUTPUT_FILE)

wipe: clean
	-rm -f ${BIN}/*

rs:
	-rm -f $(OUTPUT_FILE) ${BIN}/${JAR_FILE}

again: make
	clear
	make run

tar:
	tar -cvz makefile Manifest.txt ReadMe.txt *.java ${BIN}/${JAR_FILE} -f ${TAR_DIR}/${TAR_FILE}