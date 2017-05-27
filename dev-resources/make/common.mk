DB_FILE = var/data/timi.db

build: $(DB_FILE) build-cljs build-clj

$(DB_FILE):
	@lein timi-init $(DB_FILE)

cookie:
	@openssl rand -base64 12 2>/dev/null

run:
	@lein timi-run

clean: clean-cljs clean-clj

clean-all: clean clean-node

deploy:
	@lein timi-deploy

heading:
	@make bar
	@echo
	@make bar
