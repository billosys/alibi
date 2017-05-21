(ns timi.server.datasource.sqlite.app-svc-test
  (:require
    [clojure.test :refer [test-vars deftest is]]
    [timi.config :refer [config]]
    [timi.server.datasource.sqlite.fixtures :refer [sqlite-fixture]]
    [timi.server.datasource.tools :as db-tools]
    [timi.server.domain.entry-app-svc-test :as entry]
    [timi.server.domain.project-admin-app-svc-test :as project-admin]
    [timi.server.domain.queries.entry-screen.activity-graphic-test]
    [timi.server.domain.queries.entry-screen.entries-for-day-test]
    [timi.server.domain.queries.entry-screen.list-all-bookable-projects-and-tasks-test]
    [timi.server.domain.task-admin-app-svc-test :as task-admin]
    [timi.server.domain.task-repository-test :as task-repo]))

(task-admin/deftests sqlite-fixture)
(task-repo/deftests sqlite-fixture)
(entry/deftests sqlite-fixture)
(project-admin/deftests sqlite-fixture)

(timi.server.domain.queries.entry-screen.entries-for-day-test/deftests sqlite-fixture)
(timi.server.domain.queries.entry-screen.list-all-bookable-projects-and-tasks-test/deftests sqlite-fixture)
(timi.server.domain.queries.entry-screen.activity-graphic-test/deftests sqlite-fixture)

(comment
  (test-vars [#'get-task]))
