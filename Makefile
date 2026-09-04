MVNW := ./mvnw
POM_PATH = hospital-scheduler-aggregator/pom.xml
JAVA_REQUIRED := 17

dependencies-check-dev:
	@echo "Checking building dependencies:"
	@if command -v java >/dev/null 2>&1; then \
		JAVA_VER=$$(java -version 2>&1 | awk -F '"' '/version/ {print $$2}' | cut -d'.' -f1); \
		if [ "$$JAVA_VER" = "$(JAVA_REQUIRED)" ]; then \
			echo "Java (>= $(JAVA_REQUIRED)):   found"; \
		else \
			echo >&2 "Java $(JAVA_REQUIRED): NOT found, but $$JAVA_VER was"; \
			exit 1; \
		fi; \
	else \
		echo >&2 "Java $(JAVA_REQUIRED):  NOT found"; \
		exit 1; \
	fi
	@if command -v docker >/dev/null 2>&1; then echo "Docker:         found"; else echo >&2 "Docker: NOT found"; exit 1; fi
	@if command -v xvfb-run >/dev/null 2>&1; then echo "xvfb-run:       found"; else echo >&2 "xvfb-run: NOT found"; exit 1; fi
	@if command -v openbox >/dev/null 2>&1; then echo "openbox:        found"; else echo >&2 "openbox: NOT found"; exit 1; fi
	@echo "All set!"
.PHONY: dependencies-check-dev

dependencies-check-run:
	@echo "Checking run only dependencies:"
	@if command -v docker >/dev/null 2>&1; then echo "Docker:         found"; else echo >&2 "Docker: NOT found"; exit 1; fi
	@if docker compose version >/dev/null 2>&1; then \
		echo "Docker compose: found"; \
	else \
		echo "Docker compose: NOT found"; \
		exit 1; \
	fi
	@if command -v xhost >/dev/null 2>&1; then echo "Xhost:          found"; else echo >&2 "Xhost: NOT found"; exit 1; fi
	@echo "All set!"
.PHONY: dependencies-check-run

dependencies-check-all:
	@echo "Checking building and run dependencies:"
	@if command -v java >/dev/null 2>&1; then \
		JAVA_VER=$$(java -version 2>&1 | awk -F '"' '/version/ {print $$2}' | cut -d'.' -f1); \
		if [ "$$JAVA_VER" -ge "$(JAVA_REQUIRED)" ]; then \
			echo "Java (>= $(JAVA_REQUIRED)):   found"; \
		else \
			echo >&2 "Java $(JAVA_REQUIRED): NOT found, but $$JAVA_VER was"; \
			exit 1; \
		fi; \
	else \
		echo >&2 "Java $(JAVA_REQUIRED):  NOT found"; \
		exit 1; \
	fi
	@if command -v docker >/dev/null 2>&1; then echo "Docker:         found"; else echo >&2 "Docker: NOT found"; exit 1; fi
	@if docker compose version >/dev/null 2>&1; then \
		echo "Docker compose: found"; \
	else \
		echo "Docker compose: NOT found"; \
		exit 1; \
	fi
	@if command -v xhost >/dev/null 2>&1; then echo "Xhost:          found"; else echo >&2 "Xhost: NOT found"; exit 1; fi
	@if command -v openbox >/dev/null 2>&1; then echo "openbox:        found"; else echo >&2 "openbox: NOT found"; exit 1; fi
	@if command -v xvfb-run >/dev/null 2>&1; then echo "xvfb-run:       found"; else echo >&2 "xvfb-run: NOT found"; exit 1; fi
	@echo "All set!"
.PHONY: dependencies-check-run

clean-all:
	$(MVNW) clean -Pinclude-report-in-execution-tree -f $(POM_PATH)
.PHONY: clean-all

