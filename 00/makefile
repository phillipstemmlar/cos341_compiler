CC=javac
EXE=java
MAIN=Main
SRC=./src
BIN=./bin
INPUT_FILE=input.txt

make: $(SRC)/*.java
	$(CC) -d $(BIN) $(SRC)/*.java
	cp $(SRC)/$(INPUT_FILE) $(BIN)

run: 
	$(EXE) -cp $(BIN) $(MAIN) $(INPUT_FILE)

clean:
	-rm $(BIN)/*.class $(BIN)/*.txt

again: make
	make run