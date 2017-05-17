JS_DIR = dev-resources/js
NODE_MODS = ./node_modules
NODE_MODS_PATH = $(JS_DIR)/$(NODE_MODS)
CLJS_INSTALL_DIR = ./resources/public/dist

build-cljs: node
	@# XXX min isn't working right now ...
	@#@lein cljsbuild once min
	@lein cljsbuild once

build-cljs-dev: node
	@lein cljsbuild once

node: $(NODE_MODS_PATH)
	@cd $(JS_DIR) && \
	 $(NODE_MODS)/.bin/grunt copy

$(NODE_MODS_PATH):
	@cd $(JS_DIR) && \
	 npm install

clean-cljs:
	@rm -rf $(CLJS_INSTALL_DIR)

clean-node:
	@cd $(JS_DIR) && \
	rm -rf $(NODE_MODS)
