(ns alibi.post-entry-form
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [clojure.string :as string]
    [alibi.logging :refer [log log-cljs]]
    [time.core :refer [expand-time]]
    [om.core :as om]
    [om.dom :as dom]
    [alibi.actions :as actions]
    [alibi.entry-page-state :as state :refer [validate-form]]))

(defn parse-float [v] (js/parseFloat v))

(defn on-form-submit [entry on-error event]
  (log "submit")
  (let [errors (validate-form entry)]
    (when (seq errors)
      (do
        (.preventDefault event)
        (on-error entry)))))

(defn datepicker [data owner]
  (let [get-elements
        (fn []
          (let [el (om/get-node owner "element")
                $el (js/$ el)]
            {:$input-element (.find $el ".datepicker-input")
             :$show-btn (.find $el ".datepicker-show-btn")}))]
    (reify
      om/IDidMount
      (did-mount [_]
        (let [{:keys [$input-element $show-btn]} (get-elements)]
          (. $input-element datepicker #js {:todayBtn "linked",
                                            :todayHighlight true,
                                            :language "nl",
                                            :format "yyyy-mm-dd",
                                            :autoclose true })
          (.. $input-element datepicker
              (on "changeDate"
                  #((or (:onChangeDate data)
                        identity) %)))
          (. $show-btn on "click" #(.datepicker $input-element "show"))))

      om/IWillUnmount
      (will-unmount [_]
        (let [{:keys [$input-element $show-btn]} (get-elements)]
          (. $input-element datepicker "destroy")
          (. $show-btn off)))

      om/IRender
      (render [_]
        (dom/div
          #js {:className "input-group input-group-datepicker"
               :ref "element"}
          (dom/input
            #js {:className "form-control datepicker-input"
                 :type "text"
                 :value (:selected-date data)
                 :name (:input-name data)
                 :readOnly true})
          (dom/span
            #js {:className "input-group-addon datepicker-show-btn"}
            (dom/i
              #js {:className "glyphicon glyphicon-calendar"})))))))

(defn render-state
  [for-state]
  (let [{:keys [input-entry
                dispatch!
                ]} for-state
        {:keys [startTime endTime selected-date comment isBillable
                formWasSubmitted submit-time-state selected-item
                entry-id]} input-entry
        {:keys [project-id task-id]} selected-item
        editing? (integer? (parse-float entry-id))]
    (when input-entry
      ;(log "render-state" comment)
      (let [on-change-start-time (fn [start-time]
                                   (dispatch! {:action :change-start-time
                                               :start-time start-time}))
            on-change-end-time (fn [end-time]
                                 (dispatch! {:action :change-end-time
                                             :end-time end-time}))
            summary-errors (seq (when formWasSubmitted
                                  (validate-form submit-time-state)))
            active-errors (seq (when formWasSubmitted
                                 (validate-form input-entry)))
            failed-fields (into {} active-errors)
            start-time-error? (failed-fields "Start time")
            end-time-error? (failed-fields "End time")
            time-errors? (or start-time-error? end-time-error?)]
        (dom/form
          #js {:method "post"
               :action (str "/entry/" selected-date)
               :className "form-horizontal entry-form"
               :onSubmit (partial on-form-submit input-entry
                                  #(dispatch! {:action :entry-form-show-errors
                                               :for-entry %}))}
          (dom/div
            #js {:className (string/join
                              " "
                              [(when summary-errors "has-errors")
                               (when (seq selected-item) "entry-form-visible")])
                 :id "entry-form-container"}
            (dom/input #js {:type "hidden"
                            :name "selected-project-id"
                            :value (or project-id "")})
            (dom/input #js {:type "hidden"
                            :name "selected-task-id"
                            :value (or task-id "")})
            (dom/input #js {:type "hidden"
                            :name "entry-id"
                            :value (or entry-id "")})
            (dom/div
              #js {:className "row"}
              (dom/div
                #js {:className "col-md-12"}
                (dom/div
                  #js {:className
                       (str "alert entry-form-errors"
                            (if active-errors " alert-danger" " alert-success")
                            (when summary-errors " entry-form-errors-has-errors"))}
                  "There is a problem with one or more fields in the form, please correct them:"
                  (dom/ul
                    #js {:className "entry-form-errors-list-container"}
                    (map #(-> (dom/li #js {:key (get % 0)}
                                      (dom/strong nil (get % 0))
                                      (str " "(get % 1))))
                         summary-errors)))
                (dom/div
                  #js {:className "panel panel-default"}
                  (dom/div
                    #js {:className "panel-body"}
                    (dom/fieldset
                      nil
                      (dom/div
                        #js {:className "form-group"}
                        (dom/label
                          #js {:className "control-label col-md-2"}
                          "Date:")
                        (dom/div
                          #js {:className "col-md-10"}
                          (dom/div
                            #js {:className "form-inline form-inline-no-horz-margin"}
                            (dom/div
                              #js {:className "form-group"}
                              (om/build datepicker {:input-name "for-date"
                                                    :selected-date selected-date
                                                    :onChangeDate
                                                    (fn [event]
                                                      (let [instant
                                                            (..
                                                              js/JSJoda -Instant
                                                              (ofEpochMilli
                                                                (.. event -date getTime)))

                                                            new-date
                                                            (.. js/JSJoda -LocalDate
                                                                (ofInstant instant) toString)]
                                                        (dispatch! (actions/change-entry-page-date new-date))))}))
                            " "
                            (dom/div
                              #js {:className (str "form-group"
                                                   (when start-time-error?
                                                     " has-error"))}
                              (dom/label
                                #js {:htmlFor "start-time"}
                                "From:")
                              " "
                              (dom/input
                                #js {:id "start-time"
                                     :className "form-control time-entry-input"
                                     :type "text"
                                     :name "start-time"
                                     :value startTime
                                     :size "5"
                                     :maxLength "5"
                                     :autoComplete "off"
                                     :placeholder "Time"
                                     :onFocus #(.. % -target select)
                                     :onChange #(on-change-start-time (.. % -target -value))
                                     :onBlur #(on-change-start-time (expand-time
                                                                      (.. % -target -value)))
                                     :onKeyDown #(when (= (.-keyCode %) 13)
                                                   (on-change-start-time
                                                     (expand-time (.. % -target -value))))
                                     }))
                            " "
                            (dom/div
                              #js {:className (str "form-group"
                                                   (when end-time-error? " has-error"))}
                              (dom/label
                                #js {:htmlFor "end-time"} "To:")
                              " "
                              (dom/input
                                #js {:type "text"
                                     :className "form-control time-entry-input"
                                     :id "end-time"
                                     :name "end-time"
                                     :value endTime
                                     :size "5"
                                     :maxLength "5"
                                     :autoComplete "off"
                                     :placeholder "Time"
                                     :onFocus #(.. % -target select)
                                     :onChange #(on-change-end-time (.. % -target -value))
                                     :onBlur #(on-change-end-time (expand-time
                                                                    (.. % -target -value)))
                                     :onKeyDown #(when (= (.-keyCode %) 13)
                                                   (on-change-end-time (expand-time
                                                                         (.. % -target -value))))}))
                            " "
                            (dom/div
                              #js {:className "form-group help-block"}
                              "Use format 13:37")
                            (dom/div
                              #js {:className (when time-errors? "has-error")}
                              (dom/span
                                #js {:className "help-block entry-forms-errors-date-time-errors"}
                                (when start-time-error?
                                  (dom/span nil (str "Start time: " start-time-error?)
                                            (dom/br nil)))
                                (when end-time-error?
                                  (dom/span nil (str "End time: " end-time-error?))))))))
                      (dom/div
                        #js {:className "form-group"}
                        (dom/label
                          #js {:className "col-md-2 control-label"
                               :htmlFor "opmerking"}
                          "Comment:")
                        (dom/div
                          #js {:className "col-md-10"}
                          (dom/input
                            #js {:type "text"
                                 :name "comment"
                                 :value comment
                                 :onChange #(dispatch! {:action :change-comment
                                                        :comment (.. % -target -value)})
                                 :id "comment"
                                 :className "form-control"})
                          (dom/span
                            #js {:className "help-block"}
                            "Provide any details that can help the customer understand what you've worked on.")))
                      (dom/div
                        #js {:className "form-group"}
                        (dom/div
                          #js {:className "col-md-offset-2 col-md-10"}
                          (dom/div
                            #js {:className "checkbox"}
                            nil
                            (dom/label
                              nil
                              (dom/input
                                #js {:type "checkbox"
                                     :id "billable"
                                     :name "billable?"
                                     :checked isBillable
                                     :onChange #(dispatch! {:action :change-billable?
                                                            :billable? (.. % -target -checked)})})
                              "This entry is billable"))))
                      (dom/div
                        #js {:className "form-group"}
                        (dom/div
                          #js {:className "col-md-offset-2 col-md-10 form-actions"}
                          (dom/button
                            #js {:className "btn btn-success save-hours"
                                 :type "submit"}
                            (if editing? " Save changes" " Post entry"))
                          " "
                          (if editing?
                            (dom/button
                              #js {:className "btn btn-danger delete-entry"
                                   :name "delete-entry"}
                              "Delete entry"))
                          " "
                          (dom/button
                            #js {:className "btn btn-link"
                                 :name "cancel"
                                 :onClick #(do
                                             (.preventDefault %)
                                             (dispatch! {:action :cancel-entry}))}
                            "Cancel"))))))))))))))

(defn om-component [for-state owner]
  (reify
    om/IRender
    (render [_]
      (let [form (om/observe owner (state/entry-screen-form))
            input-entry (state/input-entry form)]
        (render-state {:dispatch! (:dispatch! for-state)
                       :input-entry input-entry})))))
