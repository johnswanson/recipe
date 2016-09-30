(ns recipe.datomic
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(def schema
  [(s/schema user
             (s/fields
              [username :string :unique-identity]
              [access-token :string]))])

(def options {:index-all? true})

(def parts [(s/part "app")])

(def init-tx (concat (s/generate-parts parts)
                     (s/generate-schema schema options)))

(defn init [conn] (d/transact conn init-tx))

(defn user [gh-user]
  {:user/username (:login gh-user)
   :db/id (d/tempid :db.part/user)})

(defn add-or-update-user! [conn gh-user access-token]
  (d/transact conn [(-> gh-user
                        (user)
                        (assoc :user/access-token access-token))]))

(defn get-user [conn username]
  (let [res (d/pull (d/db conn) '[*] [:user/username username])]
    (when-let [id (:db/id res)]
      (assoc res :user/id id))))

