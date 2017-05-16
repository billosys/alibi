(ns timi.datasource.sqlite.app-svc-test
  (:require
    [timi.datasource.sqlite.fixtures :refer [sqlite-fixture]]
    [clojure.test :refer [test-vars deftest is]]
    [timi.domain.entry-app-svc-test :as entry]
    [timi.domain.project-admin-app-svc-test :as project-admin]
    [timi.domain.task-admin-app-svc-test :as task-admin]
    [timi.domain.task-repository-test :as task-repo]
    [timi.config :refer [config]]
    [timi.db-tools :as db-tools]
    [timi.domain.queries.entry-screen.entries-for-day-test]
    [timi.domain.queries.entry-screen.list-all-bookable-projects-and-tasks-test]
    [timi.domain.queries.entry-screen.activity-graphic-test]
    ))

(task-admin/deftests sqlite-fixture)
(task-repo/deftests sqlite-fixture)
(entry/deftests sqlite-fixture)
(project-admin/deftests sqlite-fixture)

(timi.domain.queries.entry-screen.entries-for-day-test/deftests sqlite-fixture)
(timi.domain.queries.entry-screen.list-all-bookable-projects-and-tasks-test/deftests sqlite-fixture)
(timi.domain.queries.entry-screen.activity-graphic-test/deftests sqlite-fixture)

(comment
  (test-vars [#'get-task]))
