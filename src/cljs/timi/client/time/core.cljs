(ns timi.client.time.core
  (:require
    [cljs.reader]
    [om.core :as om]
    [om.dom :as dom]
    [timi.client.actions :as actions]
    [timi.client.activity.graphic.core :as activity-graphic]
    [timi.client.day.table :as day-table]
    [timi.client.logging :refer [log log-cljs]]
    [timi.client.time.form :as form]
    [timi.client.time.project :as project]
    [timi.client.time.state :as state]))

(defonce state (atom state/initial-state))

(enable-console-print!)

(defn dispatch! [action]
  (if (fn? action)
    (action dispatch! @state)
    (swap! state state/reducer action)))

(let [current-state @state]
  (when-not (seq (state/entries current-state))
    (dispatch! (actions/entries-load-data
                 (state/selected-date current-state)))))

(def component-state {:dispatch! dispatch!
                      :get-state (constantly state)})

; if you wonder why we introduce an itermediate IRender here: it seems Om
; ref-cursors only work if there is at least one om/root that binds to the root
; atom, so we do that here even though it is not passed on to om/build
; see also https://github.com/omcljs/om/issues/864
(om/root
  (fn [_data owner]
    (reify
      om/IRender
      (render [_]
        (om/build project/entry-bar-form component-state))))
  state
  {:target (js/document.getElementById "post-new-entry-bar-container")})

(om/root
  form/om-component
  component-state
  {:target (js/document.getElementById "entry-form-react-container")})

(om/root
  activity-graphic/render-html
  component-state
  {:target (js/document.getElementById "activity-graphic")})

(om/root
  activity-graphic/render-navigator
  component-state
  {:target (js/document.getElementById "date-navigator")})

(om/root
  activity-graphic/render-tooltip
  component-state
  {:target (js/document.getElementById "activity-graphic-tooltip-container")})

(add-watch
  state :renderer
  (fn [_ _ _ new-state]
     (log "new-state %o" new-state)
    (->> new-state
         (state/selected-date)
         (day-table/render "day-entry-table"))))

(reset! state @state)
