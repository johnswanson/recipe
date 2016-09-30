(ns recipe.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn github-auth-url
  [db _]
  (:github/auth-url db))

(reg-sub :github/auth-url github-auth-url)

(defn logged-in-user
  [db _]
  (when-let [user-id (:recipe.db/logged-in-user db)]
    (get-in db [:recipe.db/by-id user-id])))

(reg-sub :app/logged-in-user logged-in-user)

(defn recipe-ids
  [db _]
  (:recipes.db/recipes db))

(reg-sub :app/recipes recipe-ids)

(defn recipe
  [db [_ id]]
  (get-in db [:recipe.db/by-id id]))

(reg-sub :app/recipe recipe)
