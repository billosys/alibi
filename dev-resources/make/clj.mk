build-clj:
	@lein with-profile +local,+uberjar uberjar

clean-clj:
	@lein clean
