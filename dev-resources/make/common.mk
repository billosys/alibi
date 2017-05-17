build: build-cljs build-clj

run:
	@lein timi-run

clean: clean-cljs clean-clj

clean-all: clean clean-node
