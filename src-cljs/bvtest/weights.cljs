(ns bvtest.weights
  (:require-macros [reagent.ratom :refer [reaction run!]])
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string]))

(defn print-state [prefix state max-total-weight]
  (print prefix state "+ reactions:" {:max-total-weight max-total-weight}))

(defn int-unless-blank [val]
  (when-not (string/blank? val)
    (int (string/trim val))))

(defn weights-info [params result]
  ;; would ultimately be:
  #_(GET "/api/vehicle"
         {:headers {"Accept" "application/transit+json"}
          :params @params
          :handler #(reset! result %)})
  (reset! result {:regnr           (:regnr @params)
                  :total-weight    2000
                  :unloaded-weight 1500}))

(defn reset-weights [state params result]
  ;; enable these in order for validation to work
  #_(.val (js/$ "#in-total-weight") (:total-weight @result))
  #_(.val (js/$ "#in-unloaded-weight") (:unloaded-weight @result))

  (swap! state assoc :total-weight (:total-weight @result))
  (swap! state assoc :unloaded-weight (:unloaded-weight @result))
  (print 'reset-weights @state)
  (reset! result nil)
  (reset! params {}))

(defn weights-page-component []
  (let [state (atom {:eligibility "A"})
        max-total-weight (reaction (if (= "B" (:eligibility @state)) 4000 3000))]
    (reagent/create-class
      {:reagent-render      (let [params (atom {})
                                  result (atom nil)]
                              (run! (when @result
                                      (print "Setting the weights from:" @result)
                                      (reset-weights state params result)))
                              (fn []
                                [:div.container.col-xs-6
                                 [:div#search-wizard.tabbable.tabs-left {:role "tabpanel"}
                                  [:ul {:role "tablist"}
                                   [:li.active {:role "presentation"}
                                    [:a {:href "#weights-tab" :role "tab" :data-toggle "tab"} "Weights " [:i.fa]]]
                                   [:li {:role "presentation"}
                                    [:a {:href "#eligibility-tab" :role "tab" :data-toggle "tab"} "Eligibility " [:i.fa]]]]
                                  [:form#form-search.form-horizontal
                                   [:div.tab-content
                                    [:div#weights-tab.tab-pane.active
                                     [:h1 "Step 1: Weights"]
                                     [:p "If you have the registration number, enter it, otherwise fill in the weights."]
                                     [:div.form-group
                                      [:label.control-label.col-xs-7 {:for "in-regnr"} "Regnr:"]
                                      [:div.col-xs-5
                                       [:input#in-regnr.form-control
                                        {:on-change   #(swap! params assoc :regnr (-> % .-target .-value))
                                         :name        "in-regnr"
                                         :data-toggle "tooltip"
                                         :title       "Enter the registration number and click 'Get weights'"
                                         :type        :text
                                         :value       (:regnr @params)}]]]
                                     [:div.form-group
                                      [:div.col-xs-offset-4.col-xs-8
                                       [:button.btn.btn-info
                                        {:on-click #(weights-info params result)
                                         :disabled (string/blank? (:regnr @params))}
                                        "Get weights \u2193"]]]
                                     [:div.form-group
                                      [:p.col-xs-12 "How much does it weigh?"]]
                                     [:div.form-group
                                      [:label.control-label.col-xs-7 {:for "in-total-weight"}
                                       "Total weight (kg):"]
                                      [:div.col-xs-5
                                       [:input#in-total-weight.form-control
                                        {:on-change   #(do (swap! state assoc :total-weight (int-unless-blank (-> % .-target .-value)))
                                                           (print-state :total-weight @state @max-total-weight))
                                         :name        "in-total-weight"
                                         :type        :text
                                         :placeholder "Total weight"
                                         :data-toggle "tooltip"
                                         :title       "If eligibility is B, then max value is larger."
                                         :value       (:total-weight @state)}]]]
                                     [:div.form-group
                                      [:label.control-label.col-xs-7 {:for "in-unloaded-weight"}
                                       "Unloaded weight (kg):"]
                                      [:div.col-xs-5
                                       [:input#in-unloaded-weight.form-control
                                        {:on-change   #(do (swap! state assoc :unloaded-weight (int-unless-blank (-> % .-target .-value)))
                                                           (print-state :unloaded-weight @state @max-total-weight))
                                         :name        "in-unloaded-weight"
                                         :type        :text
                                         :placeholder "Unloaded weight"
                                         :data-toggle "tooltip"
                                         :title       "Unloaded weight is without load"
                                         :value       (:unloaded-weight @state)}]]]]
                                    [:div#eligibility-tab.tab-pane
                                     [:h1 "Step 2: Eligibility"]
                                     [:div.form-group
                                      [:p.col-xs-12 "Specify your eligibility."]]
                                     [:div.form-group
                                      [:label.control-label.col-xs-7 {:for "in-eligibility"} "Eligibility:"]
                                      [:div.col-xs-5
                                       [:select.form-control
                                        {:id           "in-eligibility"
                                         :name         "in-eligibility"
                                         :defaultValue (:eligibility @state)
                                         :data-toggle  "tooltip"
                                         :title        "Eligibility and total weight decides outgoing total weight"
                                         :style        {:margin-left  "15px"
                                                        :margin-right "15px"}
                                         :on-change    #(do (swap! state assoc :eligibility (-> % .-target .-value))
                                                            (print-state :eligibility @state @max-total-weight))}
                                        [:option {:value "A"} "A"]
                                        [:option {:value "B"} "B (higher total weight)"]]]]]
                                    [:ul.pager.wizard
                                     [:li.previous
                                      [:a {:href "javascript:void(0)"} "Previous"]]
                                     [:li.next
                                      [:a {:href "javascript:void(0)"} "Next"]]]]]]]))
       :component-did-mount (fn []

                              ;;; validator

                              (let [validator (.bootstrapValidator
                                                (js/$ "#form-search")
                                                (clj->js {"excluded"      [":disabled"]
                                                          "feedbackIcons" {"valid"      "glyphicon glyphicon-ok"
                                                                           "invalid"    "glyphicon glyphicon-remove"
                                                                           "validating" "glyphicon glyphicon-refresh"}
                                                          "fields"        {"in-total-weight"    {"validators" {"integer"     {}
                                                                                                               "notEmpty"    {}
                                                                                                               "between"     {"min" 0
                                                                                                                              "max" (fn [value validator field]
                                                                                                                                      @max-total-weight)}
                                                                                                               "greaterThan" {"value"     "in-unloaded-weight"
                                                                                                                              "inclusive" false}}}
                                                                           "in-unloaded-weight" {"validators" {"integer"  {}
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
                                       (print "failed validation of" (-> data .-element (.attr "id"))
                                              "for value" (.-value (.getElementById js/document (-> data .-element (.attr "id")))))
                                       ;; whole tab is now also invalid, so change tab icon accordingly
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
                                       (print "successful validation of" (-> data .-element (.attr "id"))
                                              "for value" (.-value (.getElementById js/document (-> data .-element (.attr "id")))))
                                       ;; now check if whole tab is valid, and change tab icon accordingly
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
                                                          (print-state "onTabShow" @state @max-total-weight)
                                                          (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                                                            (.revalidateField validator "in-total-weight")
                                                            (.revalidateField validator "in-unloaded-weight")))})))
                              ;; revalidate total weight when max limit changes
                              (run!
                                (when @max-total-weight
                                  (print "revalidating for new max total weight" @max-total-weight)
                                  (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                                    (.revalidateField validator "in-total-weight")
                                    (.revalidateField validator "in-unloaded-weight"))))
                              ;; revalidate unloaded weight when total weight changes
                              (run!
                                (when (:total-weight @state)
                                  (print "total weight changed, revalidating unloaded weight")
                                  (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                                    (.revalidateField validator "in-unloaded-weight"))))
                              ;; revalidate total weight when unloaded weight changes
                              (run!
                                (when (:unloaded-weight @state)
                                  (print "unloaded weight changed, revalidating total weight")
                                  (let [validator (.data (js/$ "#form-search") "bootstrapValidator")]
                                    (.revalidateField validator "in-total-weight")))))})))
