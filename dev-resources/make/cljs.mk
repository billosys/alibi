NODE_MODULES = node_modules

build-cljs: $(NODE_MODULES)
	@lein cljsbuild once

$(NODE_MODULES):
	@npm install && ./node_modules/.bin/grunt copy

clean-cljs:
	@echo

clean-node:
	@rm -rf $(NODE_MODULES)
