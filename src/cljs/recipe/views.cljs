(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [markdown.core :refer [md->html]]))

(defn input'
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

(defn login-button
  []
  (let [auth-url (subscribe [:github/auth-url])]
    [:a {:href @auth-url} "login"]))

(defn logout-button
  [] [:a {:href "/logout"} "logout"])

(defn recipe-list [ids]
  [:div
   (for [id ids]
     ^{:key id} [:div id])])

(defn recipe-importer
  []
  (let [v (reagent/atom "")]
    (fn []
      [:div
       [:input {:value @v
                :on-change #(reset! v (.. % -target -value))}]
       [:button {:on-click #(dispatch [:start-import-recipe @v])}
        "Import"]])))

(defn save-button [url] [:button {:on-click #(dispatch [:save-import url])} "Save"])
(defn cancel-button [url] [:button {:on-click #(dispatch [:cancel-import url])} "Cancel"])

(defn image-selector [url possible-images]
  [:div [:h3 "Images"] ;; image-selector
   (for [img possible-images]
     ^{:key img} [:img {:width "100px"
                        :height "100px"
                        :on-click #(dispatch [:import/select-image url img])
                        :src img}])])

(defn markdown [value]
  [:div {:dangerouslySetInnerHTML
         {:__html (md->html value)}}])

(defn editor
  [{:keys [url value editor title markdown?]}]
  (console.log url title editor)
  (let [state (reagent/atom {:value value :editing? false})
        save #(dispatch [:import/update url key (:value @state)])
        stop #(swap! state assoc :editing? false)
        save-local #(swap! state assoc :value (.. % -target -value))]
    (fn [{:keys [url value editor title markdown?]}]
      (console.log url title editor)
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

(defn title-view
  [url title]
  [editor {:url url
           :value title
           :editor input
           :title "Title"}])

(defn note-editor
  [url notes]
  [editor {:url url
           :value notes
           :editor textarea
           :title "Notes"}])

(defn ingredient-editor
  [url ingredients]
  [:div [:h3 "Ingredients"]
   [:ul (for [i ingredients]
          ^{:key i} [:li i])]])

(defn procedure-editor
  [url procedure]
  [editor {:url url
           :value procedure
           :editor textarea
           :title "Procedure"
           :markdown? true}])

(defn import-editor [{:keys [import/recipe
                             import/url
                             import/possible-images]}]
  (let [{:keys [recipe/title
                recipe/notes
                recipe/ingredients
                recipe/procedure]} recipe]
    [:div
     [:div
      [save-button url]
      [cancel-button url]]
     [image-selector url possible-images]
     [title-view url title]
     [note-editor url notes]
     [procedure-editor url procedure]]))

(defn import-manager [{:keys [import/recipe import/body] :as import}]
  [:div.import-manager {:style {:display :flex
                                :margin "5%"
                                :height "500px"}}
   [:div ;; import-editor
    {:style {:overflow-y :scroll}}
    [import-editor import]]])

(defn recipe-import-list
  []
  (let [imports (subscribe [:app/imports])]
    (fn []
      [:div {:style {:display :flex
                     :justify-content :space-around}}
       [:div {:style {:width "80%"
                      :border "2px solid black"}}
        (for [import @imports]
          ^{:key (:import/url import)}
          [import-manager import])]])))

(defn logged-in-app
  [user]
  (let [recipes (subscribe [:app/recipes])]
    (fn logged-in-app [user]
      [:div
       [:div "Hello, " (:user/username user)]
       [recipe-importer]
       [recipe-import-list]
       [recipe-list @recipes]
       [:div [logout-button]]])))

(defn app-view [user]
  (if user
    [logged-in-app user]
    [login-button]))

(defn app
  []
  (let [user (subscribe [:app/logged-in-user])]
    (fn [] [app-view @user])))

