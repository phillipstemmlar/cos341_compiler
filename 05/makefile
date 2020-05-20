CC=javac
JAR=jar
EXE=java
PROJECT_NAME=COS341_u18171185_P04
TAR_FILE=${PROJECT_NAME}.tar
TAR_DIR=..
TAR_INPUT=makefile ReadMe.txt scope_design.txt *.java bin
MAIN=Compiler
BIN=./bin
TestFileDir=./TestFiles
BuildDir=./build
JAR_FILE=${PROJECT_NAME}.jar
MANIFEST_FILE=Manifest.txt
MANIFEST_CONTENT="Main-Class: ${MAIN}"
INPUT_FILE=${TestFileDir}/example_input.txt
BASIC_FILE=${BuildDir}/BASIC_FILE.B # BASIC_FILE=./Hello.b
OUTPUT_FILE=*_output.txt Manifest.txt
INTERP=../Interpreter/BASIC
m=Automated commit with MakeFile

make: *.java
	$(CC) *.java

jar: make
	echo ${MANIFEST_CONTENT} > ${MANIFEST_FILE}
	$(JAR) cmvf $(MANIFEST_FILE) $(JAR_FILE) *.class 
	mkdir -p ${BIN}
	mv $(JAR_FILE) ${BIN} 

run: 
	mkdir -p ${BuildDir}
	$(EXE) $(MAIN) $(INPUT_FILE) $(BASIC_FILE)

exec:
	mkdir -p ${BuildDir}
	${EXE} -${JAR} ${BIN}/${JAR_FILE} $(INPUT_FILE)

test: run
	${INTERP} ${BASIC_FILE}

clean:
	-rm -f *.class $(OUTPUT_FILE)

wipe: clean
	-rm -f -r ${BIN} ${BuildDir}

rs:
	-rm -f $(OUTPUT_FILE) ${BIN}/${JAR_FILE}

again: make
	clear
	make run

go: again
	clear
	make test

tar:
	tar -cvz ${TAR_INPUT} -f ${TAR_DIR}/${TAR_FILE}

git:
	git add ../
	git commit -m "$(m)"
	git push

status:
	git status;