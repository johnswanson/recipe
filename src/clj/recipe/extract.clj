(ns recipe.extract
  (:require [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [datomic-schema.schema :as s]))

(defn host [url] (.getHost url))

(defn clean-str [s]
  (-> s
      (str/trim)
      (str/replace (-> 160 char str) " ")
      (str/replace #"\s+" " ")))

(defprotocol IExtractable
  "Something we can extract recipe data from"
  (images [this])
  (ingredients [this])
  (title [this])
  (procedure [this])
  (notes [this]))

(defrecord SmittenKitchenExtractable [html]
  IExtractable
  (images [this]
    (map
     #(first (html/attr-values % :src))
     (html/select html [:img])))
  (ingredients [this]
    (map clean-str
         (-> html
             (html/select [:li.jetpack-recipe-ingredient])
             (html/texts))))
  (title [this]
    (-> html
        (html/select [:h1.entry-title])
        (html/texts)
        (first)
        (clean-str)))
  (procedure [this]
    (-> html
        (html/select [:div.jetpack-recipe-directions])
        (html/texts)
        (str/join)
        (clean-str)))
  (notes [this]
    (-> html
        (html/select [:div.jetpack-recipe-notes])
        (html/texts)
        (str/join)
        (clean-str))))

(defrecord SeriousEatsExtractable [html]
  IExtractable
  (images [this]
    (map #(first (html/attr-values % :src))
         (html/select html [:img])))
  (ingredients [this]
    (map clean-str
         (-> html
             (html/select [:li.ingredient])
             (html/texts))))
  (title [this]
    (-> html
        (html/select [:h1.recipe-title])
        (html/texts)
        (first)
        (clean-str)))
  (procedure [this]
    (-> html
        (html/select [:div.recipe-procedure-text])
        (html/texts)
        (str/join)
        (clean-str)))
  (notes [this]
    (-> html
        (html/select [:div.recipe-introduction-body])
        (html/texts)
        (str/join)
        (clean-str))))

(def serious-eats? (partial re-find #"seriouseats\.com"))
(def smitten-kitchen?  (partial re-find #"smittenkitchen\.com"))

(defn site [url]
  (cond
    (serious-eats? url) :serious-eats
    (smitten-kitchen? url) :smitten-kitchen))

(defn html [url]
  (html/html-resource (java.net.URL. url)))

(defn ->extractable [url]
  (condp = (site url)
    :serious-eats (->SeriousEatsExtractable (html url))
    :smitten-kitchen (->SmittenKitchenExtractable (html url))))

(defn parse [url]
  (when-let [extractable (->extractable url)]
    {:ingredients (ingredients extractable)
     :notes (notes extractable)
     :procedure (procedure extractable)
     :title (title extractable)
     :possible-images (images extractable)}))
