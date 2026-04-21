#!/bin/bash

cd ..

class=Game

if [[ "$1" ]]; then
	class=$1
fi

if [[ "$class" =~ .*\.java ]]; then
	class=$(echo "$class" | cut -d. -f1)
fi

rm -r bin
mkdir -p bin

RED='\033[0;31m'
NC='\033[0m' # No Color
echo -e "${RED}"
echo "  ____ ___  __  __ ____ ___ _     ___ _   _  ____ "
echo " / ___/ _ \|  \/  |  _ \_ _| |   |_ _| \ | |/ ___|"
echo "| |  | | | | |\/| | |_) | | | |    | ||  \| | |  _ "
echo "| |__| |_| | |  | |  __/ | | |___ | || |\  | |_| |"
echo " \____\___/|_|  |_|_|   |___|_____|___|_| \_|\____|"
echo -e "${NC}"

# 2. Compile
javac -cp "bin:lib/*:." $class.java -d bin

echo -e "${RED}"
echo " ____  _   _ _   _ _   _ ___ _   _  ____ "
echo "|  _ \| | | | \ | | \ | |_ _| \ | |/ ___|"
echo "| |_) | | | |  \| |  \| || ||  \| | |  _ "
echo "|  _ <| |_| | |\  | |\  || || |\  | |_| |"
echo "|_| \_\\___/|_| \_|_| \_|___|_| \_|\____|"
echo -e "${NC}"

# 3. Run
java -cp "bin:lib/*:." $class
#!/bin/bash


