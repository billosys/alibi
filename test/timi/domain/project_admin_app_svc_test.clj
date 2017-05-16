(ns timi.domain.project-admin-app-svc-test
  (:require [timi.domain.project-admin-app-svc
             :as projects :refer [new-project!]]
            [clojure.test :refer [is testing]]
            [timi.db-tools :as db-tools]
            [timi.config :refer [config]]
            [timi.test-helpers :refer [deftest copy-tests]]
            [timi.domain.project :as project]))

(defn make-valid-new-project-cmd []
  {:project-name "time tracker"
   :billing-method :fixed-price})

(deftest new-project-is-persisted
  (let [project-id (new-project! {:project-name "time tracker"
                                  :billing-method :fixed-price})
        project (project/get project-id)]
    (is project "the repository should return the newly created project")
    (when project
      (is (= "time tracker" (:project-name project)))
      (is (= :fixed-price (:billing-method project))))))

(deftest billing-methods-persisted-correctly
  (doseq [billing-method [:fixed-price :hourly :overhead]]
    (testing (str "billing-method " billing-method)
      (let [project-id (new-project!
                         {:project-name (str "a " billing-method " project")
                          :billing-method billing-method})
            project (project/get project-id)]
        (is (= billing-method (:billing-method project)))))))

(deftest new-project-validations
  (let [valid-cmd (make-valid-new-project-cmd)]
    (testing "project-name"
      (is (thrown? AssertionError
                   (new-project! (dissoc valid-cmd :project-name)))
          "no name not allowed")
      (is (thrown? AssertionError
                   (new-project! (assoc valid-cmd :project-name "")))
          "empty name not allowed")
      (is (thrown? AssertionError
                   (new-project! (assoc valid-cmd :project-name 123)))
          "name must be a string"))
    (testing "billing-method"
      (is (thrown? AssertionError
                   (new-project! (dissoc valid-cmd :billing-method)))
          "billing-method must be present")
      (is (thrown? AssertionError
                   (new-project! (assoc valid-cmd
                                        :billing-method :not-a-billing-method)))
          "billing-method must be valid value"))))

(defn fixture [f]
  (db-tools/clean-all!)
  (f))

(def ^:private my-ns *ns*)

(defmacro deftests [with-fixture]
  (copy-tests my-ns `(fn [the-test#] (~with-fixture
                                       (fn [] (fixture the-test#))))))
