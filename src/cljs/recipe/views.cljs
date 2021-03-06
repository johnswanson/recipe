(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [markdown.core :refer [md->html]]
            [recipe.views.editors :as editors]
            [clojure.string :as str]))

(defn login-button
  []
  (let [auth-url (subscribe [:github/auth-url])]
    [:a {:href @auth-url} "login"]))

(defn logout-button
  [] [:a {:href "/logout"} "logout"])

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

(defn image-selector [url current possible]
  [:div
   (for [img possible]
     ^{:key img}
     [:img {:src img
            :style {:border (if (= current img)
                              "5px solid yellow"
                              "")}
            :width "100px"
            :height "100px"
            :on-click #(dispatch [:import/update url :recipe/thumbnail-url img])}])])

(defn title-editor
  [url title]
  [editors/editor {:save #(dispatch [:import/update url :recipe/title %])
                   :value title
                   :editor editors/input
                   :title "Title"}])

(defn note-editor
  [url notes]
  [editors/editor {:save #(dispatch [:import/update url :recipe/notes %])
                   :value notes
                   :editor editors/textarea
                   :title "Notes"
                   :markdown? true}])

(defn ingredient-editor
  [url ingredients]
  [:div [:h3 "Ingredients"]
   [editors/editor {:save #(dispatch [:import/update-ingredients url %])
                    :value (str/join "\n\n" ingredients)
                    :editor editors/textarea
                    :markdown? true}]])

(defn procedure-editor
  [url procedure]
  [editors/editor {:value procedure
                   :save #(dispatch [:import/update url :recipe/procedure %])
                   :editor editors/textarea
                   :title "Procedure"
                   :markdown? true}])

(defn import-editor [{:keys [import/recipe
                             import/url
                             import/possible-images]}]
  (let [{:keys [recipe/title
                recipe/notes
                recipe/ingredients
                recipe/procedure
                recipe/thumbnail-url]} recipe]
    [:div
     [:div
      [save-button url]
      [cancel-button url]]
     [image-selector url thumbnail-url possible-images]
     [title-editor url title]
     [ingredient-editor url ingredients]
     [note-editor url notes]
     [procedure-editor url procedure]]))

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
          [import-editor import])]])))

(defn logged-in-app
  [user]
  (let [recipes (subscribe [:app/recipes])]
    (fn logged-in-app [user]
      [:div
       [:div "Hello, " (:user/username user)]
       [recipe-importer]
       [recipe-import-list]
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

