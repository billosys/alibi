JS_DIR = ./resources/js
NODE_MODS = ./node_modules
NODE_MODS_PATH = $(JS_DIR)/$(NODE_MODS)
CLJS_INSTALL_DIR = ./resources/public/dist

build-cljs-web: node
	@# XXX min isn't working right now ...
	@#@lein cljsbuild once min
	@lein cljsbuild once

build-cljs-cli: node
	@# XXX min isn't working right now ...
	@#@lein cljsbuild once min
	@lein cljsbuild once cli
	@chmod 755 ./bin/timi

build-cljs: build-cljs-web build-cljs-cli

build-cljs-dev: build-cljs-cli
	@lein cljsbuild once

node: $(NODE_MODS_PATH)
	@cd $(JS_DIR) && \
	 $(NODE_MODS)/.bin/grunt copy

$(NODE_MODS_PATH):
	@cd $(JS_DIR) && \
	 npm install

dev:
	@rlwrap lein timi-figwheel

clean-cljs:
	@rm -rf $(CLJS_INSTALL_DIR)

clean-node:
	@cd $(JS_DIR) && \
	rm -rf $(NODE_MODS)
