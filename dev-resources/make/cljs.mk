JS_DIR = dev-resources/js
NODE_MODS = ./node_modules
NODE_MODS_PATH = $(JS_DIR)/$(NODE_MODS)
CLJS_INSTALL_DIR = ./resources/public/dist

build-cljs: $(NODE_MODS_PATH)
	@lein cljsbuild once min

build-cljs-dev: $(NODE_MODS_PATH)
	@lein cljsbuild once

$(NODE_MODS_PATH):
	@cd $(JS_DIR) && \
	 npm install && \
	 $(NODE_MODS)/.bin/grunt copy

clean-cljs:
	@rm -rf $(CLJS_INSTALL_DIR)

clean-node:
	@cd $(JS_DIR) && \
	rm -rf $(NODE_MODS)
