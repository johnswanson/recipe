(ns recipe.db
  (:require [clojure.spec :as s]
            [taoensso.timbre :as log]))

(defn tempid [] -1)

(s/def :recipe/id integer?)
(s/def :recipe/ingredient string?)
(s/def :recipe/thumbnail-url string?)
(s/def :recipe/title string?)
(s/def :recipe/ingredients (s/coll-of :recipe/ingredient))
(s/def :recipe/procedure string?)
(s/def :recipe/source-url string?)

(s/def ::recipe
  (s/keys :req [:recipe/title
                :recipe/ingredients
                :recipe/procedure]
          :opt [:recipe/id
                :recipe/thumbnail-url
                :recipe/source-url]))

(s/def :import/recipe ::recipe)
(s/def :import/body string?)
(s/def :import/possible-images (s/coll-of :recipe/thumbnail-url))
(s/def :import/status keyword?)
(s/def :import/url string?)

(s/def ::import
  (s/keys :req [:import/status
                :import/url]
          :opt [:import/recipe
                :import/body
                :import/possible-images
                :import/error]))

(s/def ::imports (s/map-of :import/url ::import))

(s/def ::editing-recipe ::recipe)


(s/def :recipes/by-id (s/map-of :recipe/id ::recipe))

(s/def ::recipes (s/coll-of :recipe/id))
(s/def :user/id integer?)
(s/def ::user (s/keys :req [:user/id :user/username]))

(s/def ::logged-in-user (s/nilable ::user))

(s/def ::db
  (s/keys :opt [::logged-in-user
                ::recipes
                :recipes/by-id
                ::editing-recipe
                ::imports]))

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
