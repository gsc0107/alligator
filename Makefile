include tools.mk config.mk
HOSTNAME=$(shell hostname)
XSD_SANDBOX=https://raw.github.com/lindenb/xsd-sandbox/master/schemas
LIST_PHONY_TARGETS=
.PHONY= ${LIST_PHONY_TARGETS} 

generated.dir=src/main/generated

LISTJAR=`cat classpath.txt| tr "\n" ":"`

ifeq "${HOSTNAME}" "hardyweinberg"

database.name=CARDIOSERVE
database.dir=$(dir $(firstword $(MAKEFILE_LIST)))tmpdb
java.proxy= -Dhttp.proxyHost=cache.ha.univ-nantes.fr  -Dhttp.proxyPort=3128 
else
glassfish.domain?=cardioserve
database.dir=${HOME}/db

endif
LIST_PHONY_TARGETS+= all
all: 
	echo "please, select one of ${LIST_PHONY_TARGETS}"


LIST_PHONY_TARGETS+= deploy 
deploy: webapp
	${asadmin} deploy --force dist/cardioserve.jar

LIST_PHONY_TARGETS+= webapp 
webapp: classpath.txt
	mkdir -p tmp/META-INF dist
	cp -r src/main/webapp/* tmp/
	mkdir -p tmp/WEB-INF/classes/META-INF tmp/WEB-INF/lib
	cp ./src/main/resources/persistence/persistence.server.xml tmp/WEB-INF/classes/META-INF/persistence.xml 
	${JAVAC} -cp $(LISTJAR)  \
		-d tmp/WEB-INF/classes \
		-sourcepath src/main/java:${generated.dir} \
		src/main/java/com/github/lindenb/cardioserve/go/GoService.java \
		`find src/main -name "ObjectFactory.java"`
	jar cvf dist/cardioserve.jar -C tmp .
	rm -rf tmp

LIST_PHONY_TARGETS+= test 
test:
	echo "connect 'jdbc:derby://localhost:1527/${database.name};user=admin;password=adminadmin;create=false'; select * from DISEASEONTOLOGY.TERM FETCH NEXT 10 ROWS ONLY;" |\
		${ij} 

LIST_PHONY_TARGETS+= load.databases 
load.databases : load.generif load.jensenlab load.go load.goa load.doid


LIST_PHONY_TARGETS+= load.generif
load.generif:
	mkdir -p tmp
	cat sql/database.in.sql sql/gene_rif.sql sql/database.out.sql | ${ij} 
	curl -s "http://projects.bioinformatics.northwestern.edu/do_rif/do_rif.human.txt" |\
		cut -d '	' -f1-5 |\
		awk -F '	' '{i++;printf("%d\t%s\n",i,$$0);}' > tmp/generif.tsv
	(cat sql/database.in.sql; echo "DELETE FROM GENERIF.ASSOCIATION;CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('GENERIF', 'ASSOCIATION', 'tmp/generif.tsv', '	', NULL, null,0); select * from GENERIF.ASSOCIATION FETCH NEXT 10 ROWS ONLY;" ;cat sql/database.out.sql;) | ${ij} 
	#rm -rf tmp


LIST_PHONY_TARGETS+= load.jensenlab
load.jensenlab:
	mkdir -p tmp
	cat sql/database.in.sql sql/jensenlab.sql sql/database.out.sql | ${ij} 
	curl -s "http://download.jensenlab.org/human_disease_textmining_filtered.tsv"|\
		awk -F '	' '{i++;printf("%d\t%s\n",i,$$0);}' > tmp/human_disease.tsv
	(cat sql/database.in.sql; echo "DELETE FROM JENSENLAB.ASSOCIATION;CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('JENSENLAB', 'ASSOCIATION', 'tmp/human_disease.tsv', '	', NULL, null,0); select * from JENSENLAB.ASSOCIATION FETCH NEXT 10 ROWS ONLY;" ;cat sql/database.out.sql;) | ${ij} 
	rm -rf tmp


LIST_PHONY_TARGETS+= load.doid
load.doid: create.doid.database classpath.txt doid.owl
	mkdir -p tmp/META-INF dist
	cp ./src/main/resources/persistence/persistence.local.xml tmp/META-INF/persistence.xml
	${JAVAC} -d tmp -cp $(LISTJAR) -sourcepath src/main/java:${generated.dir} src/main/java/com/github/lindenb/cardioserve/doid/LoadDoid.java `find src/main -name "ObjectFactory.java"`
	${JAR} cvf dist/loaddoid.jar -C tmp .
	java -Dhttp.nonProxyHosts=localhost -cp dist/loaddoid.jar:$(LISTJAR) \
		com.github.lindenb.cardioserve.doid.LoadDoid \
		-u "jdbc:derby://localhost:1527/${database.name};user=admin;password=adminadmin;create=false" \
		doid.owl
	rm -rf tmp

LIST_PHONY_TARGETS+= create.doid.database 
create.doid.database: create.database 
	cat sql/database.in.sql sql/doid.sql sql/database.out.sql |\
	${HOME}/package/glassfish3/javadb/bin/ij 

doid.owl: 
	curl  "http://www.berkeleybop.org/ontologies/$@" >  $@

LIST_PHONY_TARGETS+= load.goa
load.goa: classpath.txt 
	cat sql/database.in.sql sql/goa.sql sql/database.out.sql | ${HOME}/package/glassfish3/javadb/bin/ij 
	mkdir -p tmp/ dist
	${JAVAC} -d tmp -cp $(LISTJAR) -sourcepath src/main/java:${generated.dir} src/main/java/com/github/lindenb/cardioserve/goa/LoadGoa.java `find src/main -name "ObjectFactory.java"`	
	${JAR} cvf dist/loadgoa.jar -C tmp .
	curl -s "ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/HUMAN/gene_association.goa_human.gz" |\
		gunzip -c |\
		${JAVA} -cp dist/loadgoa.jar:$(LISTJAR)  generated.uk.ac.ebi.goa.Associations |\
		${JAVA} -cp dist/loadgoa.jar:$(LISTJAR)  \
			com.github.lindenb.cardioserve.goa.LoadGoa \
			-u "jdbc:derby://localhost:1527/${database.name};user=admin;password=adminadmin;create=false" 
		echo "done"
	
	
LIST_PHONY_TARGETS+= load.go 
load.go: create.go.database classpath.txt go.xml
	mkdir -p tmp/META-INF dist
	cp ./src/main/resources/persistence/persistence.local.xml tmp/META-INF/persistence.xml
	${JAVAC} -d tmp -cp $(LISTJAR) -sourcepath src/main/java:${generated.dir} src/main/java/com/github/lindenb/cardioserve/go/LoadGo.java `find src/main -name "ObjectFactory.java"`
	${JAR} cvf dist/loadgo.jar -C tmp .
	java -Dhttp.nonProxyHosts=localhost -cp dist/loadgo.jar:$(LISTJAR) \
		com.github.lindenb.cardioserve.go.LoadGo \
		-u "jdbc:derby://localhost:1527/${database.name};user=admin;password=adminadmin;create=false" \
		go.xml
	rm -rf tmp

go.xml: 
	curl  "http://archive.geneontology.org/latest-termdb/go_daily-termdb.rdf-xml.gz" |\
		gunzip -c | grep -v DOCTYPE >  $@


LIST_PHONY_TARGETS+= create.go.database 
create.go.database: create.database 
	cat sql/database.in.sql sql/go.sql sql/database.out.sql |\
	${HOME}/package/glassfish3/javadb/bin/ij 
	

classpath.txt:
	rm -f $@
	$(foreach B,\
		${glassfish.dir}/glassfish/modules/jersey-core.jar \
		${glassfish.dir}/glassfish/modules/javax.ejb.jar \
		${glassfish.dir}/javadb/lib/derbyclient.jar \
		${glassfish.dir}/javadb/lib/derby.jar \
		${glassfish.dir}/glassfish/modules/javax.persistence.jar \
		${glassfish.dir}/glassfish/modules/org.eclipse.persistence.antlr.jar \
		${glassfish.dir}/glassfish/modules/org.eclipse.persistence.jpa.jar \
		${glassfish.dir}/glassfish/modules/org.eclipse.persistence.core.jar \
		${glassfish.dir}/glassfish/modules/org.eclipse.persistence.asm.jar \
		,echo "$B" >> $@ ;)
	

LIST_PHONY_TARGETS+= start.domain 
start.domain:
	${asadmin} start-domain ${glassfish.domain}

LIST_PHONY_TARGETS+= restart.domain 
restart.domain:
	${asadmin} restart-domain ${glassfish.domain}

LIST_PHONY_TARGETS+= stop.domain 
stop.domain:
	${asadmin} stop-domain ${glassfish.domain}

LIST_PHONY_TARGETS+= create.database 
create.database : ${database.dir}/${database.name}
${database.dir}/${database.name}:
	echo "#creating database $@"
	echo "connect 'jdbc:derby:$@;create=true;user=admin;password=adminadmin';disconnect;" | ${ij} 

LIST_PHONY_TARGETS+= create.jdbc.connection.pool 
create.jdbc.connection.pool:
	-${asadmin} create-jdbc-connection-pool \
		--datasourceclassname org.apache.derby.jdbc.ClientDataSource \
		--restype javax.sql.XADataSource \
		--property "portNumber=1527:password=adminadmin:user=admin:serverName=localhost:databaseName=${database.name}" \
		  cardioserve

LIST_PHONY_TARGETS+= list.jdbc.connection.pools 
list.jdbc.connection.pools:
	${asadmin} list-jdbc-connection-pools

LIST_PHONY_TARGETS+= create.jdbc.resource 
create.jdbc.resource :
	-${asadmin}  create-jdbc-resource \
   		--connectionpoolid cardioserve \
   		"jdbc/cardioserve"

LIST_PHONY_TARGETS+= list.jdbc.resources 
list.jdbc.resources:
	${asadmin} list-jdbc-resources

LIST_PHONY_TARGETS+= start.database 
start.database: create.database
	mkdir -p ${database.dir} 
	${asadmin} start-database --dbhome ${database.dir}

LIST_PHONY_TARGETS+= stop.database 
stop.database:
	-${asadmin} stop-database

LIST_PHONY_TARGETS+= shutdown
shutdown: stop.domain stop.database

LIST_PHONY_TARGETS+= startup
startup: start.database start.domain deploy

LIST_PHONY_TARGETS+= startup.denovo
startup.denovo: \
			create.database \
			start.database \
			restart.domain \
			create.jdbc.connection.pool \
			create.jdbc.resource \
			load.databases
LIST_PHONY_TARGETS+= clean		
clean:


LIST_PHONY_TARGETS+= clean.all		
cean.all: shutdown
	rm -rf ${database.dir} ${generated.dir}

generate.classes:\
	${generated.dir}/generated/org/geneontology \
	${generated.dir}/generated/org/diseaseontology \
	${generated.dir}/generated/ebi/ac/uk/goa \
	${generated.dir}/generated/org/jensenlab \
	${generated.dir}/generated.edu.northwestern.bioinformatics.projects.do_rif \

${generated.dir}/generated/org/geneontology:
	mkdir -p ${generated.dir}
	${XJC} -extension -Xinject-code -d ${generated.dir} \
		 -b  "${XSD_SANDBOX}/bio/go/go.jxb" \
		"${XSD_SANDBOX}/bio/go/go.xsd"

${generated.dir}/generated/org/diseaseontology:
	mkdir -p ${generated.dir}
	${XJC} -extension -Xinject-code -d ${generated.dir} \
		 -b  "${XSD_SANDBOX}/bio/doid/doid.jxb" \
		"${XSD_SANDBOX}/bio/doid/doid.xsd"

${generated.dir}/generated/ebi/ac/uk/goa:
	mkdir -p ${generated.dir}
	${XJC} -extension -Xinject-code -d ${generated.dir} \
		 -b  "${XSD_SANDBOX}/bio/goa/goa.jxb" \
		"${XSD_SANDBOX}/bio/goa/goa.xsd"

${generated.dir}/generated/org/jensenlab:
	mkdir -p ${generated.dir}
	${XJC} -extension -Xinject-code -d ${generated.dir} \
		 -b  "${XSD_SANDBOX}/bio/jensenlab/jensenlab.jxb" \
		"${XSD_SANDBOX}/bio/jensenlab/jensenlab.xsd"

${generated.dir}/generated.edu.northwestern.bioinformatics.projects.do_rif:
	mkdir -p ${generated.dir}
		${XJC} -extension -Xinject-code -d ${generated.dir} \
			 -b  "${XSD_SANDBOX}/bio/do_rif/dorif.jxb" \
			"${XSD_SANDBOX}/bio/do_rif/dorif.xsd"


archive.tar.gz: 
	tar  cvfz $@ Makefile sql src

#echo "1 2" > jeter.txt ; echo "connect 'jdbc:derby://localhost:1527/GO;user=admin;password=adminadmin;create=true';set SCHEMA GENEONTOLOGY; drop table TEST;create  table GENEONTOLOGY.TEST(id1 int,id2 int); CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('GENEONTOLOGY', 'TEST', '/home/lindenb/src/cardioserve/jeter.txt', '       ', NULL, null,0);select * from test;drop table test; disconnect;" |~/packages/glassfish3/javadb/bin/ij
	#mkdir -p tmp/META-INF dist
	#cp ./src/main/resources/persistence/persistence.xml tmp/META-INF/persistence.xml
	#javac -cp ${glassfish.dir}/glassfish/modules/javax.persistence.jar -d tmp -sourcepath src/main/java src/main/java/com/github/lindenb/cardioserve/goa/LoadGoa.java
	#jar cvf dist/$@.jar -C tmp .
	#rm -rf tmp
	#java -cp dist/$@.jar:${glassfish.dir}/glassfish/modules/javax.persistence.jar:$(subst $(space),:,$(foreach S,antlr asm core jpa,${glassfish.dir}/glassfish/modules/org.eclipse.persistence.${S}.jar)):/home/lindenb/package/glassfish3/javadb/lib/derby.jar com.github.lindenb.cardioserve.goa.LoadGoa
	#java -cp dist/$@.jar:${glassfish.dir}/glassfish/modules/javax.persistence.jar:$(subst $(space),:,$(foreach S,antlr jpa core asm,${glassfish.dir}/glassfish/modules/org.eclipse.persistence.${S}.jar)):/home/lindenb/package/glassfish3/javadb/lib/derby.jar:/home/lindenb/package/glassfish3/javadb/lib/derbyclient.jar com.github.lindenb.cardioserve.goa.LoadGoa
