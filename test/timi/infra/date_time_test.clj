(ns timi.infra.date-time-test
  (:require
    [clojure.test :refer [deftest is]]
    [timi.infra.date-time :as dt]))

(deftest ->local-time-fails-on-2400
  (is (thrown? java.lang.IllegalArgumentException
               (dt/->local-time "24:00"))))
