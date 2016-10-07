(ns recipe.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn github-auth-url
  [db _]
  (:github/auth-url db))

(reg-sub :github/auth-url github-auth-url)

(defn logged-in-user
  [db _]
  (when-let [user (:recipe.db/logged-in-user db)]
    user))

(defn editing-recipe
  [db _]
  (:recipe.db/editing-recipe db))

(reg-sub :app/editing-recipe editing-recipe)

(reg-sub :app/logged-in-user logged-in-user)

(defn recipe-ids
  [db _]
  (:recipes.db/recipes db))

(reg-sub :app/recipes recipe-ids)

(defn recipe
  [db [_ id]]
  (get-in db [:recipes/by-id id]))

(reg-sub :app/recipe recipe)

(defn imports
  [db _]
  (vals (:recipe.db/imports db)))

(reg-sub :app/imports imports)
