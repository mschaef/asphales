.PHONY: help
 help:	                                       ## Show list of available make targets
	@cat Makefile | grep -e "^[a-zA-Z_\-]*: *.*## *" | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


.PHONY: run
run:                                           ## Run the application
	lein run

.PHONY: check-format
check-format:
	lein cljfmt check

.PHONY: format
format:                                        ## Reformat Clojure source code
	lein cljfmt fix

.PHONY: package
package: check-format test                     ## Package a new release of the application
	lein clean
	lein compile
	lein release patch

.PHONY: test
test:                                          ## Run the unit test suite
	lein test

.PHONY: clean
clean:                                         ## Clean the local build directory
	lein clean
