build-clj:
	@lein with-profile +local,+uberjar compile

clean-clj:
	@lein clean
