(ns recipe.views.editors
  (:require [reagent.core :as reagent]
            [markdown.core :refer [md->html]]
            [re-frame.core :refer [dispatch subscribe]]
            [re-frame.core :as re-frame]))

(defn- input'
  [{:keys [save change stop value]}]
  [:input {:value value
           :on-key-down #(condp = (.-which %)
                           13 (do (save) (stop))
                           27 (stop)
                           nil)
           :on-change change
           :on-blur stop}])

(def input (with-meta input' {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn textarea'
  [{:keys [save change stop value]}]
  [:textarea {:value value
              :style {:width "100%"
                      :height "500px"}
              :on-key-down #(cond
                              (and (= (.-which %) 13)
                                   (.-shiftKey %)) (do (save) (stop))
                              (= (.-which %) 27) (stop)
                              :else nil)
              :on-change change
              :on-blur stop}])

(def textarea (with-meta textarea' {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn markdown [value]
  [:div {:dangerouslySetInnerHTML
         {:__html (md->html value)}}])

(defn editor
  [{:keys [url value editor title markdown?]}]
  (let [state (reagent/atom {:value value :editing? false})
        save #(dispatch [:import/update url key (:value @state)])
        stop #(swap! state assoc :editing? false)
        save-local #(swap! state assoc :value (.. % -target -value))]
    (fn [{:keys [url value editor title markdown?]}]
      (let [{:keys [editing? value]} @state]
        [:div
         [:h3 title]
         (if editing?
           [editor {:value value
                    :change save-local
                    :stop stop
                    :save save}]
           [:div {:on-click #(swap! state assoc :editing? true)}
            (if markdown?
              [markdown value]
              value)])]))))
