export JAVA_HOME=$(PWD)/bellsoft-liberica-vm-core-openjdk21-23.1.2/Contents/Home

bellsoft-liberica-vm-core-openjdk21-23.1.2:
	curl -LO https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz

clean:
	rm -rf vizlink/target

vizlink/target/vizlink: $(shell find vizlink -type f -name '*.java')
	mvn install -f vizlink/pom.xml
	mvn -f vizlink/pom.xml -Pnative -DskipTests package