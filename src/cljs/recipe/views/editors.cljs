(ns recipe.views.editors
  (:require [reagent.core :as reagent]
            [markdown.core :refer [md->html]]
            [re-frame.core :refer [dispatch subscribe]]
            [re-frame.core :as re-frame]))

(defn- input'
  [{:keys [save stop value]}]
  (let [state (reagent/atom value)
        change #(reset! state (.. % -target -value))
        save #(save @state)]
    (fn [& args]
      [:input {:value @state
               :on-key-down #(condp = (.-which %)
                               13 (do (save) (stop))
                               27 (stop)
                               nil)
               :on-change change
               :on-blur stop}])))

(def input (with-meta input' {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn textarea'
  [{:keys [save stop value]}]
  (let [state (reagent/atom value)
        change #(reset! state (.. % -target -value))
        save #(save @state)]
    (fn [& args]
      [:textarea {:value @state
                  :style {:width "100%"
                          :height "500px"}
                  :on-key-down #(cond
                                  (and (= (.-which %) 13)
                                       (.-shiftKey %)) (do
                                                         (.preventDefault %)
                                                         (save)
                                                         (stop))
                                  (= (.-which %) 27) (stop)
                                  :else nil)
                  :on-change change
                  :on-blur stop}])))

(def textarea (with-meta textarea' {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn markdown [value]
  [:div {:dangerouslySetInnerHTML
         {:__html (md->html value)}}])

(defn editor
  [{:keys [value editor title markdown? save]}]
  (let [state (reagent/atom {:editing? false})
        stop #(swap! state assoc :editing? false)]
    (fn [{:keys [editor title markdown? value]}]
      (let [{:keys [editing?]} @state]
        [:div
         [:h3 title]
         (if editing?
           [editor {:value value
                    :stop stop
                    :save save}]
           [:div {:on-click #(swap! state assoc :editing? true)}
            (if markdown?
              [markdown value]
              value)])]))))
