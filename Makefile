MVNW := ./mvnw
POM_PATH = hospital-scheduler-aggregator/pom.xml

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