(ns timi.server.cli.parser-test
  (:require
    [clojure.test :refer :all]
    [timi.server.cli.parser :as parser]))

(deftest validate-membership
  (is (parser/validate-membership [:a :b] :a))
  (is (parser/validate-membership [:a :b] :b))
  (is (not (parser/validate-membership [:a :b] :c))))

(deftest validate-command
  (is (parser/validate-command [:a :b] :a))
  (is (parser/validate-command [:a :b] :b))
  (is (not (parser/validate-command [:a :b] :c))))

(deftest validate-subcommand
  (is (parser/validate-subcommand [:a :b] :a))
  (is (parser/validate-subcommand [:a :b] :b))
  (is (not (parser/validate-subcommand [:a :b] :c))))

(deftest get-default-subcommand
  (is :a (parser/get-default-subcommand [:a :b] :a))
  (is :b (parser/get-default-subcommand [:a :b] :b))
  (is :help (parser/get-default-subcommand [:a :b] :c))
  (is :a (parser/get-default-subcommand [:a :b] :a :z))
  (is :b (parser/get-default-subcommand [:a :b] :b :z))
  (is :z (parser/get-default-subcommand [:a :b] :c :z)))
