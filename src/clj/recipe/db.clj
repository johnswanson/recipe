(ns recipe.db
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn- add-user
  "Returns db with user added"
  [db user access-token]
  (-> db
      (assoc-in [:users (:login user)] (assoc user :access-token access-token))))

(defn add-or-update-user!
  [conn user access-token]
  (swap! conn add-user user access-token))

(defn get-user [conn uid]
  (get-in @conn [:users uid]))

