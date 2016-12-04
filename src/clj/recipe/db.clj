(ns recipe.db
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clojure.java.jdbc :as j]
            [recipe.util :as util]))

(defn add-or-update-user!
  [conn user _]
  ;; throw access-token away.
  (first
   (try (j/insert! conn :users {:username (:login user)
                                :name     (:name user)})
       (catch org.postgresql.util.PSQLException e
         (j/query conn ["select * from users where username = ?" (:login user)])))))

(defn get-user [conn uid]
  (let [[user] (j/query conn ["select * from users where username = ?" uid])]
    (when user
      (util/namespaced-map user "user"))))

