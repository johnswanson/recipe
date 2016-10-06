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
    (map #(-> % html/text clean-str) (html/select html [:li.jetpack-recipe-ingredient])))
  (title [this]
    (clean-str (html/text (first (html/select html [:h1.entry-title])))))
  (procedure [this]
    (clean-str (str/join (map html/text (html/select html [:div.jetpack-recipe-directions])))))
  (notes [this]
    (clean-str (str/join (map html/text (html/select html [:div.jetpack-recipe-notes]))))))

(defrecord SeriousEatsExtractable [html]
  IExtractable
  (images [this]
    (map #(first (html/attr-values % :src))
         (html/select html [:img])))
  (ingredients [this]
    (map #(-> % html/text clean-str) (html/select html [:li.ingredient])))
  (title [this]
    (-> html
        (html/select [:h1.recipe-title])
        (first)
        (html/text)
        (clean-str)))
  (procedure [this]
    (clean-str (str/join (map html/text (html/select html [:div.recipe-procedure-text])))))
  (notes [this]
    (clean-str
     (str/join
      (map html/text (html/select html [:div.recipe-introduction-body]))))))

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
