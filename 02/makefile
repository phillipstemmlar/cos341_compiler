CC=javac
JAR=jar
EXE=java
PROJECT_NAME=COS341_u18171185_P02
TAR_FILE=${PROJECT_NAME}.tar
TAR_DIR=..
TAR_INPUT=makefile ReadMe.txt scope_design.txt *.java
MAIN=Compiler
BIN=./bin
TestFileDir=./TestFiles
JAR_FILE=${PROJECT_NAME}.jar
MANIFEST_FILE=Manifest.txt
MANIFEST_CONTENT="Main-Class: ${MAIN}"
INPUT_FILE=${TestFileDir}/example_input1.txt
OUTPUT_FILE=*_output.txt Manifest.txt
m=Automated commit with MakeFile

make: *.java
	$(CC) *.java

jar: make
	echo ${MANIFEST_CONTENT} > ${MANIFEST_FILE}
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 
	mkdir -p ${BIN}
	mv $(JAR_FILE) ${BIN} 

run: 
	$(EXE) $(MAIN) $(INPUT_FILE)

exec:
	${EXE} -${JAR} ${BIN}/${JAR_FILE} $(INPUT_FILE)

clean:
	-rm -f *.class $(OUTPUT_FILE)

wipe: clean
	-rm -f -r ${BIN}

rs:
	-rm -f $(OUTPUT_FILE) ${BIN}/${JAR_FILE}

again: make
	clear
	make run

tar:
	tar -cvz ${TAR_INPUT} bin -f ${TAR_DIR}/${TAR_FILE}

git:
	git add ../
	git commit -m "$(m)"
	git push

status:
	git status;