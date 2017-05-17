(ns timi.time-page
  (:require
    [timi.logging :refer [log log-cljs]]
    [timi.post-new-entry-bar :as post-new-entry-bar]
    [timi.post-entry-form :as post-entry-form]
    [timi.activity-graphic :as activity-graphic]
    [timi.activity-graphic-data-source :as ag-ds]
    [timi.day-entry-table :as day-entry-table]
    [cljs.reader]
    [om.core :as om]
    [om.dom :as dom]
    [timi.time-page-state :as state]
    [timi.actions :as actions]))

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
  (fn [_ owner]
    (reify
      om/IRender
      (render [_]
        (om/build post-new-entry-bar/entry-bar-form component-state))))
  state
  {:target (js/document.getElementById "post-new-entry-bar-container")})

(om/root
  post-entry-form/om-component
  component-state
  {:target (js/document.getElementById "entry-form-react-container")})

(om/root
  activity-graphic/render-html
  component-state
  {:target (js/document.getElementById "activity-graphic")})

(om/root
  activity-graphic/render-tooltip
  component-state
  {:target (js/document.getElementById "activity-graphic-tooltip-container")})

(defn render-day-entry-table!
  [for-state]
  (let [new-date (state/selected-date for-state)]
    (day-entry-table/render "day-entry-table" new-date)))

(add-watch
  state :renderer
  (fn [_ _ _ new-state]
    ;(log "new-state %o" new-state)
    (render-day-entry-table! new-state)))

(reset! state @state)
