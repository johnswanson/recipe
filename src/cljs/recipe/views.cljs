(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [markdown.core :refer [md->html]]
            [recipe.views.editors :as editors]))

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
  [:div])


(defn title-view
  [url title]
  [editors/editor {:url url
                   :value title
                   :editor editors/input
                   :title "Title"}])

(defn note-editor
  [url notes]
  [editors/editor {:url url
                   :value notes
                   :editor editors/textarea
                   :title "Notes"
                   :markdown? true}])

(defn ingredient-editor
  [url ingredients]
  [:div [:h3 "Ingredients"]
   [:ul (for [i ingredients]
          ^{:key i} [:li i])]])

(defn procedure-editor
  [url procedure]
  [editors/editor {:url url
                   :value procedure
                   :editor editors/textarea
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

(defn import-manager [import]
  [import-editor import])

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
  (let [user (subscribe [:app/logged-in-user])
        imports (subscribe [:app/imports])]
    (fn [] [:div [app-view @user]
            [:pre (with-out-str (cljs.pprint/pprint @imports))]])))

