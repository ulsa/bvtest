(ns bvtest.weights
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.string :as gstring]
            goog.string.format))

(defn format "Removed from cljs.core 0.0-1885, Ref. http://goo.gl/su7Xkj"
  [fmt & args] (apply gstring/format fmt args))

(def state (atom {:eligibility "A"}))

;; related to validation
(def max-total-weight (reaction (if (= "B" (:eligibility @state)) 4000 3000)))

(defn do-toggle
  ([pred]
   (do-toggle pred {}))
  ([pred m]
   (merge m {:style {:display (if pred "block" "none")}})))

(defn print-state [& [prefix]]
  (prn prefix @state {:max-total-weight @max-total-weight}))

(defn weights-page []
  [:div.container.col-xs-6
   [:div {:id "search-wizard" :role "tabpanel" :class "tabbable tabs-left"}
    [:ul {:role "tablist"}
     [:li {:role "presentation" :class "active"}
      [:a {:href "#weights-tab" :role "tab" :data-toggle "tab"} "Weights " [:i {:class "fa"}]]]
     [:li {:role "presentation"}
      [:a {:href "#eligibility-tab" :role "tab" :data-toggle "tab"} "Eligibility " [:i {:class "fa"}]]]]
    [:form {:id    "form-search"
            :class "form-horizontal"}
     [:div {:class "tab-content"}
      [:div {:class "tab-pane active" :id "weights-tab"}
       [:h1 "Step 1: Weights"]
       [:div {:class "form-group"}
        [:p {:class "col-xs-12"} "How much does it weigh?"]]
       [:div {:class "form-group"}
        [:label {:for "in-total-weight" :class "control-label col-xs-7"}
         "Total weight (kg):"]
        [:div {:class "col-xs-5"}
         [:input
          {:on-change   #(do (swap! state assoc :total-weight (int (-> % .-target .-value)))
                             (print-state "total"))
           :id          "in-total-weight"
           :name        "in-total-weight"
           :class       "form-control"
           :type        "input"
           :placeholder "Total weight"
           :data-toggle "tooltip"
           :title       "If eligibility is B, then max value is larger."
           :value       (:total-weight @state)}]]]
       [:div {:class "form-group"}
        [:label {:for "in-unloaded-weight" :class "control-label col-xs-7"}
         "Unloaded weight (kg):"]
        [:div {:class "col-xs-5"}
         [:input
          {:on-change   #(do (swap! state assoc :unloaded-weight (int (-> % .-target .-value)))
                             (print-state "unloaded"))
           :id          "in-unloaded-weight"
           :name        "in-unloaded-weight"
           :class       "form-control"
           :type        "input"
           :placeholder "Unloaded weight"
           :data-toggle "tooltip"
           :title       "Unloaded weight is without load"
           :value       (:unloaded-weight @state)}]]]]
      [:div {:class "tab-pane" :id "eligibility-tab"}
       [:h1 "Step 2: Eligibility"]
       [:div {:class "form-group"}
        [:p {:class "col-xs-12"} "Specify your eligibility."]]
       [:div {:class "form-group"}
        [:label {:for "in-eligibility" :class "control-label col-xs-7"} "Eligibility:"]
        [:div {:class "col-xs-5"}
         [:select
          {:id           "in-eligibility"
           :name         "in-eligibility"
           :class        "form-control"
           :defaultValue (:eligibility @state)
           :value        (:eligibility @state)
           :data-toggle  "tooltip"
           :title        "Eligibility and total weight decides outgoing total weight"
           :style        {:margin-left  "15px"
                          :margin-right "15px"}
           :on-change    #(do (swap! state assoc :eligibility (-> % .-target .-value))
                              (print-state "eligibility"))}
          [:option {:value "A"} "A"]
          [:option {:value "B"} "B (higher total weight)"]]]]]
      [:ul {:class "pager wizard"}
       [:li {:class "previous"}
        [:a {:href "javascript:void(0)"} "Previous"]]
       [:li {:class "next"}
        [:a {:href "javascript:void(0)"} "Next"]]]]]]])

(defn weights-page-did-mount []
  (.ready (js/$ js/document)
          (fn []

            ;;; validator

            (let [validator (.bootstrapValidator
                              (js/$ "#form-search")
                              (clj->js {"excluded"      [":disabled"]
                                        "feedbackIcons" {"valid"      "glyphicon glyphicon-ok"
                                                         "invalid"    "glyphicon glyphicon-remove"
                                                         "validating" "glyphicon glyphicon-refresh"}
                                        "fields"        {"in-total-weight" {"validators" {"integer"     {}
                                                                                          "notEmpty"    {}
                                                                                          "between"     {"min" 0
                                                                                                         "max" (fn [value validator field]
                                                                                                                 @max-total-weight)}
                                                                                          "greaterThan" {"value"     "in-unloaded-weight"
                                                                                                         "inclusive" false}}}
                                                         "in-unloaded-weight"  {"validators" {"integer"  {}
                                                                                          "notEmpty" {}
                                                                                          "between"  {"min" 0
                                                                                                      "max" (fn [value validator field]
                                                                                                              @max-total-weight)}
                                                                                          "lessThan" {"value"     "in-total-weight"
                                                                                                      "inclusive" false}}}}}))]
              ;; Called when a field is invalid
              (.on validator "error.field.bv"
                   (fn [e data]
                     ;; data.element is the field element
                     (let [tab-pane (-> data .-element (.parents ".tab-pane"))
                           tab-id (.attr tab-pane "id")
                           selector (str "a[href=\"#" tab-id "\"][data-toggle=\"tab\"]")]
                       (-> (js/$ selector)
                           .parent
                           (.find "i")
                           (.removeClass "fa-check")
                           (.addClass "fa-times")))))
              ;; Called when a field is valid
              (.on validator "success.field.bv"
                   (fn [e data]
                     ;; data.bv is the validator instance
                     ;; data.element is the field element
                     (let [tab-pane (-> data .-element (.parents ".tab-pane"))
                           tab-id (.attr tab-pane "id")
                           selector (str "a[href=\"#" tab-id "\"][data-toggle=\"tab\"]")
                           icon (-> (js/$ selector)
                                    .parent
                                    (.find "i")
                                    (.removeClass "fa-check fa-times"))
                           tab-valid? (.isValidContainer (.-bv data) tab-pane)]
                       (.addClass icon (if tab-valid? "fa-check" "fa-times"))))))

            ;;; wizard

            (let [wizard (js/$ "#search-wizard")]
              (.bootstrapWizard
                wizard
                (clj->js {"tabClass"  "nav nav-tabs"
                          "onTabShow" (fn [tab navigation index]
                                        ;; revalidate weights on every tab show
                                        (let [tab-count (.-length (.children navigation))
                                              current (inc index)]
                                          (print-state "onTabShow")
                                          (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                                            (.revalidateField validator "in-total-weight")
                                            (.revalidateField validator "in-unloaded-weight"))))})))
            ;; revalidate total weight when max limit changes
            ;; TODO this does not fire for some reason
            (reaction
              (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                (print-state (str "revalidating due to changed " @max-total-weight))
                (.revalidateField validator "in-total-weight"))))))

(defn weights-page-component []
  (reagent/create-class {:reagent-render      weights-page
                         :component-did-mount weights-page-did-mount}))