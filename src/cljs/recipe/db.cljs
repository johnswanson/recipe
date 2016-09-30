(ns recipe.db
  (:require [clojure.spec :as s]
            [taoensso.timbre :as log]))

(s/def :recipe/id integer?)
(s/def :recipe/ingredient string?)
(s/def :recipe/step string?)
(s/def :recipe/description string?)
(s/def :recipe/thumbnail-url string?)
(s/def :recipe/title string?)
(s/def :recipe/ingredients (s/coll-of :recipe/ingredient))
(s/def :recipe/steps (s/coll-of :recipe/step))

(s/def ::recipe
  (s/keys :req [:recipe/title
                :recipe/ingredients
                :recipe/steps
                :recipe/description
                :recipe/owner
                :recipe/id]
          :opt [:recipe/thumbnail-url]))

(s/def :user/id integer?)

(s/def ::by-id (s/map-of integer? (s/or ::recipe ::user)))

(s/def ::recipes (s/coll-of ::recipe))
(s/def ::user (s/keys :req [:user/id :user/username]))

(s/def ::logged-in-user (s/nilable :user/id))

(s/def ::db
  (s/keys :opt [::logged-in-user ::recipes ::by-id]))

(defn valid-schema?
  [db]
  (when-not (s/valid? ::db db)
    (binding [*print-fn* (fn [& args]
                           (.apply (.-error js/console)
                                   js/console
                                   (into-array args)))]
      (s/explain ::db db))))

(defn valid-db-schema?
  [{:keys [db] :as fx}]
  (when (and db (not (s/valid? ::db db)))
    (binding [*print-fn* (fn [& args]
                           (.apply (.-error js/console)
                                   js/console
                                   (into-array args)))]
      (s/explain ::db db))))

(def default-value {})

