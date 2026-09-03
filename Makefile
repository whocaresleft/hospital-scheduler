MVNW := ./mvnw
POM_PATH = hospital-scheduler-aggregator/pom.xml
JAVA_REQUIRED := 17

dependencies-check:
	@echo "Checking dependencies:"
	@if command -v java >/dev/null 2>&1; then \
		JAVA_VER=$$(java -version 2>&1 | awk -F '"' '/version/ {print $$2}' | cut -d'.' -f1); \
		if [ "$$JAVA_VER" = "$(JAVA_REQUIRED)" ]; then \
			echo "Java $(REQUIRED_JAVA):          found"; \
		else \
			echo >&2 "Java $(REQUIRED_JAVA): NOT found, but $$JAVA_VER was"; \
			exit 1; \
		fi; \
	else \
		echo >&2 "Java $(REQUIRED_JAVA):  NOT found"; \
		exit 1; \
	fi
	@if command -v docker >/dev/null 2>&1; then echo "Docker:         found"; else echo >&2 "Docker: not found"; exit 1; fi
	@if docker compose version >/dev/null 2>&1; then \
		echo "Docker compose: found"; \
	else \
		echo "Docker compose: NOT found"; \
		exit 1; \
	fi
	@if command -v xhost >/dev/null 2>&1; then echo "Xhost:          found"; else echo >&2 "Xhost: not found"; exit 1; fi
	@echo "All set!"
.PHONY: dependencies-check

clean-all:
	$(MVNW) clean -Pinclude-report-in-execution-tree -f $(POM_PATH)
.PHONY: clean-all

XVFB := $(shell [ -z "$$DISPLAY" ] || [ "$$XDG_SESSION_TYPE" = "wayland" ] || [ -n "$$WAYLAND_DISPLAY" ] && echo "xvfb-run -a -s '-screen 0 1920x1080x24' env -u WAYLAND_DISPLAY -u XDG_SESSION_TYPE NO_AT_BRIDGE=1")

package:
	$(XVFB) $(MVNW) clean package -f $(POM_PATH)
.PHONY: package

package-only:
	$(MVNW) clean package -Dmaven.test.skip=true -f $(POM_PATH)
.PHONY: package-only

run-mutation-light:
	$(XVFB) $(MVNW) clean verify -Pmutation-testing -f $(POM_PATH)
.PHONY: run-mutation-light

run-mutation-full:
	$(XVFB) $(MVNW) clean verify -Pmutation-testing-full -f $(POM_PATH)
.PHONY: run-mutation-full

run-coverage:
	$(XVFB) $(MVNW) clean verify -Pjacoco -f $(POM_PATH)
.PHONY: run-coverage

run-all:
	$(XVFB) $(MVNW) clean verify -Pjacoco,mutation-testing-full -f $(POM_PATH)
.PHONY: run-all

DB ?= mongo

docker-build-and-run:
	@export DISPLAY=${DISPLAY}
	@xhost +SI:localuser:root || xhost +local:docker || xhost +
	docker compose -f docker-compose.app.yml -f docker-compose.$(DB).yml up --build -d
.PHONY: docker-build-and-run

docker-stop:
	-xhost -SI:localuser:root
	-xhost -local:docker
	docker compose -f docker-compose.app.yml -f docker-compose.maria.yml -f docker-compose.mongo.yml down -v
.PHONY: docker-stop