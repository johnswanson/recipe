(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

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

(defn title-input [& {:keys [value on-key-down on-change stop]}]
  [:input {:value value
           :on-key-down on-key-down
           :on-change on-change
           :on-blur stop}])

(def title-edit (with-meta title-input {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn title-editor
  [orig-title & {:keys [on-save]}]
  (let [state (reagent/atom {:title orig-title
                             :editing? false})
        start #(swap! state assoc
                      :editing? true
                      :needs-focus? true)
        stop #(swap! state assoc :editing? false)
        temp-save #(swap! state assoc :title %)
        save #(let [v (-> @state :title str clojure.string/trim)]
                (when (and on-save (seq v)) (on-save v))
                (stop))
        on-change #(temp-save (.. % -target -value))
        on-key-down #(case (.-which %)
                       13 (save)
                       27 (stop)
                       nil)]
    (fn [orig-title & {:keys [on-save on-stop]}]
      (let [{:keys [title editing?]} @state]
        [:div
         (if editing?
           [title-edit :value title
            :on-key-down on-key-down
            :on-change on-change
            :stop stop]
           [:span
            {:on-click #(swap! state assoc :editing? true)}
            (or title
                [:i "title"])])
         (if (and (not editing?)
                  (not= title orig-title))
           [:i {:on-click #(swap! state assoc :title orig-title)} "(pending, click to revert)"])]))))

(defn description-input [& {:keys [value on-key-down on-change stop]}]
  [:textarea {:value value
           :on-key-down on-key-down
           :on-change on-change
           :on-blur stop}])

(def description-edit (with-meta description-input {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn description-editor
  [{:keys [value on-save]}]
  (let [state (reagent/atom {:value value
                             :editing? false})
        start #(swap! state assoc :editing? true)
        stop #(swap! state assoc :editing? false)
        save #(let [v (-> @state :value str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))
        on-change #(swap! state assoc :value (.. % -target -value))
        on-key-down #(cond
                       (and (= (.-which %) 13)
                            (.-shiftKey %)) (save)
                       (= (.-which %) 27) (stop)

                       :else nil)]
    (fn [{orig-value :value}]
      (let [{:keys [value editing?]} @state]
        [:div
         (if editing?
           [description-edit
            :value value
            :on-key-down on-key-down
            :on-change on-change
            :stop stop]
           [:div {:on-click #(swap! state assoc :editing? true)}
            (or value [:i "description"])])
         (if (and (not editing?)
                  (not= value orig-value))
           [:i {:on-click #(swap! state assoc :value orig-value)} "(pending, click to revert)"])]))))

(defn recipe-editor []
  (let [editing (subscribe [:app/editing-recipe])]
    (fn []
      (let [{:keys [recipe/title
                    recipe/ingredients
                    recipe/steps
                    recipe/description
                    recipe/owner
                    recipe/id]} @editing]
        [:div "Recipe Editor"
         [:div
          [title-editor title :on-save #(dispatch [:update-editing-recipe :recipe/title %])]
          [description-editor {:value description :on-save #(dispatch [:update-editing-recipe :recipe/description %])}]]]))))

(defn recipe-importer
  []
  (let [v (reagent/atom "")]
    (fn []
      [:div
       [:input {:value @v
                :on-change #(reset! v (.. % -target -value))}]
       [:button {:on-click #(dispatch [:start-import-recipe @v])}
        "Import"]])))

(defn logged-in-app
  [user]
  (let [recipes (subscribe [:app/recipes])]
    (fn logged-in-app [user]
      [:div
       [:div "Hello, " (:user/username user)]
       [recipe-importer]
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