XVFB := $(shell [ -z "$$DISPLAY" ] || [ "$$XDG_SESSION_TYPE" = "wayland" ] || [ -n "$$WAYLAND_DISPLAY" ] && echo "env -u WAYLAND_DISPLAY -u XDG_SESSION_TYPE NO_AT_BRIDGE=1 xvfb-run -a -s '-screen 0 1920x1080x24' bash -c")

package:
	$(XVFB) "openbox & $(MVNW) clean package -f $(POM_PATH)"
.PHONY: package

package-only:
	$(MVNW) clean package -Dmaven.test.skip=true -f $(POM_PATH)
.PHONY: package-only

run-mutation-light:
	$(XVFB) "openbox & $(MVNW) clean verify -Pmutation-testing -f $(POM_PATH)"
.PHONY: run-mutation-light

run-mutation-full:
	$(XVFB) "openbox & $(MVNW) clean verify -Pmutation-testing-full -f $(POM_PATH)"
.PHONY: run-mutation-full

run-coverage:
	$(XVFB) "openbox & $(MVNW) clean verify -Pjacoco -f $(POM_PATH)"
.PHONY: run-coverage

run-all:
	$(XVFB) "openbox & $(MVNW) clean verify -Pjacoco,mutation-testing-full -f $(POM_PATH)"
.PHONY: run-all

DB ?= mongo
DOC_COMPOSE_BASE := docker-build-run
NATIVE_BASE := native-build-run

docker-build-and-run: dependencies-check-run
	@export DISPLAY=${DISPLAY}
	@xhost +SI:localuser:root || xhost +local:docker || xhost +
	docker compose -f $(DOC_COMPOSE_BASE)/docker-compose.app.yml -f $(DOC_COMPOSE_BASE)/docker-compose.$(DB).yml up --build -d
.PHONY: docker-build-and-run

docker-stop:
	-xhost -SI:localuser:root
	-xhost -local:docker
	docker compose -f $(DOC_COMPOSE_BASE)/docker-compose.app.yml -f $(DOC_COMPOSE_BASE)/docker-compose.maria.yml -f $(DOC_COMPOSE_BASE)/docker-compose.mongo.yml down
.PHONY: docker-stop

docker-clean:
	-xhost -SI:localuser:root
	-xhost -local:docker
	docker compose -f $(DOC_COMPOSE_BASE)/docker-compose.app.yml -f $(DOC_COMPOSE_BASE)/docker-compose.maria.yml -f $(DOC_COMPOSE_BASE)/docker-compose.mongo.yml down -v
.PHONY: docker-stop

MONGO_ARGS := 	--mongo-connection-string="mongodb://localhost:27017/?directConnection=true" \
				--db-mongo-name=hospital_docker_mongo \
				--db-mongo-doctor-collection=doctor_docker_mongo \
				--db-mongo-department-collection=department-docker_mongo \
				--db-mongo-shift-collection=shift-docker_mongo

MARIA_ARGS := 	--db-backend=mariadb \
				--maria-jdbc-url="jdbc:mariadb://localhost:3306/hospital_docker_maria" \
				--maria-user=testuserdocker \
				--maria-password=testpassworddocker \
				--maria-ddl=update

ifeq ($(DB),mongo)
	NATIVE_ARGS := $(MONGO_ARGS)
else
	NATIVE_ARGS := $(MARIA_ARGS)
endif

native-build-and-run: dependencies-check-dev
	make package-only
	docker compose -f $(NATIVE_BASE)/docker-compose.$(DB).yml up -d --wait
	java -jar hospital-scheduler-app/target/*-jar-with-dependencies.jar $(NATIVE_ARGS) >/dev/null
.PHONY: native-build-and-run

native-stop:
	docker compose -f $(NATIVE_BASE)/docker-compose.maria.yml -f $(NATIVE_BASE)/docker-compose.mongo.yml down
.PHONY: native-stop

native-clean:
	docker compose -f $(NATIVE_BASE)/docker-compose.maria.yml -f $(NATIVE_BASE)/docker-compose.mongo.yml down -v
.PHONY: native-stop